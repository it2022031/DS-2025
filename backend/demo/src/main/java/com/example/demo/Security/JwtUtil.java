package com.example.demo.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final Key signingKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:3600000}") long expirationMillis
    ) {
        this.expirationMillis = expirationMillis;
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
            if (keyBytes.length < 32) {
                logger.warn("Decoded JWT secret is shorter than 256 bits ({} bytes). It's recommended to use at least 256-bit.", keyBytes.length);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("JWT secret is not valid Base64; falling back to raw bytes. This is less secure.", e);
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                logger.warn("Raw JWT secret is shorter than 256 bits ({} bytes).", keyBytes.length);
            }
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Δημιουργεί token με subject = username και claim "roles" από το UserDetails.
     */
    public String generateToken(UserDetails userDetails) {
        return generateTokenWithExtraClaims(userDetails, Collections.emptyMap());
    }

    /**
     * Δημιουργεί token με επιπλέον claims αν χρειάζεται.
     */
    public String generateTokenWithExtraClaims(UserDetails userDetails, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        Map<String, Object> claims = new HashMap<>(extraClaims);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Εξάγει το username (subject) από το token, αν είναι έγκυρο.
     */
    public Optional<String> extractUsername(String token) {
        try {
            return Optional.ofNullable(parseClaims(token).getSubject());
        } catch (JwtException e) {
            logger.debug("Could not extract username from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Εξάγει τους ρόλους από το claim "roles".
     */
    public List<String> extractRoles(String token) {
        try {
            Claims claims = parseClaims(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?> list) {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        } catch (JwtException e) {
            logger.debug("Could not extract roles from token: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Ελέγχει αν το token έχει λήξει.
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true; // άκυρο / ληγμένο
        }
    }

    /**
     * Επικυρώνει ότι το token ταιριάζει στον userDetails και δεν έχει λήξει.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            String username = claims.getSubject();
            if (!username.equals(userDetails.getUsername())) return false;
            if (claims.getExpiration().before(new Date())) return false;
            return true;
        } catch (JwtException e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Κεντρική μέθοδος για pars-άρισμα και επαλήθευση του JWT (signature + structure).
     * Αν το token είναι άκυρο/κατεστραμμένο/ληγμένο θα πετάξει JwtException.
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

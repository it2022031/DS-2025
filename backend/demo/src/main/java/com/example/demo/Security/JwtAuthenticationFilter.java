package com.example.demo.Security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(UserDetailsService uds, JwtUtil jwtUtil) {
        this.userDetailsService = uds;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Πάρε την Authorization header
        String header = request.getHeader("Authorization");

        // Έλεγξε ότι υπάρχει και ξεκινά με "Bearer "
        if (header != null && header.startsWith("Bearer ")) {
            // Κόψε το "Bearer " και κράτα μόνο το token
            String token = header.substring(7);

            try {
                // Προσπάθησε να εξάγεις το username από το JWT (μπορεί να λείπει -> Optional)
                Optional<String> usernameOpt = jwtUtil.extractUsername(token);

                // Αν βρέθηκε username ΚΑΙ δεν υπάρχει ήδη Authentication στο SecurityContext
                if (usernameOpt.isPresent() &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    String username = usernameOpt.get();

                    // Φόρτωσε τα UserDetails (ρόλοι/δικαιώματα κλπ.) από το δικό σου service
                    UserDetails ud = userDetailsService.loadUserByUsername(username);

                    // Επικύρωσε ότι το token είναι έγκυρο για τα συγκεκριμένα UserDetails
                    if (jwtUtil.validateToken(token, ud)) {

                        // Δημιούργησε Authentication object με τα authorities του χρήστη
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());

                        // Πρόσθεσε επιπλέον στοιχεία request (IP, session id κ.ά.) στο auth
                        authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request));

                        // Πέρασε το Authentication στο SecurityContext ώστε τα επόμενα φίλτρα/controllers
                        // να "βλέπουν" τον χρήστη ως authenticated
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (JwtException e) {
                // Αν το token είναι άκυρο/ληγμένο/πειραγμένο, φτάνουμε εδώ.
            }
        }
        filterChain.doFilter(request, response);
    }
}

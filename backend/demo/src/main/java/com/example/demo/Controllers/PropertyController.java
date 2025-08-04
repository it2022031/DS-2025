package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;

    public PropertyController(PropertyService propertyService, UserRepository userRepository) {
        this.propertyService = propertyService;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProperties(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admins can list all properties"));
        }
        List<Property> props = propertyService.findAll();
        return ResponseEntity.ok(props);
    }
//    @PostMapping("/add")
//    public ResponseEntity<Property> addProperty(@RequestBody Property dto) {
//        Property newProperty = new Property();
//        newProperty.setName(dto.getName());
//        newProperty.setDescription(dto.getDescription());
//        newProperty.setCountry(dto.getCountry());
//        newProperty.setCity(dto.getCity());
//        newProperty.setStreet(dto.getStreet());
//        newProperty.setPostalCode(dto.getPostalCode());
//        newProperty.setSquareMeters(dto.getSquareMeters());
//        newProperty.setStatus(dto.getStatus());
//
//        Property saved = propertyService.addProperty(newProperty);
//        return ResponseEntity.ok(saved);
//    }

    @PostMapping("/add")
    public ResponseEntity<?> addProperty(@RequestBody PropertyCreateRequest req,
                                         @RequestParam(required = false) Long ownerId,
                                         Authentication authentication) {
        String callerUsername = extractUsername(authentication);
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        // Καθορισμός finalOwnerId με προτεραιότητα: query param > body.ownerId > caller
        Long finalOwnerId;
        if (ownerId != null) {
            if (!isAdmin(authentication) && !ownerId.equals(caller.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot assign property to another user"));
            }
            finalOwnerId = ownerId;
        } else if (req.ownerId() != null) {
            if (!isAdmin(authentication) && !req.ownerId().equals(caller.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot assign property to another user"));
            }
            finalOwnerId = req.ownerId();
        } else {
            finalOwnerId = caller.getId();
        }

        // Βασική validation
        if (req.name() == null || req.name().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Property name is required"));
        }

        // Κατασκευή entity από request
        Property property = new Property();
        property.setName(req.name());
        property.setDescription(req.description());
        property.setCountry(req.country());
        property.setCity(req.city());
        property.setStreet(req.street());
        property.setPostalCode(req.postalCode());
        property.setSquareMeters(req.squareMeters());
        property.setStatus(req.status());

        try {
            Property saved = propertyService.createWithOwner(finalOwnerId, property);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    // βοηθητικά (μπορείς να τα βάλεις σε base class ή εδώ)
    private String extractUsername(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return principal.toString();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public record PropertyCreateRequest(
            String name,
            String description,
            String country,
            String city,
            String street,
            String postalCode,
            Double squareMeters,
            Boolean status,
            Long ownerId // προαιρετικό, μπορεί να περαστεί εδώ αντί για ?ownerId=
    ) {}


}



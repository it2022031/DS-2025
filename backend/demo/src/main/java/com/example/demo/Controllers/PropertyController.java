package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.User;
import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Services.PropertyService;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.PropertyCreateRequest;
import com.example.demo.dto.PropertyDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;

    public PropertyController(PropertyService propertyService, UserRepository userRepository) {
        this.propertyService = propertyService;
        this.userRepository = userRepository;
    }



    // 1. List all properties (admin only)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyDto>> getAllProperties() {
        List<PropertyDto> dto = propertyService.findAll().stream()
                .map(PropertyDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // 2. Create new property
    @PostMapping
    public ResponseEntity<?> createProperty(
            @RequestBody PropertyCreateRequest req,
            @RequestParam(required = false) Long ownerId,
            Authentication auth) {

        // 1) βγάλε το username του καλούντα
        String callerUsername = ((UserDetails)auth.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 2) καθόρισε το πραγματικό ownerId
        Long finalOwnerId;
        if (ownerId != null) {
            // αν πέρασε ownerId, μόνο admin το επιτρέπουμε
            if (!isAdmin) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can assign properties to other users"));
            }
            finalOwnerId = ownerId;
        } else {
            // χωρίς ownerId, ο καλών γίνεται owner
            finalOwnerId = caller.getId();
        }

        // 3) basic validation
        if (req.name() == null || req.name().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Property name is required"));
        }

        // 4) φτιάξε το entity
        Property p = new Property();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setCountry(req.country());
        p.setCity(req.city());
        p.setStreet(req.street());
        p.setPostalCode(req.postalCode());
        p.setSquareMeters(req.squareMeters());
        p.setApprovalStatus(ApprovalStatus.PENDING);

        // 5) delega te στο service
        Property saved = propertyService.createWithOwner(finalOwnerId, p);
        return ResponseEntity.ok(PropertyDto.fromEntity(saved));
    }


    // 3. Get property by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        Property p = propertyService.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return ResponseEntity.ok(PropertyDto.fromEntity(p));
    }

    // 4. Patch property (owner can update fields, admin can also change approval)
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchProperty(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        Property prop = propertyService.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String callerUsername = extractUsername(authentication);
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean admin = isAdmin(authentication);
        boolean owner = prop.getOwner().getId().equals(caller.getId());
        if (!admin && !owner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not allowed"));
        }

        // common updates
        updates.forEach((k,v) -> {
            switch(k) {
                case "name": prop.setName((String)v); break;
                case "description": prop.setDescription((String)v); break;
                case "country": prop.setCountry((String)v); break;
                case "city": prop.setCity((String)v); break;
                case "street": prop.setStreet((String)v); break;
                case "postalCode": prop.setPostalCode((String)v); break;
                case "squareMeters": prop.setSquareMeters(((Number)v).doubleValue()); break;
                case "approvalStatus":
                    if (!admin) throw new RuntimeException("Only admin can change approvalStatus");
                    prop.setApprovalStatus(ApprovalStatus.valueOf(v.toString().toUpperCase()));
                    break;
            }
        });

        Property saved = propertyService.save(prop);
        return ResponseEntity.ok(PropertyDto.fromEntity(saved));
    }

    // 5. Approve / reject (admin only)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        Property updated = propertyService.setApprovalStatus(id, ApprovalStatus.APPROVED);
        return ResponseEntity.ok(PropertyDto.fromEntity(updated));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        Property updated = propertyService.setApprovalStatus(id, ApprovalStatus.REJECTED);
        return ResponseEntity.ok(PropertyDto.fromEntity(updated));
    }

    // helpers
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

    private Long resolveOwnerId(Long bodyId, Long paramId, User caller, Authentication auth) {
        if (paramId != null) return paramId;
        if (bodyId != null) return bodyId;
        return caller.getId();
    }
}

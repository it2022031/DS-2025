package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.PropertyPhoto;
import com.example.demo.Entities.User;
import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Repositories.PropertyPhotoRepository;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Services.PropertyService;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.PropertyCreateRequest;
import com.example.demo.dto.PropertyDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class PropertyController {

    private final PropertyService propertyService;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    public PropertyController(PropertyService propertyService, UserRepository userRepository, PropertyRepository propertyRepository, PropertyPhotoRepository propertyPhotoRepository) {
        this.propertyService = propertyService;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.propertyPhotoRepository = propertyPhotoRepository;
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePropertyAsAdmin(@PathVariable Long id) {
        propertyService.adminDeleteProperty(id);
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/status/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Property>> getAllRejectedProperties() {
        return ResponseEntity.ok(propertyService.getRejectedProperties());
    }

    @DeleteMapping("/status/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAllRejectedProperties() {
        propertyService.deleteAllRejectedProperties();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{propertyId}/photos")
    public ResponseEntity<?> uploadPropertyPhoto(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Έλεγχος αν ο χρήστης είναι ο ιδιοκτήτης ή admin
        User caller = userRepository.findByUsername(
                ((UserDetails) authentication.getPrincipal()).getUsername()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = property.getOwner().getId().equals(caller.getId());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 🛑 Έλεγχος μέγιστου αριθμού φωτογραφιών

        long existingPhotos = propertyPhotoRepository.countByPropertyId(propertyId);
        if (existingPhotos >= 5 && !isAdmin) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Μπορείς να ανεβάσεις μέχρι 5 φωτογραφίες")
            );
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }

        PropertyPhoto photo = new PropertyPhoto();
        photo.setImage(file.getBytes());
        photo.setContentType(file.getContentType());
        photo.setFilename(file.getOriginalFilename());
        photo.setProperty(property);

        propertyPhotoRepository.save(photo);

        return ResponseEntity.ok(Map.of("message", "Photo uploaded successfully"));
    }


    @GetMapping("/photos/{photoId}")
    public ResponseEntity<?> getPropertyPhoto(@PathVariable Long photoId) {
        PropertyPhoto photo = propertyPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, photo.getContentType())
                .body(photo.getImage());
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<?> deletePropertyPhoto(
            @PathVariable Long photoId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PropertyPhoto photo = propertyPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        User caller = userRepository.findByUsername(
                ((UserDetails) authentication.getPrincipal()).getUsername()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = photo.getProperty().getOwner().getId().equals(caller.getId());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        propertyPhotoRepository.delete(photo);

        return ResponseEntity.ok(Map.of("message", "Photo deleted successfully"));
    }

    @GetMapping("/{propertyId}/photos")
    public ResponseEntity<?> getAllPropertyPhotos(@PathVariable Long propertyId) {
        List<PropertyPhoto> photos = propertyPhotoRepository.findByPropertyId(propertyId);

        // Αν δεν έχει, καλύτερα 200 με [] αντί για 404 (πιο φιλικό για UI)
        if (photos.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Φτιάχνουμε URL προς το ήδη υπάρχον endpoint /api/properties/photos/{photoId}
        List<Map<String, Object>> photoDtos = photos.stream()
                .map(p -> {
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()                // π.χ. http://localhost:8080
                            .path("/api/properties/photos/")        // σταθερό μέρος
                            .path(p.getId().toString())             // το id
                            .toUriString();

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("url", url);                          // <-- μόνο URL, όχι bytes
                    m.put("contentType", p.getContentType());
                    m.put("filename", p.getFilename());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(photoDtos);
    }

}

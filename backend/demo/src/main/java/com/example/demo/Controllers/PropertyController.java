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
import com.example.demo.dto.DateRange;
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
import java.math.BigDecimal;
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



    // List all properties (admin only)
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','RENTER')")
    public ResponseEntity<List<PropertyDto>> getAllProperties() {
        List<PropertyDto> dto = propertyService.findAll().stream()
                .map(PropertyDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    // Create new property
    @PostMapping
    public ResponseEntity<?> createProperty(
            @RequestBody PropertyCreateRequest req,
            @RequestParam(required = false) Long ownerId,
            Authentication auth) {

        String callerUsername = ((UserDetails)auth.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long finalOwnerId;
        if (ownerId != null) {
            if (!isAdmin) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only admins can assign properties to other users"));
            }
            finalOwnerId = ownerId;
        } else {
            finalOwnerId = caller.getId();
        }

        if (req.name() == null || req.name().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Property name is required"));
        }
        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Price must be positive"));
        }

        Property p = new Property();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setCountry(req.country());
        p.setCity(req.city());
        p.setStreet(req.street());
        p.setPostalCode(req.postalCode());
        p.setSquareMeters(req.squareMeters());
        p.setApprovalStatus(ApprovalStatus.PENDING);
        p.setPrice(req.price());

        Property saved = propertyService.createWithOwner(finalOwnerId, p);
        return ResponseEntity.ok(PropertyDto.fromEntity(saved));
    }


    // Get property by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable Long id) {
        Property p = propertyService.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return ResponseEntity.ok(PropertyDto.fromEntity(p));
    }

    // Patch property (owner can update fields, admin can also change approval)
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchProperty(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        Property prop = propertyService.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        String callerUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = prop.getOwner() != null && prop.getOwner().getId().equals(caller.getId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Not allowed"));
        }

        // επιτρεπτά πεδία για owner
        Set<String> ownerAllowed = Set.of(
                "name", "description", "country", "city", "street",
                "postalCode", "squareMeters", "price" // <-- price (BigDecimal)
        );

        // μόνο για admin
        Set<String> adminOnly = Set.of("approvalStatus");

        List<String> unknown = new ArrayList<>();
        List<String> notAllowed = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        for (Map.Entry<String, Object> e : updates.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();

            boolean keyAllowedForOwner = ownerAllowed.contains(key);
            boolean keyAdminOnly = adminOnly.contains(key);

            if (!keyAllowedForOwner && !keyAdminOnly) {
                unknown.add(key);
                continue;
            }
            if (!isAdmin && keyAdminOnly) {
                notAllowed.add(key);
                continue;
            }

            try {
                switch (key) {
                    case "name" -> prop.setName(asString(val));
                    case "description" -> prop.setDescription(asString(val));
                    case "country" -> prop.setCountry(asString(val));
                    case "city" -> prop.setCity(asString(val));
                    case "street" -> prop.setStreet(asString(val));
                    case "postalCode" -> prop.setPostalCode(asString(val));

                    case "squareMeters" -> {
                        Double d = asDouble(val);
                        if (d == null || d <= 0) validationErrors.add("squareMeters must be > 0");
                        else prop.setSquareMeters(d);
                    }

                    case "price" -> {
                        BigDecimal bd = asBigDecimal(val);
                        if (bd == null || bd.compareTo(BigDecimal.ZERO) <= 0)
                            validationErrors.add("price must be > 0");
                        else
                            prop.setPrice(bd);
                    }

                    case "approvalStatus" -> {
                        String s = asString(val);
                        try { prop.setApprovalStatus(ApprovalStatus.valueOf(s)); }
                        catch (Exception ex) {
                            validationErrors.add("approvalStatus must be one of: " +
                                    Arrays.toString(ApprovalStatus.values()));
                        }
                    }

                    default -> unknown.add(key);
                }
            } catch (ClassCastException ex) {
                validationErrors.add("Invalid type for field '" + key + "'");
            }
        }

        if (!unknown.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unknown fields: " + String.join(", ", unknown)));
        }
        if (!notAllowed.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Fields not allowed for your role: " + String.join(", ", notAllowed)));
        }
        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", validationErrors));
        }

        Property saved = propertyService.save(prop);
        return ResponseEntity.ok(PropertyDto.fromEntity(saved));
    }

    // Helpers
    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
    private static Double asDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (NumberFormatException e) { return null; }
    }
    private static java.math.BigDecimal asBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof java.math.BigDecimal b) return b;
        if (v instanceof Number n) return new java.math.BigDecimal(n.toString());
        try { return new java.math.BigDecimal(String.valueOf(v)); }
        catch (NumberFormatException e) { return null; }
    }

    // Approve / reject (admin only)
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

    // Helpers
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
    public ResponseEntity<?> deleteProperty(
            @PathVariable Long id,
            Authentication authentication
    ) {
        // Βρες το property
        Property property = propertyService.findByIdOptional(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        // Βρες τον τρέχοντα χρήστη
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isOwner = property.getOwner() != null &&
                property.getOwner().getId().equals(caller.getId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not allowed to delete this property"));
        }

        // Κάνε το delete
        propertyService.adminDeleteProperty(id);
        return ResponseEntity.noContent().build();
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

        // Έλεγχος μέγιστου αριθμού φωτογραφιών
        long existingPhotos = propertyPhotoRepository.countByPropertyId(propertyId);
        if (existingPhotos >= 5 && !isAdmin) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Μπορείς να ανεβάσεις μέχρι 5 φωτογραφίες")
            );
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }

        // Έλεγχος μεγέθους αρχείου (μέχρι 10MB)
        final long MAX_SIZE = 10L * 1024 * 1024; // 10 MB
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Το αρχείο ξεπερνά το επιτρεπόμενο όριο των 10MB"));
        }

        // Αποθήκευση φωτογραφίας
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

    @PatchMapping("/photos/{photoId}")
    public ResponseEntity<?> replacePhoto(
            @PathVariable Long photoId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        // έλεγχος ρόλου admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // φόρτωσε το photo και βρες τον owner
        var photo = propertyPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));

        var ownerId = photo.getProperty().getOwner().getId();

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        var caller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = caller.getId().equals(ownerId);

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only admin or property owner can update photo"));
        }

        try {
            photo.setImage(file.getBytes());
            propertyPhotoRepository.save(photo);
            return ResponseEntity.ok(Map.of("message", "Photo updated successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update photo"));
        }
    }

    @GetMapping("/{propertyId}/photos")
    public ResponseEntity<?> getAllPropertyPhotos(@PathVariable Long propertyId) {
        List<PropertyPhoto> photos = propertyPhotoRepository.findByPropertyId(propertyId);

        if (photos.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // URL προς το ήδη υπάρχον endpoint /api/properties/photos/{photoId}
        List<Map<String, Object>> photoDtos = photos.stream()
                .map(p -> {
                    String url = ServletUriComponentsBuilder
                            .fromCurrentContextPath()                // http://localhost:8080
                            .path("/api/properties/photos/")        // σταθερό μέρος
                            .path(p.getId().toString())             // το id
                            .toUriString();

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("url", url);                          // μόνο URL, όχι bytes
                    m.put("contentType", p.getContentType());
                    m.put("filename", p.getFilename());
                    return m;
                })
                .toList();

        return ResponseEntity.ok(photoDtos);
    }

    @GetMapping("/{id}/closed-dates")
    public ResponseEntity<?> getClosedDates(
            @PathVariable Long id,
            @RequestParam(name = "flatten", defaultValue = "false") boolean flatten
    ) {
        if (flatten) {
            List<String> dates = propertyService.getClosedDatesFlat(id);
            return ResponseEntity.ok(dates);
        } else {
            List<DateRange> ranges = propertyService.getClosedDateRanges(id);
            return ResponseEntity.ok(ranges);
        }
    }

}

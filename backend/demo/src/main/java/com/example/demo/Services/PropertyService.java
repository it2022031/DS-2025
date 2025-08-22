package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.PropertyPhoto;
import com.example.demo.Entities.User;
import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Repositories.PropertyPhotoRepository;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.DateRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final PropertyPhotoRepository photoRepo;

    public PropertyService(PropertyRepository propertyRepository,
                           UserRepository userRepository, RentalRepository rentalRepository, PropertyPhotoRepository photoRepo) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
        this.photoRepo = photoRepo;
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Optional<Property> findByIdOptional(Long id) {
        return propertyRepository.findById(id);
    }

    @Transactional
    public Property createWithOwner(Long ownerId, Property property) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id " + ownerId));
        owner.addProperty(property);
        property.setApprovalStatus(ApprovalStatus.PENDING);
        return propertyRepository.save(property);
    }

    @Transactional
    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    @Transactional
    public Property setApprovalStatus(Long propertyId, ApprovalStatus newStatus) {
        Property prop = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found with id " + propertyId));
        prop.setApprovalStatus(newStatus);
        return propertyRepository.save(prop);
    }

    /** Admin: διαγραφή οποιουδήποτε property */
    @Transactional
    public void adminDeleteProperty(Long propertyId) {
        Property p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));

        // εδώ μπορείς προαιρετικά να κάνεις validations (π.χ. να μην έχει ενεργά rentals)
        // if (rentalRepository.existsByPropertyIdAndStatusActive(propertyId)) throw new ...

        propertyRepository.delete(p); // ενεργοποιεί cascade/orphanRemoval προς bookings/rentals
    }

    public List<Property> getRejectedProperties() {
        return propertyRepository.findByApprovalStatus(ApprovalStatus.REJECTED);
    }

    @Transactional
    public void deleteAllRejectedProperties() {
        propertyRepository.deleteByApprovalStatus(ApprovalStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public List<DateRange> getClosedDateRanges(Long propertyId) {
        // βεβαιώσου ότι υπάρχει το property (προαιρετικό)
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        LocalDate today = LocalDate.now();

        var rentals = rentalRepository.findByPropertyIdAndApprovalStatusInAndEndDateGreaterThanEqual(
                propertyId, List.of(ApprovalStatus.APPROVED, ApprovalStatus.PENDING), today
        );


        // μετατροπή σε ranges (clip το start στο today αν είναι παρελθόν)
        List<DateRange> raw = new ArrayList<>();
        for (var r : rentals) {
            LocalDate start = r.getStartDate().isBefore(today) ? today : r.getStartDate();
            LocalDate end   = r.getEndDate();
            if (!end.isBefore(start)) {
                raw.add(new DateRange(start.toString(), end.toString()));
            }
        }

        // merge overlapping/adjacent ranges
        return mergeRanges(raw);
    }

    /**
     * Επιστρέφει flat λίστα με όλες τις μεμονωμένες ημερομηνίες (yyyy-MM-dd)
     * που είναι κλειστό το property, με βάση τα merged ranges.
     */
    @Transactional(readOnly = true)
    public List<String> getClosedDatesFlat(Long propertyId) {
        List<DateRange> ranges = getClosedDateRanges(propertyId);
        List<String> days = new ArrayList<>();
        for (DateRange dr : ranges) {
            LocalDate s = LocalDate.parse(dr.startDate());
            LocalDate e = LocalDate.parse(dr.endDate());
            for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) {
                days.add(d.toString());
            }
        }
        return days;
    }

    // ---- helpers ----

    private List<DateRange> mergeRanges(List<DateRange> ranges) {
        if (ranges.isEmpty()) return ranges;

        // sort by start
        List<DateRange> sorted = new ArrayList<>(ranges);
        Collections.sort(sorted, Comparator.comparing(r -> LocalDate.parse(r.startDate())));

        List<DateRange> merged = new ArrayList<>();
        LocalDate curStart = LocalDate.parse(sorted.get(0).startDate());
        LocalDate curEnd   = LocalDate.parse(sorted.get(0).endDate());

        for (int i = 1; i < sorted.size(); i++) {
            LocalDate s = LocalDate.parse(sorted.get(i).startDate());
            LocalDate e = LocalDate.parse(sorted.get(i).endDate());

            // overlap ή ακριβώς συνεχόμενα (curEnd.plusDays(1).isAfterOrEquals(s))
            if (!s.isAfter(curEnd.plusDays(1))) {
                if (e.isAfter(curEnd)) curEnd = e;
            } else {
                merged.add(new DateRange(curStart.toString(), curEnd.toString()));
                curStart = s;
                curEnd   = e;
            }
        }
        merged.add(new DateRange(curStart.toString(), curEnd.toString()));
        return merged;
    }



//    @Transactional
//    public PropertyPhoto replacePhoto(Long photoId, MultipartFile file, User caller, boolean isAdmin) throws IOException {
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("No file provided");
//        }
//        // basic validation
//        long maxSize = 5L * 1024 * 1024; // 5MB
//        if (file.getSize() > maxSize) {
//            throw new IllegalArgumentException("File too large (max 5MB)");
//        }
//        String ct = file.getContentType();
//        if (ct == null || !(ct.startsWith("image/"))) {
//            throw new IllegalArgumentException("Only image/* content types are allowed");
//        }
//
//        PropertyPhoto photo = photoRepo.findById(photoId)
//                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));
//
//        // owner or admin check
//        Long ownerId = photo.getProperty().getOwner().getId();
//        if (!isAdmin && !ownerId.equals(caller.getId())) {
//            throw new SecurityException("Not allowed to modify this photo");
//        }
//
//        // update bytes + metadata
//        photo.setImage(file.getBytes());                 // byte[]/@Lob field
//        photo.setContentType(ct);
//        photo.setFilename(file.getOriginalFilename());
//
//        return photoRepo.save(photo);
//    }
}
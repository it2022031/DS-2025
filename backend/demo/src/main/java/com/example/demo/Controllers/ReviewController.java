package com.example.demo.Controllers;

import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Entities.Rental;
import com.example.demo.Entities.Review;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Services.ReviewService;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.ReviewDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    public ReviewController(ReviewService reviewService, UserRepository userRepository, RentalRepository rentalRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
    }

    // GET: reviews για συγκεκριμένο property
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviewsForProperty(@PathVariable Long id) {
        List<ReviewDto> dto = reviewService.getReviewsForProperty(id)
                .stream()
                .map(ReviewDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dto);
    }

    // POST: add review (must be logged in)
    // POST: add review (must be logged in & rental completed)
    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addReviewForProperty(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> body,
                                                  Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "You must be logged in"));
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // --- βρες rentals για αυτό το property και τον συγκεκριμένο χρήστη
        List<Rental> rentals = rentalRepository.findByPropertyIdAndUserId(id, user.getId());

        LocalDate now = LocalDate.now();

        boolean hasValidRental = rentals.stream().anyMatch(r ->
                r.getApprovalStatus() == ApprovalStatus.APPROVED &&
                        r.getEndDate().isBefore(now)   // έχει λήξει
        );

        if (!hasValidRental) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You can only leave a review after completing an approved rental"));
        }

        // --- προχώρα με το review
        String content = (String) body.get("content");
        int rating = (int) body.getOrDefault("rating", 0);

        Review saved = reviewService.addReview(id, user.getId(), content, rating);
        return ResponseEntity.ok(saved);
    }


    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @RequestBody Map<String, Object> body,
                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String content = (String) body.get("content");
        Integer rating = body.get("rating") != null
                ? ((Number) body.get("rating")).intValue()
                : null;

        if ((content == null || content.isBlank()) && rating == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No fields to update"));
        }

        try {
            Review updated = reviewService.updateReview(reviewId, user.getId(), content, rating);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }


    // DELETE: delete review (must be logged in and owner)
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                          Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            reviewService.deleteReview(reviewId, user.getId());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
    // GET: όλα τα reviews ενός χρήστη
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewDto> dto = reviewService.getReviewsByUser(userId)
                .stream()
                .map(ReviewDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dto);
    }
}

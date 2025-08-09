package com.example.demo.Controllers;

import com.example.demo.Entities.Review;
import com.example.demo.Entities.User;
import com.example.demo.Services.ReviewService;
import com.example.demo.Repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    // GET: reviews for a property
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Review>> getReviewsForProperty(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsForProperty(id));
    }

    // POST: add review (must be logged in)
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Review> addReviewForProperty(@PathVariable Long id,
                                                       @RequestBody Map<String, Object> body,
                                                       Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
}

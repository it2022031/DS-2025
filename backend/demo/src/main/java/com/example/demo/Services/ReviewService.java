package com.example.demo.Services;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Repositories.ReviewRepository;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.dto.ReviewDto;
//import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    public ReviewService(ReviewRepository reviewRepository, PropertyRepository propertyRepository, UserRepository userRepository, RentalRepository rentalRepository) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Review> getReviewsForProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        return reviewRepository.findByProperty(property);
    }

    public Review addReview(Long propertyId, Long userId, String content, int rating) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Review review = new Review();
        review.setContent(content);
        review.setRating(rating);
        review.setProperty(property);
        review.setUser(user);

        return reviewRepository.save(review);
    }

    public Review addReview(Long propertyId, Long userId, String content, int rating, Long rentalId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be 1–5");
        }

        Property p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Review r = new Review();
        r.setContent(content);
        r.setRating(rating);
        r.setProperty(p);
        r.setUser(u);

        LocalDate today = LocalDate.now();


        if (rentalId != null) {
            // 1) Αν ήρθε rentalId, κάνε αυστηρούς ελέγχους
            Rental rent = rentalRepository.findById(rentalId)
                    .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

            if (!rent.getUser().getId().equals(userId)) {
                throw new SecurityException("Rental does not belong to this user");
            }
            if (!rent.getProperty().getId().equals(propertyId)) {
                throw new IllegalArgumentException("Rental is not for this property");
            }
            if (rent.getApprovalStatus() != ApprovalStatus.APPROVED) {
                throw new IllegalStateException("Rental is not APPROVED");
            }
            if (!rent.getEndDate().isBefore(today)) {
                throw new IllegalStateException("Rental has not finished yet");
            }
            r.setRental(rent);
        } else {
            // 2) Αλλιώς, βρες το πιο πρόσφατο ολοκληρωμένο APPROVED rental και σύνδεσέ το (αν υπάρχει)
            // (Χωρίς νέο repo method: φιλτράρουμε τη λίστα rentals του χρήστη-ακινήτου)
            List<Rental> userPropRentals = rentalRepository.findByPropertyIdAndUserId(propertyId, userId);
            userPropRentals.stream()
                    .filter(rx -> rx.getApprovalStatus() == ApprovalStatus.APPROVED && rx.getEndDate().isBefore(today))
                    .max(Comparator.comparing(Rental::getEndDate))
                    .ifPresent(r::setRental);
            // Αν δεν βρεθεί, το αφήνουμε null (μια χαρά, δεν σπάει τα παλιά)
        }

        return reviewRepository.save(r);
    }


    @Transactional
    public Review updateReview(Long reviewId, Long userId, String newContent, int newRating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not allowed to edit this review");
        }

        review.setContent(newContent);
        review.setRating(newRating);
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
    }

    @Transactional
    public void adminDeleteReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found"));
        reviewRepository.delete(r);
    }

    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(ReviewDto::fromEntity)
                .toList();
    }
}

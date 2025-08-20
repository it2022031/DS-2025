package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Review;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.ReviewRepository;
import com.example.demo.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
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


    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}

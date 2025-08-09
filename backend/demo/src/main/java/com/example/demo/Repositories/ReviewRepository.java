package com.example.demo.Repositories;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProperty(Property property);
}
package com.example.demo.Repositories;

import com.example.demo.Entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    // Προαιρετικά πρόσθεσε custom query methods αν χρειάζεται
}

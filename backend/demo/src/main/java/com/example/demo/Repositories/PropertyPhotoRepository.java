package com.example.demo.Repositories;

import com.example.demo.Entities.PropertyPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {
    List<PropertyPhoto> findByPropertyId(Long propertyId);
    long countByPropertyId(Long propertyId);

}

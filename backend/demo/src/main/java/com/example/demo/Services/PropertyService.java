package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Repositories.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Optional<Property> findById(Long id) {
        return propertyRepository.findById(id);
    }

    public Property save(Property property) {
        return propertyRepository.save(property);
    }

    public void deleteById(Long id) {
        propertyRepository.deleteById(id);
    }
}
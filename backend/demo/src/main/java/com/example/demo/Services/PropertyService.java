package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Repositories.PropertyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }

    public Property findById(Long id) {
        return propertyRepository.findById(id).orElse(null);
    }

    public Property addProperty(Property property) {
        return propertyRepository.save(property);
    }
}

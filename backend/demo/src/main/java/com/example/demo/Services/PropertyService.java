package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.User;
import com.example.demo.Entities.ApprovalStatus;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public PropertyService(PropertyRepository propertyRepository,
                           UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
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
}
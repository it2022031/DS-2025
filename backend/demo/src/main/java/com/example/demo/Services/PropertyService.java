package com.example.demo.Services;

import com.example.demo.Entities.Property;
import com.example.demo.Repositories.PropertyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import com.example.demo.Entities.Property;
import com.example.demo.Entities.User;
import com.example.demo.Repositories.PropertyRepository;
import com.example.demo.Repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Property findById(Long id) {
        return propertyRepository.findById(id).orElse(null);
    }

    public Property addProperty(Property property) {
        return propertyRepository.save(property);
    }

    @Transactional
    public Property createWithOwner(Long ownerId, Property property) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id " + ownerId));

        // Συνδέουμε δύο πλευρές σωστά
        owner.addProperty(property); // αυτό κάνει και property.setOwner(owner)

        return propertyRepository.save(property);
    }

}

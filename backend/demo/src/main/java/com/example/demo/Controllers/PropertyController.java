package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Services.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    // GET όλα τα ακίνητα
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyService.findAll();
    }

    // GET ακίνητο με id
    @GetMapping("/{id}")
    public ResponseEntity<Property> getPropertyById(@PathVariable Long id) {
        Optional<Property> property = propertyService.findById(id);
        if (property.isPresent()) {
            return ResponseEntity.ok(property.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST νέο ακίνητο
    @PostMapping
    public Property createProperty(@RequestBody Property property) {
        return propertyService.save(property);
    }

//    // PUT ενημέρωση ακίνητου
//    @PutMapping("/{id}")
//    public ResponseEntity<Property> updateProperty(@PathVariable Long id, @RequestBody Property updatedProperty) {
//        Optional<Property> existingProperty = propertyService.findById(id);
//        if (existingProperty.isPresent()) {
//            Property property = existingProperty.get();
//            // Ενημέρωσε εδώ τα πεδία που θες να επιτρέπεις να αλλάζουν
//            property.setTitle(updatedProperty.getTitle());
//            property.setDescription(updatedProperty.getDescription());
//            // ...άλλα πεδία
//            Property savedProperty = propertyService.save(property);
//            return ResponseEntity.ok(savedProperty);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    // DELETE ακίνητο
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        Optional<Property> existingProperty = propertyService.findById(id);
        if (existingProperty.isPresent()) {
            propertyService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

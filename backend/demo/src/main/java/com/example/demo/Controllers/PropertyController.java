package com.example.demo.Controllers;

import com.example.demo.Entities.Property;
import com.example.demo.Services.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/all")
    public List<Property> getAllProperties() {
        return propertyService.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Property> addProperty(@RequestBody Property dto) {
        Property newProperty = new Property();
        newProperty.setName(dto.getName());
        newProperty.setDescription(dto.getDescription());
        newProperty.setCountry(dto.getCountry());
        newProperty.setCity(dto.getCity());
        newProperty.setStreet(dto.getStreet());
        newProperty.setPostalCode(dto.getPostalCode());
        newProperty.setSquareMeters(dto.getSquareMeters());
        newProperty.setStatus(dto.getStatus());

        Property saved = propertyService.addProperty(newProperty);
        return ResponseEntity.ok(saved);
    }
}


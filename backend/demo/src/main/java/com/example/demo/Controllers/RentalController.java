package com.example.demo.Controllers;

import com.example.demo.Entities.Rental;
import com.example.demo.Services.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/all")
    public List<Rental> getAllRentals() {
        return rentalService.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Rental> addRental(@RequestBody Rental dto) {
        Rental saved = rentalService.addRental(dto);
        return ResponseEntity.ok(saved);
    }
}

package com.example.demo.Controllers;

import com.example.demo.Entities.Rental;
import com.example.demo.Services.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.List;


import java.util.List;
@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

//    @GetMapping("/all")
//    public List<Rental> getAllRentals() {
//        return rentalService.findAll();
//    }
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllRentals() {
        List<Map<String, Object>> rentals = rentalService.getAllRentalData();
        return ResponseEntity.ok(rentals);
    }

//    @PostMapping("/add")
//    public ResponseEntity<Rental> addRental(@RequestBody Rental dto) {
//        Rental saved = rentalService.addRental(dto);
//        return ResponseEntity.ok(saved);
//    }
    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.getRentalWithRelations(id);
        return ResponseEntity.ok(rental);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createRental(@RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("propertyId") || !body.containsKey("userId") ||
                    !body.containsKey("startDate") || !body.containsKey("endDate") ||
                    !body.containsKey("paymentAmount")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Long propertyId = Long.valueOf(body.get("propertyId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());
            LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(body.get("endDate").toString());
            Double paymentAmount = Double.valueOf(body.get("paymentAmount").toString());

            Rental created = rentalService.createRental(propertyId, userId, startDate, endDate, paymentAmount);
            return ResponseEntity.ok(RentalDto.fromEntity(created));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date format. Use YYYY-MM-DD."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create rental"));
        }
    }



    // RentalDto.java (μπορείς και ως record αν θες)
    public record RentalDto(
            Long id,
            String startDate,
            String endDate,
            Double paymentAmount,
            Boolean status,
            Long propertyId,
            Long userId
    ) {
        public static RentalDto fromEntity(Rental r) {
            return new RentalDto(
                    r.getId(),
                    r.getStartDate().toString(),
                    r.getEndDate().toString(),
                    r.getPaymentAmount(),
                    r.getStatus(),
                    r.getProperty().getId(),
                    r.getUser().getId()
            );
        }
    }

}


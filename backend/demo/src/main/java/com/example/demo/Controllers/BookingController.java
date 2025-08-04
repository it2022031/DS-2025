package com.example.demo.Controllers;

import com.example.demo.Entities.Booking;
import com.example.demo.Entities.Property;
import com.example.demo.Services.BookingService;
import com.example.demo.Services.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PropertyService propertyService;

    public BookingController(BookingService bookingService, PropertyService propertyService) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addBooking(@RequestBody Booking dto) {
        if (dto.getProperty() == null || dto.getProperty().getId() == null) {
            return ResponseEntity.badRequest().body("Property ID is required");
        }
        Long propId = dto.getProperty().getId();

        Property property = propertyService.findByIdOptional(propId)
                .orElseThrow(() -> new RuntimeException("Property not found with id " + propId));

        Booking newBooking = new Booking();
        newBooking.setBookedFrom(dto.getBookedFrom());
        newBooking.setBookedTo(dto.getBookedTo());
        newBooking.setStatus(dto.getStatus());
        newBooking.setProperty(property);

        Booking saved = bookingService.addBooking(newBooking);
        return ResponseEntity.ok(saved);
    }
}

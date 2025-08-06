package com.example.demo.Controllers;

import com.example.demo.Entities.Booking;
import com.example.demo.Entities.Property;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Services.BookingService;
import com.example.demo.Services.PropertyService;
//import org.apache.catalina.User;
import com.example.demo.Entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PropertyService propertyService;
   // private final UserRepository userRepository;

    public BookingController(BookingService bookingService, PropertyService propertyService) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
    }

//    public BookingController(BookingService bookingService, PropertyService propertyService, UserRepository userRepository) {
//        this.bookingService = bookingService;
//        this.propertyService = propertyService;
//        this.userRepository = userRepository;
//    }


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

//    @PostMapping
//    public ResponseEntity<?> addBooking(@RequestBody Booking dto, Authentication authentication) {
//        if (dto.getProperty() == null || dto.getProperty().getId() == null) {
//            return ResponseEntity.badRequest().body("Property ID is required");
//        }
//
//        Long propId = dto.getProperty().getId();
//
//        Property property = propertyService.findByIdOptional(propId)
//                .orElseThrow(() -> new RuntimeException("Property not found with id " + propId));
//
//        // 👉 πάρε τον authenticated user από το token
//        String username = authentication.getName();
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Booking newBooking = new Booking();
//        newBooking.setBookedFrom(dto.getBookedFrom());
//        newBooking.setBookedTo(dto.getBookedTo());
//        newBooking.setStatus(dto.getStatus());
//        newBooking.setProperty(property);
//        newBooking.setUser(user); // ✅ Συσχετίζει την κράτηση με τον χρήστη
//
//        Booking saved = bookingService.addBooking(newBooking);
//        return ResponseEntity.ok(saved);
//    }

}

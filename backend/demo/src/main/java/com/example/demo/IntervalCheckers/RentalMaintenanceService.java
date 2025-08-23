package com.example.demo.IntervalCheckers;

import com.example.demo.Repositories.RentalRepository;
import com.example.demo.Services.RentalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
//RentalMaintenanceService
@Service
@RequiredArgsConstructor
@Slf4j
public class RentalMaintenanceService {

    private final RentalService rentalService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Athens")
    public void nightlyReject() {
        int n = rentalService.rejectExpiredPendingRentalsToday();
        if (n > 0) log.info("Auto-rejected {} pending rentals", n);
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onStartup() {
        nightlyReject();
    }
}

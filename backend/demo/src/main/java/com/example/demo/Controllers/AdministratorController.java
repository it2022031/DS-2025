package com.example.demo.Controllers;


import com.example.demo.Entities.Administrator;
import com.example.demo.Services.AdministratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdministratorController {

    private final AdministratorService service;

    public AdministratorController(AdministratorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Administrator> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Administrator getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Administrator> create(@RequestBody Administrator admin) {
        Administrator saved = service.create(admin);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public Administrator update(
            @PathVariable Long id,
            @RequestBody Administrator admin
    ) {
        return service.update(id, admin);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
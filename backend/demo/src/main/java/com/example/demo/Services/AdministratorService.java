package com.example.demo.Services;

import com.example.demo.Entities.Administrator;
import com.example.demo.Repositories.AdministratorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService {

    private final AdministratorRepository repo;

    public AdministratorService(AdministratorRepository repo) {
        this.repo = repo;
    }

    public List<Administrator> findAll() {
        return repo.findAll();
    }

    public Administrator findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id " + id));
    }

    public Administrator create(Administrator admin) {
        return repo.save(admin);
    }

    public Administrator update(Long id, Administrator dto) {
        Administrator existing = findById(id);
        existing.setUsername(dto.getUsername());
        existing.setPassword(dto.getPassword());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
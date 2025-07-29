package com.example.demo.Repositories;

import com.example.demo.Entities.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    // αν χρειαστείς custom queries, εδώ
}
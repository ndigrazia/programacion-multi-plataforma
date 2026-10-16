package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.StudentUser;

@Repository
public interface StudentUserRepository extends JpaRepository<StudentUser, String> {

    Optional<StudentUser> findByEmail(String email);

    boolean existsByEmail(String email);
}

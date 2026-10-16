package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.InstructorUser;
import com.example.demo.entity.Skill;

@Repository
public interface InstructorUserRepository extends JpaRepository<InstructorUser, String> {

    Optional<InstructorUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<InstructorUser> findBySkillsContaining(Skill skill);
}

package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "users")
@Getter 
@Setter 
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {

    @Id 
    private String email;
    private String name;
    private String surname;
    private String password;

    public abstract void createCourse(String name, Skill skill, int studentMaxQuantity, 
        String description, LocalDate startDate, LocalDate endDate, BigDecimal fee);
}

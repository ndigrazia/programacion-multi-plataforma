package com.example.demo.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;

@Entity 
@Getter 
@Setter 
@DiscriminatorValue("Admin")
public class AdminUser extends User {

    @Override
    public void createCourse(String name, Skill skill, int studentMaxQuantity, String description, LocalDate startDate,
            LocalDate endDate, BigDecimal fee) {
        throw new UnsupportedOperationException("Unimplemented method 'createCourse'");
    }

}
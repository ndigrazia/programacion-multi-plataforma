package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Getter 
@Setter
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private CourseStatus status;
    @ManyToOne 
    private Skill skill;
    private int studentMaxQuantity;
    private String description;
    @Column(name = "startDate", columnDefinition = "DATE")
    private LocalDate startDate;
    @Column(name = "endDate", columnDefinition = "DATE")
    private LocalDate endDate;
    @Column(precision = 20, scale = 2)
    private BigDecimal fee;
    private int quantity;
    @ManyToOne
    private InstructorUser createdBy;
        
    public boolean isFull() {
        return !(quantity < studentMaxQuantity);
    }

    public void increaseQuantity(){
        quantity++;
    }

    public boolean isOpen() {
        LocalDate today = LocalDateTime.now().toLocalDate();
        return today.isAfter(startDate) &&
            today.isBefore(endDate);
    }
}


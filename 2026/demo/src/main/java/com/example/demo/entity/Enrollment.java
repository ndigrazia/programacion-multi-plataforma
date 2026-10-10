package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private StudentUser student;

    @ManyToOne
    private Course course;

    @Column(name = "date", columnDefinition = "DATE")
    private LocalDate date;

    @Column(precision = 20, scale = 2)
    private BigDecimal price;

    public Enrollment(StudentUser student, Course course, 
        LocalDate date, BigDecimal price) {
        this.student = student;
        this.course = course;
        this.date = date;
        this.price = price;
    }

    public boolean isSameMonthAndSameSkill(Month m, Skill s) {
        if (m == null || s == null || date == null || course == null || course.getSkill() == null) 
            return false;
        return date.getMonth().equals(m)
            && course.getSkill().equals(s);
    }

    public boolean isSameMonthAndSameSkill(LocalDate d, Skill s) {
        return isSameMonthAndSameSkill(d.getMonth(), s);
    }

}

package com.example.demo.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.example.demo.exception.ClosedCourseException;
import com.example.demo.exception.FullCourseException;
import com.example.demo.exception.NoPublishStatusException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.DiscriminatorValue;

import lombok.Getter;
import lombok.Setter;

@Entity 
@Getter 
@Setter 
@DiscriminatorValue("Student")
public class StudentUser extends User {

    private static final BigDecimal CIEN_BIG_DECIMAL = new BigDecimal("100.00");

    @Value("${student.discount:25}")
    private static BigDecimal discount;

    public static void setDiscount(BigDecimal discount) {
        StudentUser.discount = discount;
    }

    public static BigDecimal getDiscount() {
        return discount;
    }
 
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private Set<Enrollment> enrollmens = new LinkedHashSet<Enrollment>();
    
    public void applyCourse(Course course, LocalDate date) throws NoPublishStatusException, 
        FullCourseException, ClosedCourseException {
        if(!course.getStatus().equals(CourseStatus.PUBLISHED))
            throw new NoPublishStatusException();
        if(course.isFull())
            throw new FullCourseException();
        if(!course.isOpen())
            throw new ClosedCourseException();

        BigDecimal payment = course.getFee();
        if(hasDiscount(course, date)) {
            BigDecimal discountAmount = payment.multiply(discount)
                .divide(CIEN_BIG_DECIMAL, 2, RoundingMode.HALF_UP);
            payment = payment.subtract(discountAmount);
        }
        
        enrollmens.add(new Enrollment(this, course, date, payment));
        course.increaseQuantity();
    }

    public Set<Enrollment> getEnrollments() {
        return Collections.unmodifiableSet(enrollmens);
    }   

    private boolean hasDiscount(Course course, LocalDate date) {
        if (course == null || date == null)
            return false;
        
        for (Enrollment e : enrollmens) {
            if (e.isSameMonthAndSameSkill(date.getMonth(), course.getSkill()))
                   return true; 
        }
        return false;
    }

    @Override
    public void createCourse(String name, Skill skill, int studentMaxQuantity, String description, LocalDate startDate,
            LocalDate endDate, BigDecimal fee) {
        throw new UnsupportedOperationException("Unimplemented method 'createCourse'");
    }

}


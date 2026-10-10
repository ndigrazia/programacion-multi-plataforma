package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.example.demo.exception.NoDraftCourseException;
import com.example.demo.exception.NoOwnerCourseException;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;

@Entity 
@Getter 
@Setter 
@DiscriminatorValue("Instructor")
public class InstructorUser extends User {

    @ManyToMany
    private Set<Skill> skills = new  LinkedHashSet<Skill>();

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL) 
    private Set<Course> courses = new LinkedHashSet<Course>();

    @OneToMany
    private Set<ApprovalRequest> approvalRequests = new LinkedHashSet<ApprovalRequest>();

    public void addSkill(Skill s) {
        skills.add(s);
    }

    public void removeSkill(Skill s) {
        skills.remove(s);
    }

    public boolean hasASkill(Skill s) {
        return skills.contains(s);
    }

    public boolean hasSkills(Set<Skill> skills) {
        if (skills == null) {
            return false;
        }
        return this.skills.containsAll(skills);
    }
    
    public Set<ApprovalRequest> getApprovalRequests() {
        return Collections.unmodifiableSet(approvalRequests);
    }

    public Set<Course> getCourses() {
        return Collections.unmodifiableSet(courses);
    }

    public Set<Skill> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    private void addCourse(Course c) {
        courses.add(c);
    }

    public void removeCourse(Course c) {
        courses.remove(c);
    }

    @Override
    public void createCourse(String name, Skill skill, int studentMaxQuantity, String description, LocalDate startDate,
            LocalDate endDate, BigDecimal fee) {
        Course course = Course.builder()
                .name(name)
                .status(CourseStatus.DRAFT)
                .skill(skill)
                .studentMaxQuantity(studentMaxQuantity)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .fee(fee)
                .createdBy(this)
                .build();
        addCourse(course);
    }

    private void addApprovalRequest(ApprovalRequest ar) {
        approvalRequests.add(ar);
    }

    public void requestApproval(Course c, String comment) throws NoOwnerCourseException, NoDraftCourseException {
        if(!c.getCreatedBy().equals(this))
            throw new NoOwnerCourseException();
        if(!c.getStatus().equals(CourseStatus.DRAFT))
            throw new NoDraftCourseException();
        
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
            .comment(comment)
            .status(ApprovalRequestStatus.SUBMITTED)
            .requestDate(LocalDateTime.now().toLocalDate())
            .course(c)
            .build();

        c.setStatus(CourseStatus.PENDING);
        
        addApprovalRequest(approvalRequest);
    }
    
}


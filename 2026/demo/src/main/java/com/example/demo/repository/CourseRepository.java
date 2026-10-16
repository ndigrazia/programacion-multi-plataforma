package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.CourseStatus;
import com.example.demo.entity.InstructorUser;
import com.example.demo.entity.Skill;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatus(CourseStatus status);

    List<Course> findBySkill(Skill skill);

    List<Course> findBySkillId(Long skillId);

    List<Course> findByCreatedBy(InstructorUser createdBy);

    List<Course> findByCreatedByEmail(String createdByEmail);
}

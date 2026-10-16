package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.StudentUser;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(StudentUser student);

    List<Enrollment> findByStudentEmail(String studentEmail);

    List<Enrollment> findByCourse(Course course);

    List<Enrollment> findByCourseId(Long courseId);
}

package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ApprovalRequest;
import com.example.demo.entity.ApprovalRequestStatus;
import com.example.demo.entity.Course;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByStatus(ApprovalRequestStatus status);

    List<ApprovalRequest> findByCourse(Course course);

    List<ApprovalRequest> findByCourseId(Long courseId);
}

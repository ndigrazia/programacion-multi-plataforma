package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.demo.entity.AdminUser;
import com.example.demo.entity.ApprovalRequest;
import com.example.demo.entity.ApprovalRequestStatus;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseStatus;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.InstructorUser;
import com.example.demo.entity.Skill;
import com.example.demo.entity.StudentUser;
import com.example.demo.entity.User;

@DataJpaTest
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private InstructorUserRepository instructorUserRepository;

    @Autowired
    private StudentUserRepository studentUserRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Test
    @DisplayName("Should save and find AdminUser through UserRepository and AdminUserRepository")
    void shouldPersistAndFindAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setEmail("admin@example.com");
        admin.setName("Admin");
        admin.setSurname("Root");
        admin.setPassword("secure123");

        adminUserRepository.save(admin);

        Optional<AdminUser> foundAdmin = adminUserRepository.findByEmail("admin@example.com");
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getName()).isEqualTo("Admin");

        Optional<User> foundUser = userRepository.findById("admin@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isInstanceOf(AdminUser.class);
    }

    @Test
    @DisplayName("Should save and find StudentUser through StudentUserRepository")
    void shouldPersistAndFindStudentUser() {
        StudentUser student = new StudentUser();
        student.setEmail("student@example.com");
        student.setName("Alice");
        student.setSurname("Smith");
        student.setPassword("secret");

        studentUserRepository.save(student);

        assertThat(studentUserRepository.existsByEmail("student@example.com")).isTrue();
        Optional<StudentUser> found = studentUserRepository.findByEmail("student@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should save and find InstructorUser and query by skill")
    void shouldPersistAndFindInstructorUser() {
        Skill javaSkill = new Skill();
        javaSkill.setName("Java");
        skillRepository.save(javaSkill);

        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("instructor@example.com");
        instructor.setName("Bob");
        instructor.setSurname("Builder");
        instructor.setPassword("secret");
        instructor.addSkill(javaSkill);

        instructorUserRepository.save(instructor);

        List<InstructorUser> instructors = instructorUserRepository.findBySkillsContaining(javaSkill);
        assertThat(instructors).hasSize(1);
        assertThat(instructors.get(0).getEmail()).isEqualTo("instructor@example.com");
    }

    @Test
    @DisplayName("Should save and find Course with custom finder methods")
    void shouldPersistAndFindCourse() {
        Skill skill = new Skill();
        skill.setName("Spring Boot");
        skillRepository.save(skill);

        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("course_inst@example.com");
        instructor.setName("Jane");
        instructor.setSurname("Doe");
        instructor.setPassword("secret");
        instructorUserRepository.save(instructor);

        Course course = Course.builder()
                .name("Spring Boot 3 Masterclass")
                .status(CourseStatus.PUBLISHED)
                .skill(skill)
                .studentMaxQuantity(30)
                .description("In-depth Spring Boot course")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(30))
                .fee(new BigDecimal("199.99"))
                .createdBy(instructor)
                .build();

        courseRepository.save(course);

        List<Course> publishedCourses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        assertThat(publishedCourses).isNotEmpty();

        List<Course> bySkill = courseRepository.findBySkill(skill);
        assertThat(bySkill).isNotEmpty();

        List<Course> bySkillId = courseRepository.findBySkillId(skill.getId());
        assertThat(bySkillId).isNotEmpty();

        List<Course> byInstructor = courseRepository.findByCreatedBy(instructor);
        assertThat(byInstructor).isNotEmpty();

        List<Course> byInstructorEmail = courseRepository.findByCreatedByEmail("course_inst@example.com");
        assertThat(byInstructorEmail).isNotEmpty();
    }

    @Test
    @DisplayName("Should save and find Enrollment through EnrollmentRepository")
    void shouldPersistAndFindEnrollment() {
        StudentUser student = new StudentUser();
        student.setEmail("enrolled_student@example.com");
        student.setName("Charlie");
        student.setSurname("Brown");
        student.setPassword("pwd");
        studentUserRepository.save(student);

        Course course = Course.builder()
                .name("Intro to Algorithms")
                .status(CourseStatus.PUBLISHED)
                .studentMaxQuantity(50)
                .description("Algorithms course")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .fee(new BigDecimal("50.00"))
                .build();
        courseRepository.save(course);

        Enrollment enrollment = new Enrollment(student, course, LocalDate.now(), new BigDecimal("50.00"));
        enrollmentRepository.save(enrollment);

        List<Enrollment> byStudent = enrollmentRepository.findByStudent(student);
        assertThat(byStudent).hasSize(1);

        List<Enrollment> byStudentEmail = enrollmentRepository.findByStudentEmail("enrolled_student@example.com");
        assertThat(byStudentEmail).hasSize(1);

        List<Enrollment> byCourse = enrollmentRepository.findByCourse(course);
        assertThat(byCourse).hasSize(1);

        List<Enrollment> byCourseId = enrollmentRepository.findByCourseId(course.getId());
        assertThat(byCourseId).hasSize(1);
    }

    @Test
    @DisplayName("Should save and find ApprovalRequest through ApprovalRequestRepository")
    void shouldPersistAndFindApprovalRequest() {
        Course course = Course.builder()
                .name("Data Structures")
                .status(CourseStatus.PENDING)
                .studentMaxQuantity(25)
                .description("Data structures basics")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(60))
                .fee(new BigDecimal("75.00"))
                .build();
        courseRepository.save(course);

        ApprovalRequest request = ApprovalRequest.builder()
                .course(course)
                .requestDate(LocalDate.now())
                .status(ApprovalRequestStatus.SUBMITTED)
                .comment("Please approve course")
                .build();
        approvalRequestRepository.save(request);

        List<ApprovalRequest> submitted = approvalRequestRepository.findByStatus(ApprovalRequestStatus.SUBMITTED);
        assertThat(submitted).isNotEmpty();

        List<ApprovalRequest> byCourse = approvalRequestRepository.findByCourse(course);
        assertThat(byCourse).isNotEmpty();

        List<ApprovalRequest> byCourseId = approvalRequestRepository.findByCourseId(course.getId());
        assertThat(byCourseId).isNotEmpty();
    }
}

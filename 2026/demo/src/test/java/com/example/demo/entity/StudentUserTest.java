package com.example.demo.entity;

import com.example.demo.exception.ClosedCourseException;
import com.example.demo.exception.FullCourseException;
import com.example.demo.exception.NoPublishStatusException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class StudentUserTest {

    @Autowired
    private TestEntityManager entityManager;

    @Value("${student.discount}")
    private BigDecimal studentDiscount;

    @BeforeEach
    void setUp() {
        StudentUser.setDiscount(studentDiscount);
    }

    @Test
    @DisplayName("Should instantiate StudentUser and set inherited and own fields including applyDate")
    void shouldSetInheritedProperties() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@example.com");
        student.setName("John");
        student.setSurname("Doe");
        student.setPassword("student123");

        Course course1 = new Course();
        course1.setId(101L);
        course1.setName("Advanced Java");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(10));
        course1.setEndDate(LocalDate.now().plusDays(10));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setId(102L);
        course2.setName("Spring Boot");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(10));
        course2.setEndDate(LocalDate.now().plusDays(10));
        course2.setFee(new BigDecimal("100.00"));

        LocalDate date1 = LocalDate.of(2026, 10, 1);
        LocalDate date2 = LocalDate.of(2026, 10, 2);

        student.applyCourse(course1, date1);
        student.applyCourse(course2, date2);

        assertThat(student.getEmail()).isEqualTo("student@example.com");
        assertThat(student.getName()).isEqualTo("John");
        assertThat(student.getSurname()).isEqualTo("Doe");
        assertThat(student.getPassword()).isEqualTo("student123");

        Set<Enrollment> applications = student.getEnrollments();
        assertThat(applications).hasSize(2);
        assertThat(applications).extracting(Enrollment::getCourse).containsExactlyInAnyOrder(course1, course2);
        assertThat(applications).extracting(Enrollment::getDate).containsExactlyInAnyOrder(date1, date2);
    }

    @Test
    @DisplayName("Should apply to course using applyCourse helper method")
    void shouldApplyCourseUsingHelperMethod() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("helper_student@example.com");

        Course course = new Course();
        course.setId(99L);
        course.setName("Cloud Native Java");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(10));
        course.setEndDate(LocalDate.now().plusDays(10));
        course.setFee(new BigDecimal("100.00"));

        LocalDate applyDate = LocalDate.of(2026, 10, 3);
        student.applyCourse(course, applyDate);

        assertThat(student.getEnrollments()).hasSize(1);
        Enrollment application = student.getEnrollments().iterator().next();
        assertThat(application.getStudent()).isEqualTo(student);
        assertThat(application.getCourse()).isEqualTo(course);
        assertThat(application.getDate()).isEqualTo(applyDate);
    }

    @Test
    @DisplayName("Should evaluate how course applyQuantity is increased when student applies")
    void shouldEvaluateHowCourseApplyQuantityIsIncreased() throws Exception {
        Course course1 = new Course();
        course1.setName("Data Structures");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(10));
        course1.setEndDate(LocalDate.now().plusDays(10));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setName("Web Development");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(10));
        course2.setEndDate(LocalDate.now().plusDays(10));
        course2.setFee(new BigDecimal("100.00"));

        // Initially quantity should be 0
        assertThat(course1.getQuantity()).isEqualTo(0);
        assertThat(course2.getQuantity()).isEqualTo(0);

        StudentUser student1 = new StudentUser();
        student1.setEmail("student1@example.com");

        StudentUser student2 = new StudentUser();
        student2.setEmail("student2@example.com");

        StudentUser student3 = new StudentUser();
        student3.setEmail("student3@example.com");

        // 1st application: course1 quantity increases from 0 to 1
        student1.applyCourse(course1, LocalDate.of(2026, 10, 1));
        assertThat(course1.getQuantity()).isEqualTo(1);

        // 2nd application to course1: course1 quantity increases from 1 to 2
        student2.applyCourse(course1, LocalDate.of(2026, 10, 2));
        assertThat(course1.getQuantity()).isEqualTo(2);

        // 3rd application to course1: course1 quantity increases from 2 to 3
        student3.applyCourse(course1, LocalDate.of(2026, 10, 3));
        assertThat(course1.getQuantity()).isEqualTo(3);

        // Application to course2: only course2 increases (from 0 to 1), course1 remains 3
        student1.applyCourse(course2, LocalDate.of(2026, 10, 4));
        assertThat(course2.getQuantity()).isEqualTo(1);
        assertThat(course1.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should reflect relationship where multiple students can apply to multiple courses with applyDates")
    void shouldReflectManyToManyRelationWithCourses() throws Exception {
        StudentUser student1 = new StudentUser();
        student1.setEmail("student1@example.com");

        StudentUser student2 = new StudentUser();
        student2.setEmail("student2@example.com");

        Course course1 = new Course();
        course1.setId(101L);
        course1.setName("Advanced Java");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(10));
        course1.setEndDate(LocalDate.now().plusDays(10));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setId(102L);
        course2.setName("Spring Boot");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(10));
        course2.setEndDate(LocalDate.now().plusDays(10));
        course2.setFee(new BigDecimal("100.00"));

        Course course3 = new Course();
        course3.setId(103L);
        course3.setName("Microservices");
        course3.setStatus(CourseStatus.PUBLISHED);
        course3.setStudentMaxQuantity(10);
        course3.setStartDate(LocalDate.now().minusDays(10));
        course3.setEndDate(LocalDate.now().plusDays(10));
        course3.setFee(new BigDecimal("100.00"));

        LocalDate date1 = LocalDate.of(2026, 10, 1);
        LocalDate date2 = LocalDate.of(2026, 10, 2);
        LocalDate date3 = LocalDate.of(2026, 10, 3);

        // Student 1 applies to course1 and course2
        student1.applyCourse(course1, date1);
        student1.applyCourse(course2, date2);

        // Student 2 applies to course1 and course3
        student2.applyCourse(course1, date2);
        student2.applyCourse(course3, date3);

        assertThat(student1.getEnrollments()).hasSize(2);
        assertThat(student1.getEnrollments()).extracting(Enrollment::getCourse).contains(course1, course2);
        assertThat(student2.getEnrollments()).hasSize(2);
        assertThat(student2.getEnrollments()).extracting(Enrollment::getCourse).contains(course1, course3);
    }

    @Test
    @DisplayName("Should return an unmodifiable set from getEnrollments")
    void shouldReturnUnmodifiableSet() throws Exception {
        StudentUser student = new StudentUser();
        Course course = new Course();
        course.setId(99L);
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(10));
        course.setEndDate(LocalDate.now().plusDays(10));
        course.setFee(new BigDecimal("100.00"));
        student.applyCourse(course, LocalDate.now());

        Set<Enrollment> courses = student.getEnrollments();
        Enrollment extra = new Enrollment();

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            courses.add(extra);
        });
    }

    @Test
    @DisplayName("Should have @OneToMany annotation on enrollmens field")
    void shouldHaveOneToManyAnnotationOnEnrollmens() throws NoSuchFieldException {
        Field field = StudentUser.class.getDeclaredField("enrollmens");
        assertThat(field.isAnnotationPresent(OneToMany.class)).isTrue();
    }

    @Test
    @DisplayName("Should have @DiscriminatorValue('Student') annotation")
    void shouldHaveDiscriminatorValueAnnotation() {
        DiscriminatorValue dv = StudentUser.class.getAnnotation(DiscriminatorValue.class);
        assertThat(dv).isNotNull();
        assertThat(dv.value()).isEqualTo("Student");
    }

    @Test
    @DisplayName("Should persist and retrieve StudentUser with courses")
    void shouldPersistAndRetrieveStudentUser() throws Exception {
        Course course1 = new Course();
        course1.setName("Algorithms");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(10));
        course1.setEndDate(LocalDate.now().plusDays(10));
        course1.setFee(new BigDecimal("100.00"));
        entityManager.persistAndFlush(course1);

        Course course2 = new Course();
        course2.setName("Data Structures");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(10));
        course2.setEndDate(LocalDate.now().plusDays(10));
        course2.setFee(new BigDecimal("100.00"));
        entityManager.persistAndFlush(course2);

        StudentUser student = new StudentUser();
        student.setEmail("student@example.com");
        student.setName("John");
        student.setSurname("Doe");
        student.setPassword("student123");

        LocalDate applyDate1 = LocalDate.of(2026, 9, 15);
        LocalDate applyDate2 = LocalDate.of(2026, 9, 20);

        student.applyCourse(course1, applyDate1);
        student.applyCourse(course2, applyDate2);

        assertThat(course1.getQuantity()).isEqualTo(1);
        assertThat(course2.getQuantity()).isEqualTo(1);

        entityManager.persistAndFlush(student);
        entityManager.clear();

        StudentUser found = entityManager.find(StudentUser.class, "student@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("student@example.com");
        assertThat(found.getName()).isEqualTo("John");
        assertThat(found.getSurname()).isEqualTo("Doe");
        assertThat(found.getPassword()).isEqualTo("student123");
        assertThat(found.getEnrollments()).hasSize(2);
        assertThat(found.getEnrollments())
            .extracting(ac -> ac.getCourse().getName())
            .containsExactlyInAnyOrder("Algorithms", "Data Structures");
        assertThat(found.getEnrollments())
            .extracting(Enrollment::getDate)
            .containsExactlyInAnyOrder(applyDate1, applyDate2);
    }

    @Test
    @DisplayName("Should update StudentUser details")
    void shouldUpdateStudentUser() {
        StudentUser student = new StudentUser();
        student.setEmail("student2@example.com");
        student.setName("Jane");
        student.setSurname("Doe");
        student.setPassword("pass1");

        entityManager.persistAndFlush(student);

        student.setName("Janet");
        student.setPassword("newPass");
        entityManager.persistAndFlush(student);

        StudentUser updated = entityManager.find(StudentUser.class, "student2@example.com");
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Janet");
        assertThat(updated.getPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("Should delete StudentUser by entity")
    void shouldDeleteStudentUser() {
        StudentUser student = new StudentUser();
        student.setEmail("student3@example.com");
        student.setName("Delete");
        student.setSurname("Me");
        student.setPassword("deletePass");

        entityManager.persistAndFlush(student);
        assertThat(entityManager.find(StudentUser.class, "student3@example.com")).isNotNull();

        entityManager.remove(student);
        entityManager.flush();

        StudentUser deleted = entityManager.find(StudentUser.class, "student3@example.com");
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("Should find StudentUser polymorphically through User base class")
    void shouldFindPolymorphicallyAsUser() {
        StudentUser student = new StudentUser();
        student.setEmail("polystudent@example.com");
        student.setName("Poly");
        student.setSurname("Student");
        student.setPassword("polyPass");

        entityManager.persistAndFlush(student);

        User found = entityManager.find(User.class, "polystudent@example.com");
        assertThat(found).isNotNull();
        assertThat(found).isInstanceOf(StudentUser.class);
        assertThat(found.getEmail()).isEqualTo("polystudent@example.com");
    }

    @Test
    @DisplayName("Should throw NoPublishStatusException when course is in DRAFT status")
    void shouldThrowNoPublishStatusExceptionWhenCourseIsDraft() {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Course course = new Course();
        course.setStatus(CourseStatus.DRAFT);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(5));
        course.setEndDate(LocalDate.now().plusDays(5));
        course.setFee(new BigDecimal("100.00"));

        assertThrows(NoPublishStatusException.class, () -> {
            student.applyCourse(course, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Should throw NoPublishStatusException when course is in PENDING status")
    void shouldThrowNoPublishStatusExceptionWhenCourseIsPending() {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Course course = new Course();
        course.setStatus(CourseStatus.PENDING);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(5));
        course.setEndDate(LocalDate.now().plusDays(5));
        course.setFee(new BigDecimal("100.00"));

        assertThrows(NoPublishStatusException.class, () -> {
            student.applyCourse(course, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Should throw FullCourseException when course has reached max student quantity")
    void shouldThrowFullCourseExceptionWhenCourseIsFull() {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Course course = new Course();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStudentMaxQuantity(2);
        course.setQuantity(2);
        course.setStartDate(LocalDate.now().minusDays(5));
        course.setEndDate(LocalDate.now().plusDays(5));
        course.setFee(new BigDecimal("100.00"));

        assertThrows(FullCourseException.class, () -> {
            student.applyCourse(course, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Should throw ClosedCourseException when today is before course start date")
    void shouldThrowClosedCourseExceptionWhenTodayIsBeforeStartDate() {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Course course = new Course();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().plusDays(5));
        course.setEndDate(LocalDate.now().plusDays(30));
        course.setFee(new BigDecimal("100.00"));

        assertThrows(ClosedCourseException.class, () -> {
            student.applyCourse(course, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Should throw ClosedCourseException when today is after course end date")
    void shouldThrowClosedCourseExceptionWhenTodayIsAfterEndDate() {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Course course = new Course();
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(30));
        course.setEndDate(LocalDate.now().minusDays(5));
        course.setFee(new BigDecimal("100.00"));

        assertThrows(ClosedCourseException.class, () -> {
            student.applyCourse(course, LocalDate.now());
        });
    }

    @Test
    @DisplayName("Should apply full fee when applying for the first course with no discount")
    void shouldApplyFullFeeWhenNoDiscountApplies() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");

        Course course = new Course();
        course.setName("Java Fundamentals");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setSkill(skill);
        course.setStudentMaxQuantity(10);
        course.setStartDate(LocalDate.now().minusDays(5));
        course.setEndDate(LocalDate.now().plusDays(5));
        course.setFee(new BigDecimal("100.00"));

        LocalDate applyDate = LocalDate.of(2026, 10, 1);
        student.applyCourse(course, applyDate);

        assertThat(student.getEnrollments()).hasSize(1);
        Enrollment enrollment = student.getEnrollments().iterator().next();
        assertThat(enrollment.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(enrollment.getCourse()).isEqualTo(course);
        assertThat(enrollment.getStudent()).isEqualTo(student);
        assertThat(enrollment.getDate()).isEqualTo(applyDate);
        assertThat(course.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should apply 25% discount when enrolling in second course with same skill in same month")
    void shouldApplyDiscountWhenEnrollingInSecondCourseWithSameSkillInSameMonth() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");

        Course course1 = new Course();
        course1.setId(10L);
        course1.setName("Java Basics");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setSkill(skill);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(5));
        course1.setEndDate(LocalDate.now().plusDays(5));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setId(20L);
        course2.setName("Advanced Java");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setSkill(skill);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(5));
        course2.setEndDate(LocalDate.now().plusDays(5));
        course2.setFee(new BigDecimal("200.00"));

        LocalDate date1 = LocalDate.of(2026, 10, 5);
        LocalDate date2 = LocalDate.of(2026, 10, 15);

        student.applyCourse(course1, date1);
        student.applyCourse(course2, date2);

        assertThat(student.getEnrollments()).hasSize(2);

        Enrollment enrollment1 = student.getEnrollments().stream()
                .filter(e -> e.getCourse().equals(course1))
                .findFirst().orElseThrow();
        assertThat(enrollment1.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));

        Enrollment enrollment2 = student.getEnrollments().stream()
                .filter(e -> e.getCourse().equals(course2))
                .findFirst().orElseThrow();
        assertThat(enrollment2.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should not apply discount when courses have same skill but different months")
    void shouldNotApplyDiscountWhenSameSkillDifferentMonths() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");

        Course course1 = new Course();
        course1.setId(10L);
        course1.setName("Java Basics");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setSkill(skill);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(5));
        course1.setEndDate(LocalDate.now().plusDays(5));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setId(20L);
        course2.setName("Advanced Java");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setSkill(skill);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(5));
        course2.setEndDate(LocalDate.now().plusDays(5));
        course2.setFee(new BigDecimal("200.00"));

        LocalDate date1 = LocalDate.of(2026, 10, 5);
        LocalDate date2 = LocalDate.of(2026, 11, 5);

        student.applyCourse(course1, date1);
        student.applyCourse(course2, date2);

        Enrollment enrollment2 = student.getEnrollments().stream()
                .filter(e -> e.getCourse().equals(course2))
                .findFirst().orElseThrow();
        assertThat(enrollment2.getPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should not apply discount when courses are in same month but have different skills")
    void shouldNotApplyDiscountWhenDifferentSkillsSameMonth() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Skill javaSkill = new Skill();
        javaSkill.setId(1L);
        javaSkill.setName("Java");

        Skill pythonSkill = new Skill();
        pythonSkill.setId(2L);
        pythonSkill.setName("Python");

        Course course1 = new Course();
        course1.setId(10L);
        course1.setName("Java Course");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setSkill(javaSkill);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(5));
        course1.setEndDate(LocalDate.now().plusDays(5));
        course1.setFee(new BigDecimal("100.00"));

        Course course2 = new Course();
        course2.setId(20L);
        course2.setName("Python Course");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setSkill(pythonSkill);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(5));
        course2.setEndDate(LocalDate.now().plusDays(5));
        course2.setFee(new BigDecimal("150.00"));

        LocalDate date1 = LocalDate.of(2026, 10, 5);
        LocalDate date2 = LocalDate.of(2026, 10, 15);

        student.applyCourse(course1, date1);
        student.applyCourse(course2, date2);

        Enrollment enrollment2 = student.getEnrollments().stream()
                .filter(e -> e.getCourse().equals(course2))
                .findFirst().orElseThrow();
        assertThat(enrollment2.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Should correctly round discount amount using RoundingMode.HALF_UP")
    void shouldCorrectlyRoundDiscountAmount() throws Exception {
        StudentUser student = new StudentUser();
        student.setEmail("student@test.com");

        Skill skill = new Skill();
        skill.setId(1L);
        skill.setName("Cloud");

        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Cloud 1");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setSkill(skill);
        course1.setStudentMaxQuantity(10);
        course1.setStartDate(LocalDate.now().minusDays(5));
        course1.setEndDate(LocalDate.now().plusDays(5));
        course1.setFee(new BigDecimal("50.00"));

        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Cloud 2");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setSkill(skill);
        course2.setStudentMaxQuantity(10);
        course2.setStartDate(LocalDate.now().minusDays(5));
        course2.setEndDate(LocalDate.now().plusDays(5));
        course2.setFee(new BigDecimal("33.33"));

        student.applyCourse(course1, LocalDate.of(2026, 10, 1));
        student.applyCourse(course2, LocalDate.of(2026, 10, 2));

        Enrollment enrollment2 = student.getEnrollments().stream()
                .filter(e -> e.getCourse().equals(course2))
                .findFirst().orElseThrow();
        assertThat(enrollment2.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
    }
}

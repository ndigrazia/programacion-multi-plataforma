package com.example.demo.entity;

import jakarta.persistence.ManyToOne;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should instantiate Course and set fields")
    void shouldInstantiateAndSetFields() {
        Skill skill = new Skill();
        skill.setId(100L);
        skill.setName("Backend Development");

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        Course course = new Course();
        course.setName("Spring Boot Fundamentals");
        course.setStatus(CourseStatus.DRAFT);
        course.setSkill(skill);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setFee(new BigDecimal("199.99"));

        assertThat(course.getName()).isEqualTo("Spring Boot Fundamentals");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getSkill()).isEqualTo(skill);
        assertThat(course.getStartDate()).isEqualTo(startDate);
        assertThat(course.getEndDate()).isEqualTo(endDate);
        assertThat(course.getFee()).isEqualTo(new BigDecimal("199.99"));
        assertThat(course.getStudentMaxQuantity()).isEqualTo(0);
        assertThat(course.getQuantity()).isEqualTo(0);

        course.increaseQuantity();
        assertThat(course.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should build Course with Lombok builder")
    void shouldBuildCourseWithBuilder() {
        Course course = Course.builder()
                .name("Builder Pattern Course")
                .status(CourseStatus.DRAFT)
                .studentMaxQuantity(30)
                .fee(new BigDecimal("99.99"))
                .build();

        assertThat(course.getName()).isEqualTo("Builder Pattern Course");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getStudentMaxQuantity()).isEqualTo(30);
        assertThat(course.getFee()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should persist and retrieve Course with Skill by ID")
    void shouldPersistAndRetrieveCourse() {
        Skill skill = new Skill();
        skill.setName("Backend Development");
        entityManager.persistAndFlush(skill);

        Course course = new Course();
        course.setName("Java 17 Masterclass");
        course.setStatus(CourseStatus.DRAFT);
        course.setSkill(skill);

        entityManager.persistAndFlush(course);
        Long courseId = course.getId();
        entityManager.clear();

        Course found = entityManager.find(Course.class, courseId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(courseId);
        assertThat(found.getName()).isEqualTo("Java 17 Masterclass");
        assertThat(found.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(found.getSkill()).isNotNull();
        assertThat(found.getSkill().getId()).isEqualTo(skill.getId());
        assertThat(found.getSkill().getName()).isEqualTo("Backend Development");
    }

    @Test
    @DisplayName("Should update Course name, status and associated Skill")
    void shouldUpdateCourseStatus() {
        Skill skill1 = new Skill();
        skill1.setName("Spring Framework");
        entityManager.persistAndFlush(skill1);

        Skill skill2 = new Skill();
        skill2.setName("Cloud Computing");
        entityManager.persistAndFlush(skill2);

        Course course = new Course();
        course.setName("Microservices with Spring Cloud");
        course.setStatus(CourseStatus.DRAFT);
        course.setSkill(skill1);

        entityManager.persistAndFlush(course);
        Long courseId = course.getId();

        course.setStatus(CourseStatus.PENDING);
        entityManager.persistAndFlush(course);

        Course pending = entityManager.find(Course.class, courseId);
        assertThat(pending.getStatus()).isEqualTo(CourseStatus.PENDING);
        assertThat(pending.getSkill().getId()).isEqualTo(skill1.getId());

        course.setStatus(CourseStatus.PUBLISHED);
        course.setName("Advanced Microservices with Spring Cloud");
        course.setSkill(skill2);
        entityManager.persistAndFlush(course);
        entityManager.clear();

        Course published = entityManager.find(Course.class, courseId);
        assertThat(published.getName()).isEqualTo("Advanced Microservices with Spring Cloud");
        assertThat(published.getStatus()).isEqualTo(CourseStatus.PUBLISHED);
        assertThat(published.getSkill().getId()).isEqualTo(skill2.getId());
        assertThat(published.getSkill().getName()).isEqualTo("Cloud Computing");
    }

    @Test
    @DisplayName("Should support many courses pointing to one skill")
    void shouldSupportManyCoursesForOneSkill() {
        Skill skill = new Skill();
        skill.setName("Java Programming");
        entityManager.persistAndFlush(skill);

        Course course1 = new Course();
        course1.setName("Java Basics");
        course1.setStatus(CourseStatus.PUBLISHED);
        course1.setSkill(skill);

        Course course2 = new Course();
        course2.setName("Advanced Java");
        course2.setStatus(CourseStatus.PUBLISHED);
        course2.setSkill(skill);

        entityManager.persistAndFlush(course1);
        entityManager.persistAndFlush(course2);
        Long course1Id = course1.getId();
        Long course2Id = course2.getId();
        entityManager.clear();

        Course found1 = entityManager.find(Course.class, course1Id);
        Course found2 = entityManager.find(Course.class, course2Id);

        assertThat(found1).isNotNull();
        assertThat(found2).isNotNull();
        assertThat(found1.getSkill().getId()).isEqualTo(skill.getId());
        assertThat(found2.getSkill().getId()).isEqualTo(skill.getId());
    }

    @Test
    @DisplayName("Should delete Course without deleting referenced Skill")
    void shouldDeleteCourse() {
        Skill skill = new Skill();
        skill.setName("Architecture");
        entityManager.persistAndFlush(skill);
        Long skillId = skill.getId();

        Course course = new Course();
        course.setName("Legacy Architecture");
        course.setStatus(CourseStatus.DRAFT);
        course.setSkill(skill);

        entityManager.persistAndFlush(course);
        Long courseId = course.getId();
        assertThat(entityManager.find(Course.class, courseId)).isNotNull();

        entityManager.remove(course);
        entityManager.flush();

        Course deleted = entityManager.find(Course.class, courseId);
        assertThat(deleted).isNull();
        assertThat(entityManager.find(Skill.class, skillId)).isNotNull();
    }

    @Test
    @DisplayName("Should have @ManyToOne annotation on skill field")
    void shouldHaveManyToOneAnnotationOnSkill() throws NoSuchFieldException {
        Field field = Course.class.getDeclaredField("skill");
        assertThat(field.isAnnotationPresent(ManyToOne.class)).isTrue();
    }

    @Test
    @DisplayName("Should support all CourseStatus enum values")
    void shouldSupportAllCourseStatusValues() {
        assertThat(CourseStatus.values()).containsExactly(
            CourseStatus.DRAFT,
            CourseStatus.PENDING,
            CourseStatus.PUBLISHED
        );
    }

    @Test
    @DisplayName("Should persist and retrieve Course with startDate, endDate, and fee")
    void shouldPersistAndRetrieveCourseWithDatesAndFee() {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 15);
        BigDecimal fee = new BigDecimal("499.99");

        Course course = new Course();
        course.setName("Cloud Native Architecture");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setFee(fee);

        entityManager.persistAndFlush(course);
        Long courseId = course.getId();
        entityManager.clear();

        Course found = entityManager.find(Course.class, courseId);
        assertThat(found).isNotNull();
        assertThat(found.getStartDate()).isEqualTo(startDate);
        assertThat(found.getEndDate()).isEqualTo(endDate);
        assertThat(found.getFee()).isEqualByComparingTo(fee);
    }

    @Test
    @DisplayName("Should update startDate, endDate, and fee")
    void shouldUpdateDatesAndFee() {
        Course course = new Course();
        course.setName("DevOps Bootcamp");
        course.setStatus(CourseStatus.DRAFT);
        course.setStartDate(LocalDate.of(2026, 1, 1));
        course.setEndDate(LocalDate.of(2026, 3, 1));
        course.setFee(new BigDecimal("100.00"));

        entityManager.persistAndFlush(course);
        Long courseId = course.getId();

        LocalDate newStartDate = LocalDate.of(2026, 2, 1);
        LocalDate newEndDate = LocalDate.of(2026, 4, 1);
        BigDecimal newFee = new BigDecimal("250.00");
        course.setStartDate(newStartDate);
        course.setEndDate(newEndDate);
        course.setFee(newFee);

        entityManager.persistAndFlush(course);
        entityManager.clear();

        Course updated = entityManager.find(Course.class, courseId);
        assertThat(updated).isNotNull();
        assertThat(updated.getStartDate()).isEqualTo(newStartDate);
        assertThat(updated.getEndDate()).isEqualTo(newEndDate);
        assertThat(updated.getFee()).isEqualByComparingTo(newFee);
    }
}

package com.example.demo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest 
class InstructorUserTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should instantiate InstructorUser and set inherited and own fields")
    void shouldSetInheritedAndOwnProperties() {
        InstructorUser instructorUser = new InstructorUser();
        instructorUser.setEmail("instructor@example.com");
        instructorUser.setName("Dr.");
        instructorUser.setSurname("Smith");
        instructorUser.setPassword("pass123");

        Skill skill1 = new Skill();
        skill1.setId(101L);
        skill1.setName("Advanced Java");

        Skill skill2 = new Skill();
        skill2.setId(102L);
        skill2.setName("Spring Boot");

        instructorUser.addSkill(skill1);
        instructorUser.addSkill(skill2);

        instructorUser.createCourse(
                "Course 1",
                skill1,
                20,
                "Desc",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                new BigDecimal("100.00")
        );

        assertThat(instructorUser.getEmail()).isEqualTo("instructor@example.com");
        assertThat(instructorUser.getName()).isEqualTo("Dr.");
        assertThat(instructorUser.getSurname()).isEqualTo("Smith");
        assertThat(instructorUser.getPassword()).isEqualTo("pass123");

        Set<Skill> skills = instructorUser.getSkills();
        assertThat(skills).hasSize(2);
        assertThat(skills).containsExactly(skill1, skill2);

        Set<Course> courses = instructorUser.getCourses();
        assertThat(courses).hasSize(1);
    }

    @Test
    @DisplayName("Should add and remove skill using helper methods")
    void shouldAddAndRemoveSkill() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("helper@example.com");

        Skill skill = new Skill();
        skill.setId(105L);
        skill.setName("Docker");

        instructor.addSkill(skill);
        assertThat(instructor.getSkills()).contains(skill);

        instructor.removeSkill(skill);
        assertThat(instructor.getSkills()).doesNotContain(skill);
    }

    @Test
    @DisplayName("Should return an unmodifiable set from getSkills")
    void shouldReturnUnmodifiableSetFromGetSkills() {
        InstructorUser instructor = new InstructorUser();
        Skill skill = new Skill();
        skill.setId(101L);
        skill.setName("Java");

        instructor.addSkill(skill);

        Set<Skill> skills = instructor.getSkills();
        Skill extraSkill = new Skill();
        extraSkill.setId(102L);
        extraSkill.setName("Python");

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            skills.add(extraSkill);
        });
    }

    @Test
    @DisplayName("Should check if instructor has a specific skill with hasASkill")
    void shouldCheckHasASkill() {
        InstructorUser instructor = new InstructorUser();
        Skill skill1 = new Skill();
        skill1.setId(101L);
        skill1.setName("Java");

        Skill skill2 = new Skill();
        skill2.setId(102L);
        skill2.setName("Python");

        instructor.addSkill(skill1);

        assertThat(instructor.hasASkill(skill1)).isTrue();
        assertThat(instructor.hasASkill(skill2)).isFalse();
    }

    @Test
    @DisplayName("Should check if instructor has all skills in a set with hasSkills")
    void shouldCheckHasSkills() {
        InstructorUser instructor = new InstructorUser();
        Skill skill1 = new Skill();
        skill1.setId(101L);
        skill1.setName("Java");

        Skill skill2 = new Skill();
        skill2.setId(102L);
        skill2.setName("Spring Boot");

        Skill skill3 = new Skill();
        skill3.setId(103L);
        skill3.setName("Kubernetes");

        instructor.addSkill(skill1);
        instructor.addSkill(skill2);

        Set<Skill> requiredSkills = Set.of(skill1, skill2);
        Set<Skill> moreSkills = Set.of(skill1, skill2, skill3);

        assertThat(instructor.hasSkills(requiredSkills)).isTrue();
        assertThat(instructor.hasSkills(moreSkills)).isFalse();
        assertThat(instructor.hasSkills(null)).isFalse();
    }

    @Test
    @DisplayName("Should reflect many-to-many relationship where multiple instructors can have multiple skills")
    void shouldReflectManyToManyRelationWithSkills() {
        InstructorUser instructor1 = new InstructorUser();
        instructor1.setEmail("instructor1@example.com");

        InstructorUser instructor2 = new InstructorUser();
        instructor2.setEmail("instructor2@example.com");

        Skill skill1 = new Skill();
        skill1.setId(101L);
        skill1.setName("Advanced Java");

        Skill skill2 = new Skill();
        skill2.setId(102L);
        skill2.setName("Spring Boot");

        Skill skill3 = new Skill();
        skill3.setId(103L);
        skill3.setName("Microservices");

        // One instructor has many skills
        instructor1.addSkill(skill1);
        instructor1.addSkill(skill2);

        // Another instructor has many skills and shares skill1 with Instructor 1
        instructor2.addSkill(skill1);
        instructor2.addSkill(skill3);

        assertThat(instructor1.getSkills()).hasSize(2).contains(skill1, skill2);
        assertThat(instructor2.getSkills()).hasSize(2).contains(skill1, skill3);

        // Many instructors share the same skill
        assertThat(instructor1.getSkills()).contains(skill1);
        assertThat(instructor2.getSkills()).contains(skill1);
    }

    @Test
    @DisplayName("Should have @DiscriminatorValue('Instructor') annotation")
    void shouldHaveDiscriminatorValueAnnotation() {
        DiscriminatorValue dv = InstructorUser.class.getAnnotation(DiscriminatorValue.class);
        assertThat(dv).isNotNull();
        assertThat(dv.value()).isEqualTo("Instructor");
    }

    @Test
    @DisplayName("Should have @ManyToMany annotation on skills field")
    void shouldHaveManyToManyAnnotationOnSkills() throws NoSuchFieldException {
        Field field = InstructorUser.class.getDeclaredField("skills");
        assertThat(field.isAnnotationPresent(ManyToMany.class)).isTrue();
    }

    @Test
    @DisplayName("Should persist and retrieve Instructor by email")
    void shouldPersistAndRetrieveUser() {
        InstructorUser instructor1 = new InstructorUser();
        instructor1.setEmail("alice@example.com");
        instructor1.setName("Alice");
        instructor1.setSurname("Smith");
        instructor1.setPassword("password123");

        entityManager.persistAndFlush(instructor1);

        InstructorUser found = entityManager.find(InstructorUser.class, "alice@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
        assertThat(found.getName()).isEqualTo("Alice");
        assertThat(found.getSurname()).isEqualTo("Smith");
        assertThat(found.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should persist skills added via addSkill and update via removeSkill")
    void shouldPersistSkillsWithAddAndRemoveSkill() {
        Skill skill1 = new Skill();
        skill1.setName("Kotlin");
        entityManager.persistAndFlush(skill1);

        Skill skill2 = new Skill();
        skill2.setName("Rust");
        entityManager.persistAndFlush(skill2);

        Long skill1Id = skill1.getId();
        Long skill2Id = skill2.getId();

        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("polyglot@example.com");
        instructor.setName("Poly");
        instructor.setSurname("Glot");
        instructor.setPassword("secret");

        instructor.addSkill(skill1);
        instructor.addSkill(skill2);
        entityManager.persistAndFlush(instructor);
        entityManager.clear();

        InstructorUser found = entityManager.find(InstructorUser.class, "polyglot@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getSkills()).hasSize(2);

        found.removeSkill(found.getSkills().stream().filter(s -> s.getId().equals(skill1Id)).findFirst().orElseThrow());
        entityManager.persistAndFlush(found);
        entityManager.clear();

        InstructorUser afterRemove = entityManager.find(InstructorUser.class, "polyglot@example.com");
        assertThat(afterRemove.getSkills()).hasSize(1);
        assertThat(afterRemove.getSkills().iterator().next().getId()).isEqualTo(skill2Id);
    }
    
    @Test
    @DisplayName("Should update Instructor details")
    void shouldUpdateUser() {
        InstructorUser instructor1 = new InstructorUser();
        instructor1.setEmail("bob@example.com");
        instructor1.setName("Bob");
        instructor1.setSurname("Johnson");
        instructor1.setPassword("initialPass");

        entityManager.persistAndFlush(instructor1);

        instructor1.setName("Robert");
        instructor1.setPassword("updatedPass");
        entityManager.persistAndFlush(instructor1);

        User updated = entityManager.find(InstructorUser.class, "bob@example.com");
        assertThat(updated.getName()).isEqualTo("Robert");
        assertThat(updated.getPassword()).isEqualTo("updatedPass");
    }

    @Test
    @DisplayName("Should delete User by entity")
    void shouldDeleteUser() {
        InstructorUser instructor1 = new InstructorUser();
        instructor1.setEmail("charlie@example.com");
        instructor1.setName("Charlie");
        instructor1.setSurname("Brown");
        instructor1.setPassword("charliePass");

        entityManager.persistAndFlush(instructor1);
        assertThat(entityManager.find(User.class, "charlie@example.com")).isNotNull();

        entityManager.remove(instructor1);
        entityManager.flush();

        User deleted = entityManager.find(User.class, "charlie@example.com");
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("Should find InstructorUser polymorphically through User base class")
    void shouldFindPolymorphicallyAsUser() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("polyinstructor@example.com");
        instructor.setName("Poly");
        instructor.setSurname("Instructor");
        instructor.setPassword("polyPass");

        entityManager.persistAndFlush(instructor);

        User found = entityManager.find(User.class, "polyinstructor@example.com");
        assertThat(found).isNotNull();
        assertThat(found).isInstanceOf(InstructorUser.class);
        assertThat(found.getEmail()).isEqualTo("polyinstructor@example.com");
    }

    @Test
    @DisplayName("Should create course and remove course using helper methods")
    void shouldCreateAndRemoveCourse() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("instructor@example.com");

        Skill skill = new Skill();
        skill.setName("Java");

        instructor.createCourse(
                "Spring Boot Fundamentals",
                skill,
                20,
                "Desc",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                new BigDecimal("100.00")
        );
        assertThat(instructor.getCourses()).hasSize(1);
        Course course = instructor.getCourses().iterator().next();
        assertThat(course.getCreatedBy()).isEqualTo(instructor);

        instructor.removeCourse(course);
        assertThat(instructor.getCourses()).doesNotContain(course);
    }

    @Test
    @DisplayName("Should create course with all arguments")
    void shouldCreateCourseWithAllArguments() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("instructor@example.com");

        Skill skill = new Skill();
        skill.setId(10L);
        skill.setName("Java");

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        BigDecimal fee = new BigDecimal("199.99");

        instructor.createCourse(
                "Advanced Java",
                skill,
                50,
                "In-depth Java Course",
                startDate,
                endDate,
                fee
        );

        assertThat(instructor.getCourses()).hasSize(1);
        Course course = instructor.getCourses().iterator().next();
        assertThat(course.getName()).isEqualTo("Advanced Java");
        assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        assertThat(course.getSkill()).isEqualTo(skill);
        assertThat(course.getStudentMaxQuantity()).isEqualTo(50);
        assertThat(course.getDescription()).isEqualTo("In-depth Java Course");
        assertThat(course.getStartDate()).isEqualTo(startDate);
        assertThat(course.getEndDate()).isEqualTo(endDate);
        assertThat(course.getFee()).isEqualByComparingTo(fee);
        assertThat(course.getCreatedBy()).isEqualTo(instructor);
    }

    @Test
    @DisplayName("Should return an unmodifiable set from getCourses")
    void shouldReturnUnmodifiableSetFromGetCourses() {
        InstructorUser instructor = new InstructorUser();
        Skill skill = new Skill();
        skill.setName("Python");
        instructor.createCourse(
                "Python Programming",
                skill,
                20,
                "Desc",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                new BigDecimal("100.00")
        );

        Set<Course> courses = instructor.getCourses();
        Course extraCourse = new Course();
        extraCourse.setName("Python Programming");

        assertThrows(UnsupportedOperationException.class, () -> {
            courses.add(extraCourse);
        });
    }

    @Test
    @DisplayName("Should have @OneToMany annotation on courses field with mappedBy and cascade")
    void shouldHaveOneToManyAnnotationOnCourses() throws NoSuchFieldException {
        Field field = InstructorUser.class.getDeclaredField("courses");
        assertThat(field.isAnnotationPresent(OneToMany.class)).isTrue();
        OneToMany annotation = field.getAnnotation(OneToMany.class);
        assertThat(annotation.mappedBy()).isEqualTo("createdBy");
        assertThat(annotation.cascade()).containsExactly(CascadeType.ALL);
    }

    @Test
    @DisplayName("Should cascade persist courses when InstructorUser is persisted")
    void shouldCascadePersistCoursesWhenInstructorPersisted() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("prof@example.com");
        instructor.setName("Alan");
        instructor.setSurname("Turing");
        instructor.setPassword("enigma");

        Skill skill1 = new Skill();
        skill1.setName("CS");
        entityManager.persistAndFlush(skill1);

        Skill skill2 = new Skill();
        skill2.setName("Crypto");
        entityManager.persistAndFlush(skill2);

        instructor.createCourse("Theoretical Computer Science", skill1, 30, "Desc1", LocalDate.now(), LocalDate.now().plusMonths(1), new BigDecimal("100.00"));
        instructor.createCourse("Cryptography", skill2, 30, "Desc2", LocalDate.now(), LocalDate.now().plusMonths(1), new BigDecimal("100.00"));

        entityManager.persistAndFlush(instructor);
        entityManager.clear();

        InstructorUser found = entityManager.find(InstructorUser.class, "prof@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getCourses()).hasSize(2);
        assertThat(found.getCourses())
                .allMatch(c -> c.getCreatedBy() != null && "prof@example.com".equals(c.getCreatedBy().getEmail()));
    }

    @Test
    @DisplayName("Should cascade delete courses when InstructorUser is deleted")
    void shouldCascadeDeleteCoursesWhenInstructorDeleted() {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("todelete@example.com");
        instructor.setName("Delete");
        instructor.setSurname("Me");
        instructor.setPassword("pass");

        Skill skill = new Skill();
        skill.setName("Disposable");
        entityManager.persistAndFlush(skill);

        instructor.createCourse("Disposable Course", skill, 20, "Desc", LocalDate.now(), LocalDate.now().plusMonths(1), new BigDecimal("100.00"));

        entityManager.persistAndFlush(instructor);
        Long courseId = instructor.getCourses().iterator().next().getId();
        entityManager.clear();

        InstructorUser found = entityManager.find(InstructorUser.class, "todelete@example.com");
        assertThat(found).isNotNull();

        entityManager.remove(found);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(InstructorUser.class, "todelete@example.com")).isNull();
        assertThat(entityManager.find(Course.class, courseId)).isNull();
    }
}


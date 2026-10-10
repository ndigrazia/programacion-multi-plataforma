# E-Learning Platform Domain Model (Spring Boot & JPA)

A Spring Boot application implementing an e-learning platform domain model with Spring Data JPA, Hibernate, and an H2 database.

---

## Table of Contents

- [Overview](#overview)
- [Domain Model & Entities](#domain-model--entities)
- [Class & Relationship Overview](#class--relationship-overview)
- [Key Features & Business Logic](#key-features--business-logic)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Building & Testing](#building--testing)
- [Running the Application](#running-the-application)

---

## Overview

This project models the core domain of an educational course management and enrollment system:
- **Polymorphic User Management**: Role hierarchy (`AdminUser`, `InstructorUser`, `StudentUser`) inheriting from an abstract `User` base entity using Table-per-Class inheritance.
- **Course Lifecycle**: Courses with lifecycle statuses (`CourseStatus`: `DRAFT`, `PENDING`, `PUBLISHED`), date windows (`startDate`, `endDate`), capacity controls (`studentMaxQuantity`, `quantity`), fee (`fee`), and skill associations (`Skill`).
- **Course Creation & Approval Workflow**: Instructors create courses in `DRAFT` status and request administrative approval via `ApprovalRequest` (`ApprovalRequestStatus`: `SUBMITTED`, `APPROVED`, `REJECTED`), transitioning courses to `PENDING`.
- **Course Enrollment & Bundled Discounts**: Students enroll in published, open courses (`Enrollment`). When a student enrolls in multiple courses with the same skill within the same calendar month, an externalized discount (configured via `student.discount` in `application.properties`) is automatically applied.

---

## Domain Model & Entities

### 1. `User` (Abstract Base Entity)
- **Inheritance**: `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`
- **Attributes**: `email` (PK), `name`, `surname`, `password`
- **Abstract Method**:
  ```java
  public abstract void createCourse(String name, Skill skill, int studentMaxQuantity, 
      String description, LocalDate startDate, LocalDate endDate, BigDecimal fee);
  ```

### 2. `AdminUser`
- Extends `User` with `@DiscriminatorValue("Admin")`

### 3. `InstructorUser`
- Extends `User` with `@DiscriminatorValue("Instructor")`
- **Relationships**:
  - Many-to-Many with `Skill` (`Set<Skill> skills`)
  - One-to-Many with `Course` (`@OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL) Set<Course> courses`)
  - One-to-Many with `ApprovalRequest` (`@OneToMany Set<ApprovalRequest> approvalRequests`)
- **Domain Methods**:
  - `addSkill(Skill s)`, `removeSkill(Skill s)`, `hasASkill(Skill s)`, `hasSkills(Set<Skill> skills)`
  - `getSkills()`: Returns an unmodifiable `Set<Skill>` view.
  - `getCourses()`: Returns an unmodifiable `Set<Course>` view.
  - `createCourse(...)`: Instantiates a `Course` with `status = DRAFT`, sets `createdBy = this`, and associates it with the instructor.
  - `requestApproval(Course c, String comment)`: Submits an `ApprovalRequest` with status `SUBMITTED`, switches the course status to `PENDING`, and records it in `approvalRequests`. Throws `NoOwnerCourseException` if the instructor does not own the course, or `NoDraftCourseException` if the course is not in `DRAFT` status.
  - `getApprovalRequests()`: Returns an unmodifiable `Set<ApprovalRequest>` view.

### 4. `StudentUser`
- Extends `User` with `@DiscriminatorValue("Student")`
- **Relationships**:
  - One-to-Many with `Enrollment` (`@OneToMany(mappedBy = "student", cascade = CascadeType.ALL) Set<Enrollment> enrollmens`)
- **Domain Methods**:
  - `applyCourse(Course course, LocalDate date)`: Enrolls the student in a course.
    - Validates course is `PUBLISHED` (throws `NoPublishStatusException`).
    - Validates course is not full (throws `FullCourseException`).
    - Validates course registration is open today (throws `ClosedCourseException`).
    - Applies a discount (default 25%, externalized in `application.properties`) if `hasDiscount(course, date)` is true (i.e. student already has another enrollment with the same skill in the same calendar month).
    - Creates and records `Enrollment(this, course, date, payment)`.
    - Increments course enrollment count via `course.increaseQuantity()`.
  - `getEnrollments()`: Returns an unmodifiable `Set<Enrollment>` view.
  - `setDiscount(BigDecimal)` / `getDiscount()`: Configures the discount percentage.

### 5. `Course`
- **Attributes**:
  - `id` (`long`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`)
  - `name` (`String`)
  - `status` (`CourseStatus`: `DRAFT`, `PENDING`, `PUBLISHED`)
  - `skill` (`Skill`, `@ManyToOne`)
  - `studentMaxQuantity` (`int`): Maximum capacity.
  - `quantity` (`int`): Current enrolled student count.
  - `description` (`String`)
  - `startDate` (`LocalDate`, columnDefinition = "DATE")
  - `endDate` (`LocalDate`, columnDefinition = "DATE")
  - `fee` (`BigDecimal`, precision = 20, scale = 2)
  - `createdBy` (`InstructorUser`, `@ManyToOne`)
- **Domain Methods**:
  - `isFull()`: Returns `true` if `quantity >= studentMaxQuantity`.
  - `increaseQuantity()`: Increments `quantity`.
  - `isOpen()`: Returns `true` if current date (`LocalDate.now()`) is between `startDate` and `endDate`.

### 6. `Enrollment` (Association Entity)
- Represents a student enrollment in a course.
- **Attributes**:
  - `id` (`Long`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`)
  - `student` (`StudentUser`, `@ManyToOne`)
  - `course` (`Course`, `@ManyToOne`)
  - `date` (`LocalDate`, columnDefinition = "DATE")
  - `price` (`BigDecimal`, precision = 20, scale = 2)
- **Domain Methods**:
  - `isSameMonthAndSameSkill(Month m, Skill s)`: Returns `true` if the enrollment date's month equals `m` and the enrolled course's skill equals `s`.
  - `isSameMonthAndSameSkill(LocalDate d, Skill s)`: Overload accepting a `LocalDate`.

### 7. `ApprovalRequest`
- Represents a course publication approval request submitted by an instructor.
- **Attributes**:
  - `id` (`Long`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`)
  - `course` (`Course`, `@ManyToOne`)
  - `requestDate` (`LocalDate`, columnDefinition = "DATE")
  - `responseDate` (`LocalDate`, columnDefinition = "DATE")
  - `status` (`ApprovalRequestStatus`: `SUBMITTED`, `APPROVED`, `REJECTED`)
  - `commentAdmin` (`String`)
  - `comment` (`String`)

### 8. `Skill`
- Competency topic associated with instructors and courses.
- **Attributes**: `id` (`Long`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`), `name` (`String`)

---

## Class & Relationship Overview

### Class Diagram

```mermaid
classDiagram
    class User {
        <<abstract>>
        -String email
        -String name
        -String surname
        -String password
        +getEmail() String
        +setEmail(String) void
        +getName() String
        +setName(String) void
        +getSurname() String
        +setSurname(String) void
        +getPassword() String
        +setPassword(String) void
        +createCourse(String, Skill, int, String, LocalDate, LocalDate, BigDecimal)* void
    }

    class AdminUser {
        +createCourse(...) void
    }

    class InstructorUser {
        -Set~Skill~ skills
        -Set~Course~ courses
        -Set~ApprovalRequest~ approvalRequests
        +addSkill(Skill) void
        +removeSkill(Skill) void
        +hasASkill(Skill) boolean
        +hasSkills(Set~Skill~) boolean
        +getSkills() Set~Skill~
        +getCourses() Set~Course~
        +createCourse(...) void
        +requestApproval(Course, String) void
        +getApprovalRequests() Set~ApprovalRequest~
    }

    class StudentUser {
        -BigDecimal discount
        -Set~Enrollment~ enrollmens
        +applyCourse(Course, LocalDate) void
        +getEnrollments() Set~Enrollment~
        +createCourse(...) void
    }

    class Course {
        -long id
        -String name
        -CourseStatus status
        -Skill skill
        -int studentMaxQuantity
        -int quantity
        -String description
        -LocalDate startDate
        -LocalDate endDate
        -BigDecimal fee
        -InstructorUser createdBy
        +isFull() boolean
        +increaseQuantity() void
        +isOpen() boolean
    }

    class Enrollment {
        -Long id
        -StudentUser student
        -Course course
        -LocalDate date
        -BigDecimal price
        +isSameMonthAndSameSkill(Month, Skill) boolean
        +isSameMonthAndSameSkill(LocalDate, Skill) boolean
        +getSkill() Skill
    }

    class ApprovalRequest {
        -Long id
        -Course course
        -LocalDate requestDate
        -LocalDate responseDate
        -ApprovalRequestStatus status
        -String commentAdmin
        -String comment
    }

    class Skill {
        -Long id
        -String name
    }

    class CourseStatus {
        <<enumeration>>
        DRAFT
        PENDING
        PUBLISHED
    }

    class ApprovalRequestStatus {
        <<enumeration>>
        SUBMITTED
        APPROVED
        REJECTED
    }

    User <|-- AdminUser : extends
    User <|-- InstructorUser : extends
    User <|-- StudentUser : extends

    InstructorUser "1" o-- "*" Skill : @ManyToMany skills
    InstructorUser "1" *-- "*" Course : @OneToMany createdBy
    InstructorUser "1" *-- "*" ApprovalRequest : @OneToMany approvalRequests
    Course "*" --> "1" Skill : @ManyToOne skill
    Course --> "1" CourseStatus : status
    StudentUser "1" *-- "*" Enrollment : @OneToMany student
    Enrollment "*" --> "1" Course : @ManyToOne course
    Enrollment "*" --> "1" StudentUser : @ManyToOne student
    ApprovalRequest "*" --> "1" Course : @ManyToOne course
    ApprovalRequest --> "1" ApprovalRequestStatus : status
```

### Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    ADMIN_USER {
        VARCHAR email PK
        VARCHAR name
        VARCHAR surname
        VARCHAR password
    }

    INSTRUCTOR_USER {
        VARCHAR email PK
        VARCHAR name
        VARCHAR surname
        VARCHAR password
    }

    STUDENT_USER {
        VARCHAR email PK
        VARCHAR name
        VARCHAR surname
        VARCHAR password
    }

    SKILL {
        BIGINT id PK
        VARCHAR name
    }

    COURSE {
        BIGINT id PK
        VARCHAR name
        VARCHAR status
        INT student_max_quantity
        INT quantity
        VARCHAR description
        DATE start_date
        DATE end_date
        NUMERIC fee
        BIGINT skill_id FK
        VARCHAR created_by_email FK
    }

    ENROLLMENT {
        BIGINT id PK
        VARCHAR student_email FK
        BIGINT course_id FK
        DATE date
        NUMERIC price
    }

    APPROVAL_REQUEST {
        BIGINT id PK
        BIGINT course_id FK
        DATE request_date
        DATE response_date
        VARCHAR status
        VARCHAR comment_admin
        VARCHAR comment
    }

    INSTRUCTOR_USER_SKILLS {
        VARCHAR instructor_user_email PK, FK
        BIGINT skills_id PK, FK
    }

    INSTRUCTOR_USER_APPROVAL_REQUESTS {
        VARCHAR instructor_user_email PK, FK
        BIGINT approval_requests_id PK, FK
    }

    INSTRUCTOR_USER ||--o{ INSTRUCTOR_USER_SKILLS : "possesses"
    SKILL ||--o{ INSTRUCTOR_USER_SKILLS : "assigned_to"

    INSTRUCTOR_USER ||--o{ COURSE : "creates"
    SKILL ||--o{ COURSE : "categorizes"

    INSTRUCTOR_USER ||--o{ INSTRUCTOR_USER_APPROVAL_REQUESTS : "requests"
    APPROVAL_REQUEST ||--o{ INSTRUCTOR_USER_APPROVAL_REQUESTS : "linked_to"

    STUDENT_USER ||--o{ ENROLLMENT : "enrolls"
    COURSE ||--o{ ENROLLMENT : "contains"
    COURSE ||--o{ APPROVAL_REQUEST : "subject_of"
```


---

## Key Features & Business Logic

- **JPA Table-per-Class Inheritance**: Clean relational representation of users (`AdminUser`, `InstructorUser`, `StudentUser`) with polymorphic querying over `User.class`.
- **Course Enrollment Validation**:
  - Enforces course status (`PUBLISHED`), capacity limit (`quantity < studentMaxQuantity`), and date window validity (`today` between `startDate` and `endDate`).
  - Throws custom domain exceptions: `NoPublishStatusException`, `FullCourseException`, `ClosedCourseException`.
- **Bundled Enrollment Discount**:
  - If a student applies to multiple courses sharing the same skill within the same calendar month, subsequent enrollments automatically receive a 25% discount.
  - Calculated with `RoundingMode.HALF_UP` to two decimal places.
- **Externalized Configuration**:
  - The student discount percentage is externalized to `application.properties` via `student.discount=25` and dynamically bound to `StudentUser`.
- **Course Approval Workflow**:
  - Instructors submit draft courses for publication review via `requestApproval(...)`.
  - Enforces that only draft courses owned by the instructor can be submitted (`NoOwnerCourseException`, `NoDraftCourseException`).
- **Encapsulation & Immutability**:
  - Entity collections (`getSkills()`, `getCourses()`, `getEnrollments()`, `getApprovalRequests()`) return unmodifiable views via `Collections.unmodifiableSet(...)`.

---

## Configuration & Logging (`application.properties`)

All logging and application configuration is managed directly in `demo/src/main/resources/application.properties`:

```properties
spring.application.name=demo
student.discount=25

# Logging Levels
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG

# Console Formatting
logging.pattern.console=%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx

# File Output & Rolling Policy
logging.file.name=logs/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.logback.rollingpolicy.file-name-pattern=logs/archived/application-%d{yyyy-MM-dd}.%i.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=30
logging.logback.rollingpolicy.total-size-cap=1GB
```

- **No XML File Needed**: Spring Boot includes full native support for Logback configuration directly through properties, including log levels, formatting patterns, console coloring, file destinations, and rolling/retention policies.
- **HTTP Request Logging**: `CommonsRequestLoggingFilter` configured in `LoggingConfig` logs incoming web requests (client info, query strings, payloads).

---

## Project Structure

```
demo/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── DemoApplication.java
    │   │   ├── config/
    │   │   │   └── LoggingConfig.java
    │   │   ├── entity/
    │   │   │   ├── AdminUser.java
    │   │   │   ├── ApprovalRequest.java
    │   │   │   ├── ApprovalRequestStatus.java
    │   │   │   ├── Course.java
    │   │   │   ├── CourseStatus.java
    │   │   │   ├── Enrollment.java
    │   │   │   ├── InstructorUser.java
    │   │   │   ├── Skill.java
    │   │   │   ├── StudentUser.java
    │   │   │   └── User.java
    │   │   └── exception/
    │   │       ├── ClosedCourseException.java
    │   │       ├── FullCourseException.java
    │   │       ├── NoDraftCourseException.java
    │   │       ├── NoOwnerCourseException.java
    │   │       └── NoPublishStatusException.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           └── index.html
    └── test/
        └── java/com/example/demo/
            ├── DemoApplicationTests.java
            ├── config/
            │   └── LoggingConfigTest.java
            └── entity/
                ├── AdminUserTest.java
                ├── CourseTest.java
                ├── InstructorUserTest.java
                ├── SkillEntityTest.java
                └── StudentUserTest.java
```

---

## Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher
- **Maven**: Bundled Maven wrapper (`./mvnw`) is included

---

## Building & Testing

### Run All Unit and Integration Tests
```bash
cd demo
./mvnw clean test
```

### Build JAR Package
```bash
cd demo
./mvnw clean package
```

### Test Coverage Highlights
- **`StudentUserTest` (21 tests)**: Tests course application flow, capacity check exceptions (`FullCourseException`), publication status checks (`NoPublishStatusException`), availability date checks (`ClosedCourseException`), same-month/same-skill discount calculation (including `RoundingMode.HALF_UP`), discount externalization via `application.properties`, unmodifiable collection enforcement, and JPA persistence.
- **`InstructorUserTest` (19 tests)**: Tests `Skill` relationships and helper methods (`addSkill`, `removeSkill`, `hasASkill`, `hasSkills`), Course creation (`createCourse`), Course cascade persistence and deletion, and polymorphic user retrieval.
- **`CourseTest` (10 tests)**: Tests Course instantiation, Lombok builder, lifecycle statuses (`CourseStatus`), `@ManyToOne` association with `Skill`, `quantity` increment logic, `LocalDate` start/end date handling, `BigDecimal` fee persistence, and updates.
- **`AdminUserTest` (7 tests)**: Tests inheritance, entity persistence, updates, deletion, discriminator values, and polymorphic querying via `User.class`.
- **`SkillEntityTest` (3 tests)**: Tests persistence, updates, and deletion for `Skill`.
- **`DemoApplicationTests` (1 test)**: Context loads verification.

---

## Running the Application

To run the Spring Boot application locally:

```bash
cd demo
./mvnw spring-boot:run
```

Once started:
- Web Page: [http://localhost:8080/index.html](http://localhost:8080/index.html)
- Embedded H2 in-memory database runs automatically during execution.

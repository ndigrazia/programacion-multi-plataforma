package com.example.demo.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdminUserTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should instantiate AdminUser and set inherited fields")
    void shouldSetInheritedProperties() {
        AdminUser admin = new AdminUser();
        admin.setEmail("admin@example.com");
        admin.setName("Admin");
        admin.setSurname("Root");
        admin.setPassword("admin123");

        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        assertThat(admin.getName()).isEqualTo("Admin");
        assertThat(admin.getSurname()).isEqualTo("Root");
        assertThat(admin.getPassword()).isEqualTo("admin123");
    }

    @Test
    @DisplayName("Should have @DiscriminatorValue('Admin') annotation")
    void shouldHaveDiscriminatorValueAnnotation() {
        DiscriminatorValue dv = AdminUser.class.getAnnotation(DiscriminatorValue.class);
        assertThat(dv).isNotNull();
        assertThat(dv.value()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("Should persist and retrieve AdminUser by email")
    void shouldPersistAndRetrieveAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setEmail("admin@example.com");
        admin.setName("Admin");
        admin.setSurname("Root");
        admin.setPassword("admin123");

        entityManager.persistAndFlush(admin);

        AdminUser found = entityManager.find(AdminUser.class, "admin@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("admin@example.com");
        assertThat(found.getName()).isEqualTo("Admin");
        assertThat(found.getSurname()).isEqualTo("Root");
        assertThat(found.getPassword()).isEqualTo("admin123");
    }

    @Test
    @DisplayName("Should update AdminUser details")
    void shouldUpdateAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setEmail("admin2@example.com");
        admin.setName("Admin");
        admin.setSurname("Two");
        admin.setPassword("pass1");

        entityManager.persistAndFlush(admin);

        admin.setName("SuperAdmin");
        admin.setPassword("newPass");
        entityManager.persistAndFlush(admin);

        AdminUser updated = entityManager.find(AdminUser.class, "admin2@example.com");
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("SuperAdmin");
        assertThat(updated.getPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("Should delete AdminUser by entity")
    void shouldDeleteAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setEmail("admin3@example.com");
        admin.setName("Delete");
        admin.setSurname("Me");
        admin.setPassword("deletePass");

        entityManager.persistAndFlush(admin);
        assertThat(entityManager.find(AdminUser.class, "admin3@example.com")).isNotNull();

        entityManager.remove(admin);
        entityManager.flush();

        AdminUser deleted = entityManager.find(AdminUser.class, "admin3@example.com");
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("Should have @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) on User base class")
    void shouldHaveTablePerClassInheritanceOnUser() {
        Inheritance inheritance = User.class.getAnnotation(Inheritance.class);
        assertThat(inheritance).isNotNull();
        assertThat(inheritance.strategy()).isEqualTo(InheritanceType.TABLE_PER_CLASS);
    }

    @Test
    @DisplayName("Should find AdminUser polymorphically through User base class")
    void shouldFindPolymorphicallyAsUser() {
        AdminUser admin = new AdminUser();
        admin.setEmail("polyadmin@example.com");
        admin.setName("Poly");
        admin.setSurname("Admin");
        admin.setPassword("polyPass");

        entityManager.persistAndFlush(admin);

        User found = entityManager.find(User.class, "polyadmin@example.com");
        assertThat(found).isNotNull();
        assertThat(found).isInstanceOf(AdminUser.class);
        assertThat(found.getEmail()).isEqualTo("polyadmin@example.com");
    }
}

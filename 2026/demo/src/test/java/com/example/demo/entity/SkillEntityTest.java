package com.example.demo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SkillEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should persist and retrieve Skill by ID")
    void shouldPersistAndRetrieveSkill() {
        Skill skill = new Skill();
        skill.setName("Java Spring Boot");

        entityManager.persistAndFlush(skill);
        Long id = skill.getId();

        Skill found = entityManager.find(Skill.class, id);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("Java Spring Boot");
    }

    @Test
    @DisplayName("Should update skill name")
    void shouldUpdateSkillName() {
        Skill skill = new Skill();
        skill.setName("Microservices");

        entityManager.persistAndFlush(skill);
        Long id = skill.getId();

        skill.setName("Advanced Microservices");
        entityManager.persistAndFlush(skill);

        Skill updated = entityManager.find(Skill.class, id);
        assertThat(updated.getName()).isEqualTo("Advanced Microservices");
    }

    @Test
    @DisplayName("Should delete Skill")
    void shouldDeleteSkill() {
        Skill skill = new Skill();
        skill.setName("Database Design");

        entityManager.persistAndFlush(skill);
        Long id = skill.getId();
        assertThat(entityManager.find(Skill.class, id)).isNotNull();

        entityManager.remove(skill);
        entityManager.flush();

        Skill deleted = entityManager.find(Skill.class, id);
        assertThat(deleted).isNull();
    }
}


package com.example.demo.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import com.example.demo.dto.CreateInstructorRequestDTO;
import com.example.demo.entity.InstructorUser;

class ModelMapperConfigTest {

    @Test
    @DisplayName("Should create ModelMapper bean")
    void shouldCreateModelMapperBean() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper modelMapper = config.modelMapper();

        assertThat(modelMapper).isNotNull();
    }

    @Test
    @DisplayName("Should map CreateInstructorRequestDTO to InstructorUser")
    void shouldMapCreateInstructorRequestDTOToInstructorUser() {
        ModelMapperConfig config = new ModelMapperConfig();
        ModelMapper modelMapper = config.modelMapper();

        CreateInstructorRequestDTO dto = new CreateInstructorRequestDTO("instructor@test.com", "securePassword");
        InstructorUser instructor = modelMapper.map(dto, InstructorUser.class);

        assertThat(instructor).isNotNull();
        assertThat(instructor.getEmail()).isEqualTo("instructor@test.com");
        assertThat(instructor.getPassword()).isEqualTo("securePassword");
    }
}

package com.example.demo.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.CreateInstructorRequestDTO;
import com.example.demo.entity.InstructorUser;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.service.InstructorService;

@WebMvcTest(InstructorResource.class)
class InstructorResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstructorService instructorService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Test
    @DisplayName("Should return 201 Created when instructor is successfully created")
    void shouldReturn201WhenInstructorCreatedSuccessfully() throws Exception {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("john.doe@example.com");
        instructor.setPassword("securePassword123");

        when(modelMapper.map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class)))
                .thenReturn(instructor);

        String requestJson = """
                {
                    "email": "john.doe@example.com",
                    "password": "securePassword123"
                }
                """;

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        ArgumentCaptor<CreateInstructorRequestDTO> captor = ArgumentCaptor.forClass(CreateInstructorRequestDTO.class);
        verify(modelMapper).map(captor.capture(), eq(InstructorUser.class));
        assertThat(captor.getValue().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("securePassword123");
        verify(instructorService).create(instructor);
    }

    @Test
    @DisplayName("Should return 409 Conflict when EmailAlreadyExistsException is thrown")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("existing@example.com");
        instructor.setPassword("password123");

        when(modelMapper.map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class)))
                .thenReturn(instructor);
        doThrow(new EmailAlreadyExistsException("Email already exists: existing@example.com"))
                .when(instructorService).create(instructor);

        String requestJson = """
                {
                    "email": "existing@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists: existing@example.com"));

        verify(modelMapper).map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class));
        verify(instructorService).create(instructor);
    }

    @Test
    @DisplayName("Should return 409 Conflict when model mapper throws exception")
    void shouldReturn409WhenModelMapperFails() throws Exception {
        when(modelMapper.map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class)))
                .thenThrow(new RuntimeException("Mapping error occurred"));

        String requestJson = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Mapping error occurred"));

        verify(instructorService, never()).create(any());
    }

    @Test
    @DisplayName("Should return 409 Conflict when service throws a generic runtime exception")
    void shouldReturn409WhenServiceThrowsRuntimeException() throws Exception {
        InstructorUser instructor = new InstructorUser();
        instructor.setEmail("instructor@example.com");

        when(modelMapper.map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class)))
                .thenReturn(instructor);
        doThrow(new RuntimeException("Database connection failure"))
                .when(instructorService).create(instructor);

        String requestJson = """
                {
                    "email": "instructor@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Database connection failure"));

        verify(modelMapper).map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class));
        verify(instructorService).create(instructor);
    }

    @Test
    @DisplayName("Should return 409 Conflict with empty body when exception message is null")
    void shouldReturn409WhenExceptionMessageIsNull() throws Exception {
        InstructorUser instructor = new InstructorUser();

        when(modelMapper.map(any(CreateInstructorRequestDTO.class), eq(InstructorUser.class)))
                .thenReturn(instructor);
        doThrow(new NullPointerException())
                .when(instructorService).create(instructor);

        String requestJson = """
                {
                    "email": "nullmessage@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(content().string(""));

        verify(instructorService).create(instructor);
    }
}

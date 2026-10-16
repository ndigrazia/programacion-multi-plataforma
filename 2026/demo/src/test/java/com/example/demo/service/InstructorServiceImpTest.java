package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entity.InstructorUser;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.repository.InstructorUserRepository;

@ExtendWith(MockitoExtension.class)
class InstructorServiceImpTest {

    @Mock
    private InstructorUserRepository instructorUserRepository;

    @InjectMocks
    private InstructorServiceImp instructorService;

    private InstructorUser instructor;

    @BeforeEach
    void setUp() {
        instructor = new InstructorUser();
        instructor.setEmail("instructor@example.com");
        instructor.setName("Jane");
        instructor.setSurname("Doe");
        instructor.setPassword("password123");
    }

    @Test
    @DisplayName("Should successfully create instructor when email does not exist")
    void shouldCreateInstructorSuccessfullyWhenEmailDoesNotExist() throws EmailAlreadyExistsException {
        // Arrange
        when(instructorUserRepository.existsByEmail("instructor@example.com")).thenReturn(false);

        // Act
        assertDoesNotThrow(() -> instructorService.create(instructor));

        // Assert
        verify(instructorUserRepository).existsByEmail("instructor@example.com");
        verify(instructorUserRepository).save(instructor);
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailExists() {
        // Arrange
        when(instructorUserRepository.existsByEmail("instructor@example.com")).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> instructorService.create(instructor)
        );

        assertThat(exception.getMessage()).isEqualTo("Email already exists: instructor@example.com");
        verify(instructorUserRepository).existsByEmail("instructor@example.com");
        verify(instructorUserRepository, never()).save(any(InstructorUser.class));
    }
}

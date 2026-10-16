package com.example.demo.resource;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CreateInstructorRequestDTO;
import com.example.demo.entity.InstructorUser;
import com.example.demo.service.InstructorService;

@RestController
public class InstructorResource {

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/instructors")
    public ResponseEntity<?> createInstructor(@RequestBody CreateInstructorRequestDTO requestDTO) {
        try {
            InstructorUser instructor = modelMapper.map(requestDTO, InstructorUser.class);
            instructorService.create(instructor);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

}


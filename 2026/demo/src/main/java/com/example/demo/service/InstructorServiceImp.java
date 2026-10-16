package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.InstructorUser;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.repository.InstructorUserRepository;

@Service
public class InstructorServiceImp implements InstructorService {

    @Autowired
    private InstructorUserRepository instructorUserRepository;

    @Override
    public void create(InstructorUser instructor) throws EmailAlreadyExistsException {
        if (instructorUserRepository.existsByEmail(instructor.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + instructor.getEmail());
        }
        
        //TODO Use PasswordEncoder service to encode the password before saving the instructor
        instructorUserRepository.save(instructor);
    }
}


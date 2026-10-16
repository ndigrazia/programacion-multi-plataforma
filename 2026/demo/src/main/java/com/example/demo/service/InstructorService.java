package com.example.demo.service;

import com.example.demo.entity.InstructorUser;
import com.example.demo.exception.EmailAlreadyExistsException;

public interface InstructorService {

    public void create(InstructorUser instructor) throws EmailAlreadyExistsException;

}


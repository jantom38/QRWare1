package com.qrware.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // Konstruktor dla standardowych komunikatów (ten był używany w serwisie, ale go brakowało)
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Konstruktor dla ustrukturyzowanych błędów
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }
}
package com.matvey.objectaccountingservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionsTest {

    @Test
    void InvalidDateException_Constructor_SetsMessage() {
        String message = "Invalid date range";
        InvalidDateException exception = new InvalidDateException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void InvalidFileException_Constructor_SetsMessage() {
        String message = "Invalid file type";
        InvalidFileException exception = new InvalidFileException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void InvalidIdException_Constructor_SetsMessage() {
        String message = "Invalid ID provided";
        InvalidIdException exception = new InvalidIdException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void InvalidPhoneNumberException_Constructor_SetsMessage() {
        String message = "Invalid phone number format";
        InvalidPhoneNumberException exception = new InvalidPhoneNumberException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void InvalidStatusException_Constructor_SetsMessage() {
        String message = "Invalid status transition";
        InvalidStatusException exception = new InvalidStatusException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void ValidationException_Constructor_SetsMessage() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void ResourceNotFoundException_Constructor_SetsMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void DuplicateResourceException_Constructor_SetsMessage() {
        String message = "Resource already exists";
        DuplicateResourceException exception = new DuplicateResourceException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void BusinessLogicException_Constructor_SetsMessage() {
        String message = "Business logic violation";
        BusinessLogicException exception = new BusinessLogicException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getClass()
                .getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
    }

    @Test
    void AllExceptions_AreRuntimeExceptions() {
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidDateException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidFileException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidIdException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidPhoneNumberException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(InvalidStatusException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(ValidationException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(ResourceNotFoundException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(DuplicateResourceException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(BusinessLogicException.class));
    }
}

package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.ObjectRequestDto;
import com.matvey.objectaccountingservice.dto.response.ObjectResponseDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.mapper.ObjectMapper;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.service.ObjectService;
import com.matvey.objectaccountingservice.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectRepository objectRepository;
    private final StorageService storageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectResponseDto> create(@Valid @RequestBody ObjectRequestDto dto) {
        Object object = objectMapper.toEntity(dto);
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        object.setCustomer(customer);
        if (dto.getResponsibleEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getResponsibleEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            object.setResponsibleEmployee(employee);
        }
        Object savedObject = objectRepository.save(object);
        return new ResponseEntity<>(objectMapper.toResponseDto(savedObject), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ObjectResponseDto> getById(@PathVariable Long id) {
        Object object = objectService.getById(id);
        return ResponseEntity.ok(objectMapper.toResponseDto(object));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ObjectResponseDto>> getAll() {
        List<Object> objects = objectService.getAll();
        return ResponseEntity.ok(objects.stream()
                .map(objectMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ObjectResponseDto>> getByCustomerId(@PathVariable Long customerId) {
        List<Object> objects = objectService.getByCustomerId(customerId);
        return ResponseEntity.ok(objects.stream()
                .map(objectMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ObjectResponseDto>> getByStatus(@PathVariable String status) {
        List<Object> objects = objectService.getByStatus(status);
        return ResponseEntity.ok(objects.stream()
                .map(objectMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/work-type/{workType}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ObjectResponseDto>> getByWorkType(@PathVariable WorkType workType) {
        List<Object> objects = objectService.getByWorkType(workType);
        return ResponseEntity.ok(objects.stream()
                .map(objectMapper::toResponseDto)
                .toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectResponseDto> update(@PathVariable Long id, @Valid @RequestBody ObjectRequestDto dto) {
        Object existingObject = objectService.getById(id);
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        existingObject.setName(dto.getName());
        existingObject.setStatus(dto.getStatus());
        existingObject.setAddress(dto.getAddress());
        existingObject.setWorkType(dto.getWorkType());
        existingObject.setCustomer(customer);
        
        if (dto.getResponsibleEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getResponsibleEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            existingObject.setResponsibleEmployee(employee);
        } else {
            existingObject.setResponsibleEmployee(null);
        }
        
        Object updatedObject = objectRepository.save(existingObject);
        return ResponseEntity.ok(objectMapper.toResponseDto(updatedObject));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ObjectResponseDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Object existingObject = objectService.getById(id);
        existingObject.setStatus(request.get("status"));
        Object updatedObject = objectRepository.save(existingObject);
        return ResponseEntity.ok(objectMapper.toResponseDto(updatedObject));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        objectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectResponseDto> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Object object = objectService.getById(id);
            String fileName = storageService.uploadObjectImage(file);
            object.setImageUniqueName(fileName);
            Object updatedObject = objectRepository.save(object);
            return ResponseEntity.ok(objectMapper.toResponseDto(updatedObject));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectResponseDto> replaceImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Object object = objectService.getById(id);
            if (object.getImageUniqueName() != null) {
                storageService.replaceObjectImage(object.getImageUniqueName(), file);
            } else {
                String fileName = storageService.uploadObjectImage(file);
                object.setImageUniqueName(fileName);
            }
            Object updatedObject = objectService.update(id, object);
            return ResponseEntity.ok(objectMapper.toResponseDto(updatedObject));
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace image: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) {
        try {
            Object object = objectService.getById(id);
            if (object.getImageUniqueName() == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] imageData = storageService.downloadObjectImage(object.getImageUniqueName());
            HttpHeaders headers = new HttpHeaders();
            String contentType = object.getImageUniqueName().endsWith(".png") ? "image/png" : "image/jpeg";
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", object.getImageUniqueName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download image: " + e.getMessage(), e);
        }
    }
}

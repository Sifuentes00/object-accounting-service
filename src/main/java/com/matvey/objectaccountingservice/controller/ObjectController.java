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
import com.matvey.objectaccountingservice.service.ObjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObjectResponseDto> create(@Valid @RequestBody ObjectRequestDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Object object = objectMapper.toEntity(dto);
        object.setCustomer(customer);
        if (dto.getResponsibleEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getResponsibleEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            object.setResponsibleEmployee(employee);
        }
        Object savedObject = objectService.create(object);
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
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Object object = objectMapper.toEntity(dto);
        object.setCustomer(customer);
        if (dto.getResponsibleEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getResponsibleEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            object.setResponsibleEmployee(employee);
        }
        Object updatedObject = objectService.update(id, object);
        return ResponseEntity.ok(objectMapper.toResponseDto(updatedObject));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        objectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

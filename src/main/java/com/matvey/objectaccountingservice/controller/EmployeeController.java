package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.EmployeeRequestDto;
import com.matvey.objectaccountingservice.dto.response.EmployeeResponseDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.exception.InvalidPhoneNumberException;
import com.matvey.objectaccountingservice.mapper.EmployeeMapper;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private final CustomerRepository customerRepository;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto dto) {
        if (!PHONE_PATTERN.matcher(dto.getPhoneNumber()).matches()) {
            throw new InvalidPhoneNumberException("Phone number must be 10-15 digits, optionally starting with +");
        }
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = employeeMapper.toEntity(dto);
        employee.setCustomer(customer);
        Employee savedEmployee = employeeService.create(employee);
        return new ResponseEntity<>(employeeMapper.toResponseDto(savedEmployee), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return ResponseEntity.ok(employeeMapper.toResponseDto(employee));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getAll() {
        List<Employee> employees = employeeService.getAll();
        return ResponseEntity.ok(employees.stream()
                .map(employeeMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<EmployeeResponseDto>> getByCustomerId(@PathVariable Long customerId) {
        List<Employee> employees = employeeService.getByCustomerId(customerId);
        return ResponseEntity.ok(employees.stream()
                .map(employeeMapper::toResponseDto)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        if (!PHONE_PATTERN.matcher(dto.getPhoneNumber()).matches()) {
            throw new InvalidPhoneNumberException("Phone number must be 10-15 digits, optionally starting with +");
        }
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = employeeMapper.toEntity(dto);
        employee.setCustomer(customer);
        Employee updatedEmployee = employeeService.update(id, employee);
        return ResponseEntity.ok(employeeMapper.toResponseDto(updatedEmployee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.CustomerRequestDto;
import com.matvey.objectaccountingservice.dto.response.CustomerResponseDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.mapper.CustomerMapper;
import com.matvey.objectaccountingservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> create(@Valid @RequestBody CustomerRequestDto dto) {
        Customer customer = customerMapper.toEntity(dto);
        Customer savedCustomer = customerService.create(customer);
        return new ResponseEntity<>(customerMapper.toResponseDto(savedCustomer), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        return ResponseEntity.ok(customerMapper.toResponseDto(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAll() {
        List<Customer> customers = customerService.getAll();
        return ResponseEntity.ok(customers.stream()
                .map(customerMapper::toResponseDto)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDto dto) {
        Customer customer = customerMapper.toEntity(dto);
        Customer updatedCustomer = customerService.update(id, customer);
        return ResponseEntity.ok(customerMapper.toResponseDto(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

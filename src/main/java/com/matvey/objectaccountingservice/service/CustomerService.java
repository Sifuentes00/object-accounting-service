package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer create(Customer customer) {
        log.info("Creating customer with name: {}", customer.getName());
        if (customerRepository.existsByName(customer.getName())) {
            throw new DuplicateResourceException("Customer", "name", customer.getName());
        }
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with id: {}", savedCustomer.getId());
        return savedCustomer;
    }

    public Customer getById(Long id) {
        log.info("Getting customer by id: {}", id);
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    public List<Customer> getAll() {
        log.info("Getting all customers");
        return customerRepository.findAll();
    }

    @Transactional
    public Customer update(Long id, Customer customer) {
        log.info("Updating customer with id: {}", id);
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (!existingCustomer.getName().equals(customer.getName()) 
                && customerRepository.existsByName(customer.getName())) {
            throw new DuplicateResourceException("Customer", "name", customer.getName());
        }

        existingCustomer.setName(customer.getName());
        existingCustomer.setLegalAddress(customer.getLegalAddress());
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated with id: {}", updatedCustomer.getId());
        return updatedCustomer;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting customer with id: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        if (!customer.getObjects().isEmpty()) {
            throw new BusinessLogicException("Cannot delete customer with existing objects");
        }

        if (!customer.getEmployees().isEmpty()) {
            throw new BusinessLogicException("Cannot delete customer with existing employees");
        }

        customerRepository.delete(customer);
        log.info("Customer deleted with id: {}", id);
    }
}

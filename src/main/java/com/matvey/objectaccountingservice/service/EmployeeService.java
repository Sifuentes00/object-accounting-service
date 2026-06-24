package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    public Employee create(Employee employee) {
        log.info("Creating employee with full name: {}", employee.getFullName());

        Customer customer = customerRepository.findById(employee.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", employee.getCustomer().getId()));

        boolean phoneExists = employeeRepository.findAll().stream()
                .filter(e -> e.getCustomer().getId().equals(customer.getId()))
                .anyMatch(e -> e.getPhoneNumber().equals(employee.getPhoneNumber()));

        if (phoneExists) {
            throw new BusinessLogicException("Employee with phone number " + employee.getPhoneNumber() + " already exists for this customer");
        }

        employee.setCustomer(customer);
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created with id: {}", savedEmployee.getId());
        return savedEmployee;
    }

    public Employee getById(Long id) {
        log.info("Getting employee by id: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public List<Employee> getAll() {
        log.info("Getting all employees");
        return employeeRepository.findAll();
    }

    public List<Employee> getByCustomerId(Long customerId) {
        log.info("Getting employees by customer id: {}", customerId);
        return employeeRepository.findAll().stream()
                .filter(e -> e.getCustomer().getId().equals(customerId))
                .toList();
    }

    @Transactional
    public Employee update(Long id, Employee employee) {
        log.info("Updating employee with id: {}", id);
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        Customer customer = customerRepository.findById(employee.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", employee.getCustomer().getId()));

        if (!existingEmployee.getPhoneNumber().equals(employee.getPhoneNumber())) {
            boolean phoneExists = employeeRepository.findAll().stream()
                    .filter(e -> e.getCustomer().getId().equals(customer.getId()) && !e.getId().equals(id))
                    .anyMatch(e -> e.getPhoneNumber().equals(employee.getPhoneNumber()));

            if (phoneExists) {
                throw new BusinessLogicException("Employee with phone number " + employee.getPhoneNumber() + " already exists for this customer");
            }
        }

        existingEmployee.setPhoneNumber(employee.getPhoneNumber());
        existingEmployee.setFullName(employee.getFullName());
        existingEmployee.setPosition(employee.getPosition());
        existingEmployee.setCustomer(customer);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated with id: {}", updatedEmployee.getId());
        return updatedEmployee;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        if (!employee.getPprs().isEmpty()) {
            throw new BusinessLogicException("Cannot delete employee with existing PPRs");
        }

        employeeRepository.delete(employee);
        log.info("Employee deleted with id: {}", id);
    }
}

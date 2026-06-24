package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.CustomerRepository;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectService {

    private final ObjectRepository objectRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public Object create(Object object) {
        log.info("Creating object with name: {}", object.getName());

        Customer customer = customerRepository.findById(object.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", object.getCustomer().getId()));

        if (object.getResponsibleEmployee() != null) {
            Employee employee = employeeRepository.findById(object.getResponsibleEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", object.getResponsibleEmployee().getId()));
            object.setResponsibleEmployee(employee);
        }

        object.setCustomer(customer);
        Object savedObject = objectRepository.save(object);
        log.info("Object created with id: {}", savedObject.getId());
        return savedObject;
    }

    public Object getById(Long id) {
        log.info("Getting object by id: {}", id);
        return objectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object", id));
    }

    public List<Object> getAll() {
        log.info("Getting all objects");
        return objectRepository.findAll();
    }

    public List<Object> getByCustomerId(Long customerId) {
        log.info("Getting objects by customer id: {}", customerId);
        return objectRepository.findByCustomerId(customerId);
    }

    public List<Object> getByStatus(String status) {
        log.info("Getting objects by status: {}", status);
        return objectRepository.findByStatus(status);
    }

    public List<Object> getByWorkType(WorkType workType) {
        log.info("Getting objects by work type: {}", workType);
        return objectRepository.findByWorkType(workType);
    }

    @Transactional
    public Object update(Long id, Object object) {
        log.info("Updating object with id: {}", id);
        Object existingObject = objectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object", id));

        Customer customer = customerRepository.findById(object.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", object.getCustomer().getId()));

        if (object.getResponsibleEmployee() != null) {
            Employee employee = employeeRepository.findById(object.getResponsibleEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", object.getResponsibleEmployee().getId()));
            existingObject.setResponsibleEmployee(employee);
        } else {
            existingObject.setResponsibleEmployee(null);
        }

        existingObject.setStatus(object.getStatus());
        existingObject.setName(object.getName());
        existingObject.setAddress(object.getAddress());
        existingObject.setWorkType(object.getWorkType());
        existingObject.setImageUniqueName(object.getImageUniqueName());
        existingObject.setCustomer(customer);
        Object updatedObject = objectRepository.save(existingObject);
        log.info("Object updated with id: {}", updatedObject.getId());
        return updatedObject;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting object with id: {}", id);
        Object object = objectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Object", id));

        if (object.getContract() != null) {
            throw new BusinessLogicException("Cannot delete object with existing contract");
        }

        if (!object.getPprs().isEmpty()) {
            throw new BusinessLogicException("Cannot delete object with existing PPRs");
        }

        objectRepository.delete(object);
        log.info("Object deleted with id: {}", id);
    }
}

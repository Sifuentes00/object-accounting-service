package com.matvey.objectaccountingservice.repository;

import com.matvey.objectaccountingservice.entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @EntityGraph(attributePaths = {"customer"})
    List<Employee> findAll();

    List<Employee> findByCustomerId(Long customerId);
    List<Employee> findByFullNameContainingIgnoreCase(String fullName);
    List<Employee> findByPosition(String position);
    List<Employee> findByCustomerIdAndPosition(Long customerId, String position);
}

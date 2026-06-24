package com.matvey.objectaccountingservice.repository;

import com.matvey.objectaccountingservice.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByObjectId(Long objectId);
    List<Employee> findByFullNameContainingIgnoreCase(String fullName);
    List<Employee> findByPosition(String position);
    List<Employee> findByObjectIdAndPosition(Long objectId, String position);
}

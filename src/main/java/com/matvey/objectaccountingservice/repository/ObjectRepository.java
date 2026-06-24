package com.matvey.objectaccountingservice.repository;

import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.enums.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectRepository extends JpaRepository<Object, Long> {
    List<Object> findByCustomerId(Long customerId);
    List<Object> findByStatus(String status);
    List<Object> findByWorkType(WorkType workType);
    List<Object> findByCustomerIdAndStatus(Long customerId, String status);
    
    @Query("SELECT o FROM Object o WHERE o.customer.id = :customerId AND o.workType = :workType")
    List<Object> findByCustomerIdAndWorkType(@Param("customerId") Long customerId, @Param("workType") WorkType workType);
}

package com.matvey.objectaccountingservice.repository;

import com.matvey.objectaccountingservice.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByObjectId(Long objectId);
    Optional<Contract> findByNumber(String number);
    boolean existsByNumber(String number);
}

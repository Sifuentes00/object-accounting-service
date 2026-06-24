package com.matvey.objectaccountingservice.repository;

import com.matvey.objectaccountingservice.entity.Ppr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PprRepository extends JpaRepository<Ppr, Long> {
    List<Ppr> findByObjectId(Long objectId);
    Optional<Ppr> findByNumber(String number);
    boolean existsByNumber(String number);
    List<Ppr> findByArchiveNumber(String archiveNumber);
}

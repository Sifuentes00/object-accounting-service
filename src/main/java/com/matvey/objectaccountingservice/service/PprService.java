package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.repository.PprRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PprService {

    private final PprRepository pprRepository;
    private final ObjectRepository objectRepository;
    private final EmployeeRepository employeeRepository;

    public Ppr create(Ppr ppr) {
        log.info("Creating ppr with number: {}", ppr.getNumber());

        if (pprRepository.existsByNumber(ppr.getNumber())) {
            throw new DuplicateResourceException("Ppr", "number", ppr.getNumber());
        }

        Object object = objectRepository.findById(ppr.getObject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Object", ppr.getObject().getId()));

        if (ppr.getEmployee() != null) {
            Employee employee = employeeRepository.findById(ppr.getEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", ppr.getEmployee().getId()));
            ppr.setEmployee(employee);
        }

        ppr.setObject(object);
        Ppr savedPpr = pprRepository.save(ppr);
        log.info("Ppr created with id: {}", savedPpr.getId());
        return savedPpr;
    }

    public Ppr getById(Long id) {
        log.info("Getting ppr by id: {}", id);
        return pprRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppr", id));
    }

    public List<Ppr> getAll() {
        log.info("Getting all pprs");
        return pprRepository.findAll();
    }

    public List<Ppr> getByObjectId(Long objectId) {
        log.info("Getting pprs by object id: {}", objectId);
        return pprRepository.findByObjectId(objectId);
    }

    public Ppr getByNumber(String number) {
        log.info("Getting ppr by number: {}", number);
        return pprRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Ppr not found with number: " + number));
    }

    public List<Ppr> getByArchiveNumber(String archiveNumber) {
        log.info("Getting pprs by archive number: {}", archiveNumber);
        return pprRepository.findByArchiveNumber(archiveNumber);
    }

    @Transactional
    public Ppr update(Long id, Ppr ppr) {
        log.info("Updating ppr with id: {}", id);
        Ppr existingPpr = pprRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppr", id));

        if (!existingPpr.getNumber().equals(ppr.getNumber()) 
                && pprRepository.existsByNumber(ppr.getNumber())) {
            throw new DuplicateResourceException("Ppr", "number", ppr.getNumber());
        }

        Object object = objectRepository.findById(ppr.getObject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Object", ppr.getObject().getId()));

        if (ppr.getEmployee() != null) {
            Employee employee = employeeRepository.findById(ppr.getEmployee().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", ppr.getEmployee().getId()));
            existingPpr.setEmployee(employee);
        } else {
            existingPpr.setEmployee(null);
        }

        existingPpr.setName(ppr.getName());
        existingPpr.setArchiveNumber(ppr.getArchiveNumber());
        existingPpr.setNumber(ppr.getNumber());
        existingPpr.setFileUniqueName(ppr.getFileUniqueName());
        existingPpr.setObject(object);
        Ppr updatedPpr = pprRepository.save(existingPpr);
        log.info("Ppr updated with id: {}", updatedPpr.getId());
        return updatedPpr;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting ppr with id: {}", id);
        Ppr ppr = pprRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ppr", id));
        pprRepository.delete(ppr);
        log.info("Ppr deleted with id: {}", id);
    }
}

package com.matvey.objectaccountingservice.service;

import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.exception.BusinessLogicException;
import com.matvey.objectaccountingservice.exception.DuplicateResourceException;
import com.matvey.objectaccountingservice.exception.ResourceNotFoundException;
import com.matvey.objectaccountingservice.repository.ContractRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final ObjectRepository objectRepository;
    private final StorageService storageService;

    public Contract create(Contract contract) {
        log.info("Creating contract with number: {}", contract.getNumber());

        if (contractRepository.existsByNumber(contract.getNumber())) {
            throw new DuplicateResourceException("Contract", "number", contract.getNumber());
        }

        Object object = objectRepository.findById(contract.getObject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Object", contract.getObject().getId()));

        if (object.getContract() != null) {
            throw new BusinessLogicException("Object already has a contract");
        }

        contract.setObject(object);
        Contract savedContract = contractRepository.save(contract);
        log.info("Contract created with id: {}", savedContract.getId());
        return savedContract;
    }

    public Contract getById(Long id) {
        log.info("Getting contract by id: {}", id);
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
    }

    public List<Contract> getAll() {
        log.info("Getting all contracts");
        return contractRepository.findAll();
    }

    public List<Contract> getByObjectId(Long objectId) {
        log.info("Getting contracts by object id: {}", objectId);
        return contractRepository.findByObjectId(objectId);
    }

    public Contract getByNumber(String number) {
        log.info("Getting contract by number: {}", number);
        return contractRepository.findByNumber(number)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with number: " + number));
    }

    @Transactional
    public Contract update(Long id, Contract contract) {
        log.info("Updating contract with id: {}", id);
        Contract existingContract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));

        if (!existingContract.getNumber().equals(contract.getNumber())
                && contractRepository.existsByNumber(contract.getNumber())) {
            throw new DuplicateResourceException("Contract", "number", contract.getNumber());
        }

        Object object = objectRepository.findById(contract.getObject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Object", contract.getObject().getId()));

        if (!existingContract.getObject().getId().equals(object.getId()) && object.getContract() != null) {
            throw new BusinessLogicException("Object already has a contract");
        }

        String oldFileName = existingContract.getFileUniqueName();
        String newFileName = contract.getFileUniqueName();

        existingContract.setConclusionDate(contract.getConclusionDate());
        existingContract.setEndDate(contract.getEndDate());
        existingContract.setNumber(contract.getNumber());
        existingContract.setFileUniqueName(newFileName);
        existingContract.setObject(object);
        Contract updatedContract = contractRepository.save(existingContract);

        if (oldFileName != null && !oldFileName.equals(newFileName)) {
            try {
                storageService.deleteContract(oldFileName);
                log.info("Old file deleted from MinIO: {}", oldFileName);
            } catch (Exception e) {
                log.error("Failed to delete old file from MinIO: {}", oldFileName, e);
            }
        }

        log.info("Contract updated with id: {}", updatedContract.getId());
        return updatedContract;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting contract with id: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        
        if (contract.getFileUniqueName() != null) {
            try {
                storageService.deleteContract(contract.getFileUniqueName());
                log.info("File deleted from MinIO: {}", contract.getFileUniqueName());
            } catch (Exception e) {
                log.error("Failed to delete file from MinIO: {}", contract.getFileUniqueName(), e);
            }
        }
        
        contractRepository.delete(contract);
        log.info("Contract deleted with id: {}", id);
    }
}

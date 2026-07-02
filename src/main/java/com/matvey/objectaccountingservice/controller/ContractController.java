package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.ContractRequestDto;
import com.matvey.objectaccountingservice.dto.response.ContractResponseDto;
import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.exception.InvalidDateException;
import com.matvey.objectaccountingservice.mapper.ContractMapper;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.service.ContractService;
import com.matvey.objectaccountingservice.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper contractMapper;
    private final ObjectRepository objectRepository;
    private final StorageService storageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDto> create(@Valid @RequestBody ContractRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getConclusionDate())) {
            throw new InvalidDateException("Дата окончания не может быть раньше даты заключения");
        }
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Contract contract = contractMapper.toEntity(dto);
        contract.setObject(object);
        Contract savedContract = contractService.create(contract);
        return new ResponseEntity<>(contractMapper.toResponseDto(savedContract), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ContractResponseDto> getById(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        return ResponseEntity.ok(contractMapper.toResponseDto(contract));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ContractResponseDto>> getAll() {
        List<Contract> contracts = contractService.getAll();
        return ResponseEntity.ok(contracts.stream()
                .map(contractMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/object/{objectId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ContractResponseDto>> getByObjectId(@PathVariable Long objectId) {
        List<Contract> contracts = contractService.getByObjectId(objectId);
        return ResponseEntity.ok(contracts.stream()
                .map(contractMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ContractResponseDto> getByNumber(@PathVariable String number) {
        Contract contract = contractService.getByNumber(number);
        return ResponseEntity.ok(contractMapper.toResponseDto(contract));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDto> update(@PathVariable Long id, @Valid @RequestBody ContractRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getConclusionDate())) {
            throw new InvalidDateException("Дата окончания не может быть раньше даты заключения");
        }
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Contract contract = contractMapper.toEntity(dto);
        contract.setObject(object);
        Contract updatedContract = contractService.update(id, contract);
        return ResponseEntity.ok(contractMapper.toResponseDto(updatedContract));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDto> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Contract contract = contractService.getById(id);
            String fileName = storageService.uploadContract(file);
            contract.setFileUniqueName(fileName);
            Contract updatedContract = contractService.update(id, contract);
            return ResponseEntity.ok(contractMapper.toResponseDto(updatedContract));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить файл: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContractResponseDto> replaceFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Contract contract = contractService.getById(id);
            if (contract.getFileUniqueName() != null) {
                storageService.replaceContract(contract.getFileUniqueName(), file);
            } else {
                String fileName = storageService.uploadContract(file);
                contract.setFileUniqueName(fileName);
            }
            Contract updatedContract = contractService.update(id, contract);
            return ResponseEntity.ok(contractMapper.toResponseDto(updatedContract));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось заменить файл: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            Contract contract = contractService.getById(id);
            if (contract.getFileUniqueName() == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] fileData = storageService.downloadContract(contract.getFileUniqueName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", contract.getFileUniqueName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось скачать файл: " + e.getMessage(), e);
        }
    }
}

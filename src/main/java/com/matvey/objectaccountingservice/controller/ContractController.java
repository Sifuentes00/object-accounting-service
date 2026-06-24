package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.ContractRequestDto;
import com.matvey.objectaccountingservice.dto.response.ContractResponseDto;
import com.matvey.objectaccountingservice.entity.Contract;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.mapper.ContractMapper;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper contractMapper;
    private final ObjectRepository objectRepository;

    @PostMapping
    public ResponseEntity<ContractResponseDto> create(@Valid @RequestBody ContractRequestDto dto) {
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Contract contract = contractMapper.toEntity(dto);
        contract.setObject(object);
        Contract savedContract = contractService.create(contract);
        return new ResponseEntity<>(contractMapper.toResponseDto(savedContract), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDto> getById(@PathVariable Long id) {
        Contract contract = contractService.getById(id);
        return ResponseEntity.ok(contractMapper.toResponseDto(contract));
    }

    @GetMapping
    public ResponseEntity<List<ContractResponseDto>> getAll() {
        List<Contract> contracts = contractService.getAll();
        return ResponseEntity.ok(contracts.stream()
                .map(contractMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/object/{objectId}")
    public ResponseEntity<List<ContractResponseDto>> getByObjectId(@PathVariable Long objectId) {
        List<Contract> contracts = contractService.getByObjectId(objectId);
        return ResponseEntity.ok(contracts.stream()
                .map(contractMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<ContractResponseDto> getByNumber(@PathVariable String number) {
        Contract contract = contractService.getByNumber(number);
        return ResponseEntity.ok(contractMapper.toResponseDto(contract));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractResponseDto> update(@PathVariable Long id, @Valid @RequestBody ContractRequestDto dto) {
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Contract contract = contractMapper.toEntity(dto);
        contract.setObject(object);
        Contract updatedContract = contractService.update(id, contract);
        return ResponseEntity.ok(contractMapper.toResponseDto(updatedContract));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

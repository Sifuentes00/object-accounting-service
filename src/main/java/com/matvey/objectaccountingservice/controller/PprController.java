package com.matvey.objectaccountingservice.controller;

import com.matvey.objectaccountingservice.dto.request.PprRequestDto;
import com.matvey.objectaccountingservice.dto.response.PprResponseDto;
import com.matvey.objectaccountingservice.entity.Employee;
import com.matvey.objectaccountingservice.entity.Object;
import com.matvey.objectaccountingservice.entity.Ppr;
import com.matvey.objectaccountingservice.mapper.PprMapper;
import com.matvey.objectaccountingservice.repository.EmployeeRepository;
import com.matvey.objectaccountingservice.repository.ObjectRepository;
import com.matvey.objectaccountingservice.service.PprService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pprs")
@RequiredArgsConstructor
public class PprController {

    private final PprService pprService;
    private final PprMapper pprMapper;
    private final ObjectRepository objectRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PprResponseDto> create(@Valid @RequestBody PprRequestDto dto) {
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Ppr ppr = pprMapper.toEntity(dto);
        ppr.setObject(object);
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            ppr.setEmployee(employee);
        }
        Ppr savedPpr = pprService.create(ppr);
        return new ResponseEntity<>(pprMapper.toResponseDto(savedPpr), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PprResponseDto> getById(@PathVariable Long id) {
        Ppr ppr = pprService.getById(id);
        return ResponseEntity.ok(pprMapper.toResponseDto(ppr));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PprResponseDto>> getAll() {
        List<Ppr> pprs = pprService.getAll();
        return ResponseEntity.ok(pprs.stream()
                .map(pprMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/object/{objectId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PprResponseDto>> getByObjectId(@PathVariable Long objectId) {
        List<Ppr> pprs = pprService.getByObjectId(objectId);
        return ResponseEntity.ok(pprs.stream()
                .map(pprMapper::toResponseDto)
                .toList());
    }

    @GetMapping("/number/{number}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PprResponseDto> getByNumber(@PathVariable String number) {
        Ppr ppr = pprService.getByNumber(number);
        return ResponseEntity.ok(pprMapper.toResponseDto(ppr));
    }

    @GetMapping("/archive-number/{archiveNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PprResponseDto>> getByArchiveNumber(@PathVariable String archiveNumber) {
        List<Ppr> pprs = pprService.getByArchiveNumber(archiveNumber);
        return ResponseEntity.ok(pprs.stream()
                .map(pprMapper::toResponseDto)
                .toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PprResponseDto> update(@PathVariable Long id, @Valid @RequestBody PprRequestDto dto) {
        Object object = objectRepository.findById(dto.getObjectId())
                .orElseThrow(() -> new RuntimeException("Object not found"));
        Ppr ppr = pprMapper.toEntity(dto);
        ppr.setObject(object);
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            ppr.setEmployee(employee);
        }
        Ppr updatedPpr = pprService.update(id, ppr);
        return ResponseEntity.ok(pprMapper.toResponseDto(updatedPpr));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pprService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

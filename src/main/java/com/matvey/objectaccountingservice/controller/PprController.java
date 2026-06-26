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

import java.util.List;

@RestController
@RequestMapping("/api/v1/pprs")
@RequiredArgsConstructor
public class PprController {

    private final PprService pprService;
    private final PprMapper pprMapper;
    private final ObjectRepository objectRepository;
    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

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

    @PostMapping("/{id}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PprResponseDto> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Ppr ppr = pprService.getById(id);
            String fileName = storageService.uploadPpr(file);
            ppr.setFileUniqueName(fileName);
            Ppr updatedPpr = pprService.update(id, ppr);
            return ResponseEntity.ok(pprMapper.toResponseDto(updatedPpr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PprResponseDto> replaceFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Ppr ppr = pprService.getById(id);
            if (ppr.getFileUniqueName() != null) {
                storageService.replacePpr(ppr.getFileUniqueName(), file);
            } else {
                String fileName = storageService.uploadPpr(file);
                ppr.setFileUniqueName(fileName);
            }
            Ppr updatedPpr = pprService.update(id, ppr);
            return ResponseEntity.ok(pprMapper.toResponseDto(updatedPpr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace file: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            Ppr ppr = pprService.getById(id);
            if (ppr.getFileUniqueName() == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] fileData = storageService.downloadPpr(ppr.getFileUniqueName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", ppr.getFileUniqueName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }
}

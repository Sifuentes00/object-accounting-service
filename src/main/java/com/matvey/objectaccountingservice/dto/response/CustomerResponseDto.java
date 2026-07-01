package com.matvey.objectaccountingservice.dto.response;

import com.matvey.objectaccountingservice.dto.response.EmployeeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {

    private Long id;
    private String name;
    private String legalAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private List<EmployeeResponseDto> employees;
}

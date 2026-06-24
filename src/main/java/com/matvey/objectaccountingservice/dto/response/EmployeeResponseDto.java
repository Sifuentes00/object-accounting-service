package com.matvey.objectaccountingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {

    private Long id;
    private String phoneNumber;
    private String fullName;
    private String position;
    private Long customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

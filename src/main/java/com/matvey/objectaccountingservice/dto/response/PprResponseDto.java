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
public class PprResponseDto {

    private Long id;
    private String name;
    private String archiveNumber;
    private String number;
    private String fileUniqueName;
    private Long objectId;
    private Long employeeId;
    private String employeeFullName;
    private String employeePosition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

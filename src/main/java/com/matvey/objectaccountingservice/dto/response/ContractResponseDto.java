package com.matvey.objectaccountingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDto {

    private Long id;
    private LocalDate conclusionDate;
    private LocalDate endDate;
    private String number;
    private String fileUniqueName;
    private Long objectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

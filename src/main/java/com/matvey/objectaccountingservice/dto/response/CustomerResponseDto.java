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
public class CustomerResponseDto {

    private Long id;
    private String name;
    private String legalAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

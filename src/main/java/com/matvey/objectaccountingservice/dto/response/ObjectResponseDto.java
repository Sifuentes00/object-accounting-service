package com.matvey.objectaccountingservice.dto.response;

import com.matvey.objectaccountingservice.enums.WorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectResponseDto {

    private Long id;
    private String status;
    private String name;
    private String address;
    private WorkType workType;
    private String imageUniqueName;
    private Long customerId;
    private Long responsibleEmployeeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}

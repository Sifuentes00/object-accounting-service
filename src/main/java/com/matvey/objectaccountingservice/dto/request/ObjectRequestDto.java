package com.matvey.objectaccountingservice.dto.request;

import com.matvey.objectaccountingservice.enums.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectRequestDto {

    private String status;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "Work type is required")
    private WorkType workType;

    private String imageUniqueName;

    @NotNull(message = "Customer id is required")
    private Long customerId;

    private Long responsibleEmployeeId;
}

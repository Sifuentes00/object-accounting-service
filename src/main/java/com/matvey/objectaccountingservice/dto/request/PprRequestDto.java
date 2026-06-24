package com.matvey.objectaccountingservice.dto.request;

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
public class PprRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Archive number is required")
    @Size(max = 100, message = "Archive number must not exceed 100 characters")
    private String archiveNumber;

    @NotBlank(message = "Number is required")
    @Size(max = 100, message = "Number must not exceed 100 characters")
    private String number;

    @NotBlank(message = "File unique name is required")
    @Size(max = 255, message = "File unique name must not exceed 255 characters")
    private String fileUniqueName;

    @NotNull(message = "Object id is required")
    private Long objectId;

    private Long employeeId;
}

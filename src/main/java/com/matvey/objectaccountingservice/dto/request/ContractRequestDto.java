package com.matvey.objectaccountingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequestDto {

    @NotNull(message = "Conclusion date is required")
    private LocalDate conclusionDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Number is required")
    @Size(max = 100, message = "Number must not exceed 100 characters")
    private String number;

    @NotBlank(message = "File unique name is required")
    @Size(max = 255, message = "File unique name must not exceed 255 characters")
    private String fileUniqueName;

    @NotNull(message = "Object id is required")
    private Long objectId;
}

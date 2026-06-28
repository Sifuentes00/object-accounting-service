package com.matvey.objectaccountingservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[А-ЯЁ][а-яё]*$", message = "First name must start with capital letter and contain only Cyrillic letters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[А-ЯЁ][а-яё]*$", message = "Last name must start with capital letter and contain only Cyrillic letters")
    private String lastName;
}

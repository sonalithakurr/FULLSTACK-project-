package com.cineverse.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    // Accepts either email or username
    @NotBlank(message = "Identifier (email or username) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}

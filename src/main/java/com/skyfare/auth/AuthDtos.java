package com.skyfare.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record Credentials(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 100) String password) { }
    public record TokenResponse(String token, String email) { }
}

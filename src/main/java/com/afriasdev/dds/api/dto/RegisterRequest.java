package com.afriasdev.dds.api.dto;

import com.afriasdev.dds.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 6)
    private String password;

    @NotNull
    private Role role;

    private String phone;

    private String bloodType;
}

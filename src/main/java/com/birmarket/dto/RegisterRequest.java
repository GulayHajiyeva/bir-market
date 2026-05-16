package com.birmarket.dto;

import com.birmarket.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
            message = "Şifrədə ən az 1 böyük hərf, 1 kiçik hərf və 1 rəqəm olmalıdır"
    )
    private String password;

    @NotBlank(message = "Telefon boş ola bilməz")
    @Pattern(
            regexp = "^(\\+994|0)(50|51|55|70|77|99)\\d{7}$",
            message = "Telefon nömrəsi düzgün formatda deyil"
    )
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;
}

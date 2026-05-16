package com.birmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Pattern(
            regexp = "\\d{16}",
            message = "Card number must contain exactly 16 digits"
    )
    private String cardNumber;

    @NotBlank(message = "Expiry month is required")
    private String expireMonth;

    @NotBlank(message = "Expiry year is required")
    private String expireYear;

    @NotBlank(message = "CVC is required")
    @Size(min = 3, max = 3, message = "CVC uzunlugu 3 olmalidir")
    private String cvc;
}

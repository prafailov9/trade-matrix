package com.ntros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class AccountDTO {

    // User's first and last names
    @NotBlank(message = "Must have a Beneficiary of the Account.")
    private String accountOwner;

    @Pattern(regexp = "\\d+", message = "AN must be a number.")
    @Size(min = 8, max = 12, message = "Invalid AN: must be 8 - 13 digits.")
    private String accountNumber;

    @NotBlank(message = "Must have a name for the Account.")
    private String accountName;

    private BigDecimal totalBalance;

    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

    private List<WalletDTO> wallets;


    /**
     * @NotBlank(message = "Invalid Name: Empty name")
     *     @NotNull(message = "Invalid Name: Name is NULL")
     *     @Size(min = 3, max = 30, message = "Invalid Name: Must be of 3 - 30 characters")
     *     String name;
     *     @Email(message = "Invalid email")
     *     String email;
     *     @NotBlank(message = "Invalid Phone number: Empty number")
     *     @NotNull(message = "Invalid Phone number: Number is NULL")
     *     @Pattern(regexp = "^\\d{10}$", message = "Invalid phone number")
     *     String mobile;
     *     @Min(value = 1, message = "Invalid Age: Equals to zero or Less than zero")
     *     @Max(value = 100, message = "Invalid Age: Exceeds 100 years")
     *     Integer age;
     */

}

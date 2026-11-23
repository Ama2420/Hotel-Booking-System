package com.rupeek.hotelbooking.api.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterOwnerRequest(@NotBlank String name, @NotBlank @Email String email) {
}

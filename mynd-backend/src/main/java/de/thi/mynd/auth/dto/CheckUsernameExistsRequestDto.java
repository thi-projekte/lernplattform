package de.thi.mynd.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CheckUsernameExistsRequestDto {

    @NotBlank
    @Size(max = 64, min = 5)
    public String username;
}

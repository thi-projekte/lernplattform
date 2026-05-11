package de.thi.mynd.auth.dto;

import de.thi.mynd.auth.RegisterRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class RegisterUserRequestDto {

  @NotBlank
  @Size(max = 64, min = 5)
  public String username;

  @NotBlank public String password;

  public String firstName;

  public String lastName;

  @Email @NotBlank public String email;

  @NotNull public RegisterRole role;
}

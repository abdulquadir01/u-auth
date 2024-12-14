package dev.aq.uauth.dto;


import dev.aq.uauth.constants.Role;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

  @NotNull
  @NotBlank
  @Min(1)
  @Max(60)
  private String firstName;
  @Nullable
  private String lastName;
  @Email
  @NotNull
  private String email;
  @NotNull
  @Min(8)
  @Max(32)
  private String password;
  private Role role;

}

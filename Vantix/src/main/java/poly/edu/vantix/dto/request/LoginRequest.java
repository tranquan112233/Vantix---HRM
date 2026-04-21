package poly.edu.vantix.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @JsonAlias({"username", "email", "login"})
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}

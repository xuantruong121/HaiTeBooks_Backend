package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phone;
    private String address;
    private String password;
    private String roleName; // e.g. "USER", "ADMIN"
    private Boolean enabled; // Trạng thái kích hoạt tài khoản (true/false)
}

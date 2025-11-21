package iuh.fit.haitebooks_backend.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}


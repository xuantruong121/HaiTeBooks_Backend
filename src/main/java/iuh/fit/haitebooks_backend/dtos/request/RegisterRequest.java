package iuh.fit.haitebooks_backend.dtos.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String address;
    private String role; // "ADMIN" | "USER" | "SELLER"
    private String phone;
}

package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.dtos.response.UserResponse;
import iuh.fit.haitebooks_backend.model.Role;
import iuh.fit.haitebooks_backend.model.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null) return null;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getAddress(),
                user.getRole() != null ? user.getRole().getName() : null
        );
    }

    public static User toEntity(UserRequest request, Role role, String encodedPassword) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setPassword(encodedPassword);
        user.setRole(role);
        return user;
    }

    public static void updateEntity(User user, UserRequest request, Role role, String encodedPassword) {
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (encodedPassword != null && !encodedPassword.isBlank()) user.setPassword(encodedPassword);
        if (role != null) user.setRole(role);
        // ✅ Cập nhật enabled status nếu có trong request
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
    }
}

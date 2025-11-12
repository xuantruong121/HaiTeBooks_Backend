package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.dtos.response.UserResponse;
import iuh.fit.haitebooks_backend.mapper.UserMapper;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Lấy tất cả user
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy user theo id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    // ✅ Tạo user mới
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    // ✅ Cập nhật user
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User updated = userService.updateUser(id, request);
        return ResponseEntity.ok(UserMapper.toResponse(updated));
    }

    // ✅ Xóa user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Lấy user hiện tại
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    // ✅ Cập nhật user hiện tại
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequest request) {

        User current = userService.getByUsername(userDetails.getUsername());
        User updated = userService.updateUser(current.getId(), request);
        return ResponseEntity.ok(UserMapper.toResponse(updated));
    }
}

package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.model.Role;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.RoleRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.setRole(role);

        return userRepository.save(user);
    }

    // Cập nhật thông tin người dùng
    public User updateUser(Long id, User data) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));

        if (data.getUsername() != null) existing.setUsername(data.getUsername());
        if (data.getEmail() != null) existing.setEmail(data.getEmail());
        if (data.getFullName() != null) existing.setFullName(data.getFullName());
        if (data.getAddress() != null) existing.setAddress(data.getAddress());
        if (data.getRole() != null) existing.setRole(data.getRole());
        if (data.getPassword() != null && !data.getPassword().isBlank())
            existing.setPassword(passwordEncoder.encode(data.getPassword()));

        return userRepository.save(existing);
    }

    // Xoá người dùng
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }
}

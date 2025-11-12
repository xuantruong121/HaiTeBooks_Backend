package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.mapper.UserMapper;
import iuh.fit.haitebooks_backend.model.Role;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.RoleRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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

    // ✅ Lấy tất cả người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Đăng ký người dùng (dành cho AuthController)
    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Tìm role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        // Mã hoá mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // ✅ Lấy người dùng theo ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));
    }

    // ✅ Tạo người dùng mới (đăng ký)
    public User createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, role, encodedPassword);
        return userRepository.save(user);
    }

    // ✅ Cập nhật người dùng (admin hoặc chính họ)
    public User updateUser(Long id, UserRequest request) {
        User existing = getUserById(id);

        // Kiểm tra trùng email hoặc username
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(id)) throw new RuntimeException("Username already exists");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(id)) throw new RuntimeException("Email already exists");
        });

        Role role = null;
        if (request.getRoleName() != null) {
            role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));
        }

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        UserMapper.updateEntity(existing, request, role, encodedPassword);
        return userRepository.save(existing);
    }

    // ✅ Xóa người dùng
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    // ✅ Lấy người dùng theo username (dùng cho /me)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}

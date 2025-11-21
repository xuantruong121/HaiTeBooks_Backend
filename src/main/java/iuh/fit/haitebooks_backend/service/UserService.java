package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.ChangePasswordRequest;
import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.dtos.response.UserResponse;
import iuh.fit.haitebooks_backend.exception.BadRequestException;
import iuh.fit.haitebooks_backend.exception.UnauthorizedException;
import iuh.fit.haitebooks_backend.mapper.UserMapper;
import iuh.fit.haitebooks_backend.model.Role;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.RoleRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Map trong transaction để đảm bảo lazy relationships được load
        return users.stream()
                .map(user -> {
                    loadLazyRelationships(user);
                    return UserMapper.toResponse(user);
                })
                .collect(Collectors.toList());
    }

    // ✅ Đăng ký người dùng (dành cho AuthController)
    @Transactional
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
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(user);
        return UserMapper.toResponse(user);
    }

    // ✅ Tạo người dùng mới (đăng ký)
    @Transactional
    public UserResponse createUser(UserRequest request) {
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
        user = userRepository.save(user);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(user);
        return UserMapper.toResponse(user);
    }

    // ✅ Cập nhật người dùng (admin hoặc chính họ)
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));

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
        existing = userRepository.save(existing);
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(existing);
        return UserMapper.toResponse(existing);
    }

    // ✅ Xóa người dùng
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    // ✅ Lấy người dùng theo username (dùng cho /me)
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Đảm bảo lazy relationships được load trong transaction
        loadLazyRelationships(user);
        return UserMapper.toResponse(user);
    }

    // ✅ Đổi mật khẩu
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));

        // ✅ Xác thực mật khẩu cũ - throw 401 Unauthorized
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Mật khẩu cũ không đúng");
        }

        // ✅ Kiểm tra mật khẩu mới không được trùng với mật khẩu cũ - throw 400 Bad Request
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        // ✅ Mã hóa và cập nhật mật khẩu mới
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    /**
     * Đảm bảo lazy relationships được load trong transaction
     */
    private void loadLazyRelationships(User user) {
        if (user.getRole() != null) {
            user.getRole().getName();
        }
    }
}

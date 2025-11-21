package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.ChangePasswordRequest;
import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.dtos.response.UserResponse;
import iuh.fit.haitebooks_backend.exception.BadRequestException;
import iuh.fit.haitebooks_backend.exception.ConflictException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
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
        // Role đã là EAGER trong entity, không cần trigger load
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ✅ Đăng ký người dùng (dành cho AuthController)
    @Transactional
    public User register(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        // Tìm role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        // Mã hoá mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    // ✅ Lấy người dùng theo ID
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        // Role đã là EAGER trong entity, không cần trigger load
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
        return UserMapper.toResponse(user);
    }

    // ✅ Tạo người dùng mới (đăng ký)
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new NotFoundException("Role not found: " + request.getRoleName()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, role, encodedPassword);
        user = userRepository.save(user);
        
        // Role đã được set trực tiếp, không cần trigger load
        return UserMapper.toResponse(user);
    }

    // ✅ Cập nhật người dùng (admin hoặc chính họ)
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));

        // Kiểm tra trùng email hoặc username
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            if (!u.getId().equals(id)) throw new ConflictException("Username already exists");
        });
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(id)) throw new ConflictException("Email already exists");
        });

        Role role = null;
        if (request.getRoleName() != null) {
            role = roleRepository.findByName(request.getRoleName())
                    .orElseThrow(() -> new NotFoundException("Role not found: " + request.getRoleName()));
        }

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // ✅ Cập nhật enabled status nếu có trong request
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }

        UserMapper.updateEntity(existing, request, role, encodedPassword);
        existing = userRepository.save(existing);
        
        // Role đã là EAGER trong entity, không cần trigger load
        return UserMapper.toResponse(existing);
    }

    // ✅ Xóa người dùng - Tối ưu: Dùng findById().orElseThrow() để tránh 2 queries
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
        userRepository.delete(user);
    }

    // ✅ Lấy người dùng theo username (dùng cho /me)
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        // Role đã là EAGER trong entity, không cần trigger load
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        return UserMapper.toResponse(user);
    }

    // ✅ Đổi mật khẩu
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

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

    // ✅ Vô hiệu hóa tài khoản hiện tại (set enabled = false)
    @Transactional
    public void deactivateCurrentUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));
        
        // ✅ Xác thực mật khẩu trước khi vô hiệu hóa
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Mật khẩu không đúng");
        }
        
        // ✅ Vô hiệu hóa tài khoản (set enabled = false) thay vì xóa
        user.setEnabled(false);
        userRepository.save(user);
    }

}

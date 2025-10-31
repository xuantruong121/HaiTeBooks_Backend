package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.LoginRequest;
import iuh.fit.haitebooks_backend.dtos.request.RegisterRequest;
import iuh.fit.haitebooks_backend.dtos.response.AuthResponse;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.service.UserService;
import iuh.fit.haitebooks_backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        String roleName = req.getRole() != null ? req.getRole() : "USER"; // mặc định là USER
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setAddress(req.getAddress());
        user.setPhone(req.getPhone());
        user.setEnabled(true);
        return ResponseEntity.ok(userService.register(user, roleName));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        // Xác thực username + password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // Lấy thông tin user sau khi xác thực thành công
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Lấy danh sách roles từ user
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Sinh JWT token kèm roles
        final String token = jwtUtil.generateToken(userDetails.getUsername(), roles);

        // Trả về response
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getUsername()));
    }

}
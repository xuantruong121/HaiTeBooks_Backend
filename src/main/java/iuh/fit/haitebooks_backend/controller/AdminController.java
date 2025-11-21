package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.UserRequest;
import iuh.fit.haitebooks_backend.dtos.response.UserResponse;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import iuh.fit.haitebooks_backend.service.StatisticService;
import iuh.fit.haitebooks_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final StatisticService statisticService;
    private final UserService userService;

    public AdminController(UserRepository userRepository, StatisticService statisticService, UserService userService) {
        this.userRepository = userRepository;
        this.statisticService = statisticService;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/statistics/overview")
    public ResponseEntity<?> overview() {
        return ResponseEntity.ok(statisticService.getOverview());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }
}

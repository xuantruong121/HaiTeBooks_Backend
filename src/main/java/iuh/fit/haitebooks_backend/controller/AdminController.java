package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import iuh.fit.haitebooks_backend.service.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final StatisticService statisticService;

    public AdminController(UserRepository userRepository, StatisticService statisticService) {
        this.userRepository = userRepository;
        this.statisticService = statisticService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/statistics/overview")
    public ResponseEntity<?> overview() {
        return ResponseEntity.ok(statisticService.getOverview());
    }
}

package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.response.StatisticResponse;
import iuh.fit.haitebooks_backend.service.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticController {
    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/overview")
    public ResponseEntity<StatisticResponse> getOverview() {
        return ResponseEntity.ok(statisticService.getOverview());
    }
}

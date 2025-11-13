package iuh.fit.haitebooks_backend.controller;

import iuh.fit.haitebooks_backend.dtos.request.PromotionRequest;
import iuh.fit.haitebooks_backend.dtos.response.PromotionResponse;
import iuh.fit.haitebooks_backend.mapper.PromotionMapper;
import iuh.fit.haitebooks_backend.model.Promotion;
import iuh.fit.haitebooks_backend.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // 🔥 Validate promotion before apply
    @GetMapping("/validate/{code}")
    public ResponseEntity<PromotionResponse> validate(@PathVariable String code) {
        Promotion p = promotionService.validatePromotion(code);
        return ResponseEntity.ok(PromotionMapper.toResponse(p));
    }

    // CREATE
    @PostMapping("/create/{creatorId}")
    public ResponseEntity<PromotionResponse> create(
            @RequestBody PromotionRequest req,
            @PathVariable Long creatorId
    ) {
        return ResponseEntity.ok(promotionService.create(req, creatorId));
    }

    // APPROVE
    @PutMapping("/approve/{promotionId}/{adminId}")
    public ResponseEntity<PromotionResponse> approve(
            @PathVariable Long promotionId,
            @PathVariable Long adminId
    ) {
        return ResponseEntity.ok(promotionService.approve(promotionId, adminId));
    }

    // REJECT
    @PutMapping("/reject/{promotionId}/{adminId}")
    public ResponseEntity<PromotionResponse> reject(
            @PathVariable Long promotionId,
            @PathVariable Long adminId
    ) {
        return ResponseEntity.ok(promotionService.reject(promotionId, adminId));
    }

    // DEACTIVATE
    @PutMapping("/deactivate/{promotionId}/{adminId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long promotionId,
            @PathVariable Long adminId
    ) {
        promotionService.deactivate(promotionId, adminId);
        return ResponseEntity.ok().build();
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAll() {
        return ResponseEntity.ok(promotionService.getAll());
    }
}

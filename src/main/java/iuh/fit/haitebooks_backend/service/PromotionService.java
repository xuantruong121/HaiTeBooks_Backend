package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.PromotionRequest;
import iuh.fit.haitebooks_backend.dtos.response.PromotionResponse;
import iuh.fit.haitebooks_backend.mapper.PromotionMapper;
import iuh.fit.haitebooks_backend.model.*;
import iuh.fit.haitebooks_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepo;
    private final UserRepository userRepo;
    private final PromotionLogRepository logRepo;

    // ---------------------------------------
    // 🔥 CREATE PROMOTION
    // ---------------------------------------
    @Transactional
    public PromotionResponse create(PromotionRequest req, Long creatorId) {

        if (promotionRepo.existsByCode(req.getCode())) {
            throw new RuntimeException("Promotion code already exists");
        }

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }

        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Promotion p = new Promotion();
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setDiscountPercent(req.getDiscountPercent());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setQuantity(req.getQuantity());
        p.setCreatedBy(creator);
        p.setActive(true);

        promotionRepo.save(p);

        // 🔥 Log CREATE
        saveLog(p, creator, PromotionLog.CREATE);

        return PromotionMapper.toResponse(p);
    }

    // ---------------------------------------
    // 🔥 APPROVE PROMOTION (ADMIN)
    // ---------------------------------------
    @Transactional
    public PromotionResponse approve(Long promotionId, Long adminId) {
        Promotion p = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!p.isActive()) {
            throw new RuntimeException("Cannot approve inactive promotion");
        }

        p.setApprovedBy(admin);
        promotionRepo.save(p);

        // 🔥 Log APPROVE
        saveLog(p, admin, PromotionLog.APPROVE);

        return PromotionMapper.toResponse(p);
    }

    // ---------------------------------------
    // 🔥 REJECT PROMOTION
    // ---------------------------------------
    @Transactional
    public PromotionResponse reject(Long promotionId, Long adminId) {
        Promotion p = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        p.setActive(false);
        promotionRepo.save(p);

        // 🔥 Log REJECT
        saveLog(p, admin, PromotionLog.REJECT);

        return PromotionMapper.toResponse(p);
    }

    // ---------------------------------------
    // 🔥 DEACTIVATE (Soft delete)
    // ---------------------------------------
    @Transactional
    public void deactivate(Long promotionId, Long adminId) {
        Promotion p = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        p.setActive(false);
        promotionRepo.save(p);

        // Log
        saveLog(p, admin, PromotionLog.DEACTIVATE);
    }

    // ---------------------------------------
    // 🔥 GET ALL
    // ---------------------------------------
    public List<PromotionResponse> getAll() {
        return promotionRepo.findAll().stream()
                .map(PromotionMapper::toResponse)
                .toList();
    }

    // ---------------------------------------
    // 🔥 LOG Saver
    // ---------------------------------------
    private void saveLog(Promotion p, User actor, String action) {
        PromotionLog log = new PromotionLog();
        log.setPromotion(p);
        log.setActor(actor);
        log.setAction(action);
        log.setLogTime(LocalDateTime.now());
        logRepo.save(log);
    }

    public Promotion validatePromotion(String code) {
        Promotion p = promotionRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không tồn tại"));

        LocalDate today = LocalDate.now();

        if (!p.isActive()) {
            throw new RuntimeException("Mã khuyến mãi đã bị vô hiệu hóa");
        }
        if (p.getApprovedBy() == null) {
            throw new RuntimeException("Mã khuyến mãi chưa được duyệt");
        }
        if (p.getQuantity() <= 0) {
            throw new RuntimeException("Mã khuyến mãi đã hết số lượng");
        }
        if (today.isBefore(p.getStartDate())) {
            throw new RuntimeException("Mã khuyến mãi chưa đến ngày sử dụng");
        }
        if (today.isAfter(p.getEndDate())) {
            throw new RuntimeException("Mã khuyến mãi đã hết hạn");
        }

        return p;
    }

    @Transactional
    public Promotion applyPromotion(String code) {
        Promotion p = validatePromotion(code);

        // giảm số lượng
        p.setQuantity(p.getQuantity() - 1);
        promotionRepo.save(p);

        // Log
        saveLog(p, p.getApprovedBy(), "USE");

        return p;
    }

}

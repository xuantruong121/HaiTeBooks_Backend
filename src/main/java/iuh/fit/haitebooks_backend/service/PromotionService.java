package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.PromotionRequest;
import iuh.fit.haitebooks_backend.dtos.response.PromotionResponse;
import iuh.fit.haitebooks_backend.exception.BadRequestException;
import iuh.fit.haitebooks_backend.exception.ConflictException;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
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
            throw new ConflictException("Promotion code already exists");
        }

        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("Creator not found"));

        Promotion p = new Promotion();
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setDiscountPercent(req.getDiscountPercent());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setQuantity(req.getQuantity());
        p.setMinimumOrderAmount(req.getMinimumOrderAmount());
        p.setMaxDiscountAmount(req.getMaxDiscountAmount());
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
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        if (!p.isActive()) {
            throw new BadRequestException("Cannot approve inactive promotion");
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
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

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
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        // ✅ Thêm kiểm tra: Chỉ cho phép vô hiệu hóa nếu đang hoạt động
        if (!p.isActive()) {
            throw new BadRequestException("Khuyến mãi đã bị vô hiệu hóa");
        }

        p.setActive(false);
        promotionRepo.save(p);

        // Log
        saveLog(p, admin, PromotionLog.DEACTIVATE);
    }

    // ---------------------------------------
    // 🔥 UPDATE STATUS (Cập nhật trạng thái)
    // ---------------------------------------
    @Transactional
    public PromotionResponse updateStatus(Long promotionId, Boolean isActive, Long adminId) {
        Promotion p = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        // Kiểm tra nếu trạng thái không thay đổi
        if (p.isActive() == isActive) {
            throw new BadRequestException("Trạng thái hiện tại đã là " + 
                (isActive ? "kích hoạt" : "vô hiệu hóa"));
        }

        if (isActive) {
            // ✅ KÍCH HOẠT: Kiểm tra thời gian còn không
            LocalDate today = LocalDate.now();
            if (today.isAfter(p.getEndDate())) {
                throw new BadRequestException("Không thể kích hoạt khuyến mãi đã hết hạn");
            }
            
            // Kiểm tra thời gian bắt đầu (tùy chọn)
            if (today.isBefore(p.getStartDate())) {
                throw new BadRequestException("Không thể kích hoạt khuyến mãi chưa đến ngày bắt đầu");
            }
        } else {
            // ✅ VÔ HIỆU HÓA: Chỉ cho phép nếu đang hoạt động
            if (!p.isActive()) {
                throw new BadRequestException("Khuyến mãi đã bị vô hiệu hóa");
            }
        }

        p.setActive(isActive);
        promotionRepo.save(p);

        // Log
        String action = isActive ? PromotionLog.ACTIVATE : PromotionLog.DEACTIVATE;
        saveLog(p, admin, action);

        return PromotionMapper.toResponse(p);
    }

    // ---------------------------------------
    // 🔥 GET ALL
    // ---------------------------------------
    @Transactional
    public List<PromotionResponse> getAll() {
        List<Promotion> promotions = promotionRepo.findAll();
        LocalDate today = LocalDate.now();
        
        // ✅ Tự động vô hiệu hóa các promotion đã hết thời gian
        List<Promotion> expiredPromotions = promotions.stream()
                .filter(p -> p.isActive() && today.isAfter(p.getEndDate()))
                .toList();
        
        if (!expiredPromotions.isEmpty()) {
            for (Promotion p : expiredPromotions) {
                p.setActive(false);
                promotionRepo.save(p);
                // Log tự động vô hiệu hóa (có thể dùng createdBy hoặc null)
                if (p.getCreatedBy() != null) {
                    saveLog(p, p.getCreatedBy(), PromotionLog.DEACTIVATE);
                }
            }
        }
        
        return promotions.stream()
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
        return validatePromotion(code, null);
    }

    public Promotion validatePromotion(String code, Double orderAmount) {
        Promotion p = promotionRepo.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Mã khuyến mãi không tồn tại"));

        LocalDate today = LocalDate.now();

        if (!p.isActive()) {
            throw new BadRequestException("Mã khuyến mãi đã bị vô hiệu hóa");
        }

        // ✅ BỎ kiểm tra approvedBy vì tạo mới đã active rồi
        // if (p.getApprovedBy() == null) {
        //     throw new BadRequestException("Mã khuyến mãi chưa được duyệt");
        // }

        if (p.getQuantity() <= 0) {
            throw new BadRequestException("Mã khuyến mãi đã hết số lượng");
        }

        if (today.isBefore(p.getStartDate())) {
            throw new BadRequestException("Mã khuyến mãi chưa đến ngày sử dụng");
        }

        if (today.isAfter(p.getEndDate())) {
            throw new BadRequestException("Mã khuyến mãi đã hết hạn");
        }

        // Kiểm tra điều kiện đơn hàng tối thiểu
        if (p.getMinimumOrderAmount() != null && orderAmount != null) {
            if (orderAmount < p.getMinimumOrderAmount()) {
                throw new BadRequestException("Đơn hàng phải có giá trị tối thiểu " + 
                    String.format("%.0f", p.getMinimumOrderAmount()) + " VND để sử dụng mã này");
            }
        }

        return p;
    }

    @Transactional
    public Promotion applyPromotion(String code) {
        return applyPromotion(code, null);
    }

    @Transactional
    public Promotion applyPromotion(String code, Double orderAmount) {
        Promotion p = validatePromotion(code, orderAmount);

        // giảm số lượng
        p.setQuantity(p.getQuantity() - 1);
        promotionRepo.save(p);

        // Log - Sửa actor vì có thể không có approvedBy
        User actor = p.getApprovedBy() != null ? p.getApprovedBy() : p.getCreatedBy();
        if (actor != null) {
            saveLog(p, actor, "USE");
        }

        return p;
    }

    // ---------------------------------------
    // 🔥 UPDATE PROMOTION
    // ---------------------------------------
    @Transactional
    public PromotionResponse update(Long promotionId, PromotionRequest req) {
        // Tìm promotion theo ID
        Promotion p = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new NotFoundException("Promotion not found"));

        // Validate: Kiểm tra code không trùng với promotion khác (trừ chính nó)
        if (!p.getCode().equals(req.getCode())) {
            if (promotionRepo.existsByCode(req.getCode())) {
                throw new ConflictException("Promotion code already exists");
            }
        }

        // Validate: Start date phải trước end date
        if (req.getStartDate().isAfter(req.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        // Cập nhật các field
        p.setName(req.getName());
        p.setCode(req.getCode());
        p.setDiscountPercent(req.getDiscountPercent());
        p.setStartDate(req.getStartDate());
        p.setEndDate(req.getEndDate());
        p.setQuantity(req.getQuantity());
        p.setMinimumOrderAmount(req.getMinimumOrderAmount());
        p.setMaxDiscountAmount(req.getMaxDiscountAmount());
        promotionRepo.save(p);

        // Log UPDATE
        if (p.getApprovedBy() != null) {
            saveLog(p, p.getApprovedBy(), PromotionLog.UPDATE);
        } else if (p.getCreatedBy() != null) {
            saveLog(p, p.getCreatedBy(), PromotionLog.UPDATE);
        }

        return PromotionMapper.toResponse(p);
    }

}

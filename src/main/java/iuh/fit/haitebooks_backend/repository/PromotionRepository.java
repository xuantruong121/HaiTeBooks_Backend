package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Promotion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCode(String code);
    boolean existsByCode(String code);
    
    // ✅ Tối ưu: Eager fetch createdBy và approvedBy khi lấy tất cả promotions
    @EntityGraph(attributePaths = {"createdBy", "approvedBy"})
    @Override
    List<Promotion> findAll();
}

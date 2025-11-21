package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Notification;
import iuh.fit.haitebooks_backend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // ✅ Tối ưu: Eager fetch sender khi lấy notifications chưa đọc
    @EntityGraph(attributePaths = {"sender"})
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);

    void deleteByReceiverId(Long receiverId);
    
    // ✅ Tối ưu: Bulk update để đánh dấu tất cả notifications là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}


package iuh.fit.haitebooks_backend.repository;

import iuh.fit.haitebooks_backend.model.Notification;
import iuh.fit.haitebooks_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);

    void deleteByReceiverId(Long receiverId);
}


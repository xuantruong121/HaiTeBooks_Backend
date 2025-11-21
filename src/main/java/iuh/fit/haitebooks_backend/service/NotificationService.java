package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.NotificationRequest;
import iuh.fit.haitebooks_backend.dtos.response.NotificationResponse;
import iuh.fit.haitebooks_backend.exception.NotFoundException;
import iuh.fit.haitebooks_backend.mapper.NotificationMapper;
import iuh.fit.haitebooks_backend.model.Notification;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.NotificationRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // 🔥 Gửi thông báo (Lưu DB + Realtime)
    @Transactional
    public NotificationResponse send(NotificationRequest req, Long senderId) {

        User receiver = userRepo.findById(req.getReceiverId())
                .orElseThrow(() -> new NotFoundException("Receiver not found"));

        User sender = senderId != null
                ? userRepo.findById(senderId).orElse(null)
                : null;

        Notification noti = new Notification();
        noti.setTitle(req.getTitle());
        noti.setContent(req.getContent());
        noti.setReceiver(receiver);
        noti.setSender(sender);
        noti.setCreatedAt(LocalDateTime.now());
        noti.setRead(false);

        notificationRepo.save(noti);

        NotificationResponse response = NotificationMapper.toResponse(noti);

        // 🔥 PUSH realtime đến FE
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + receiver.getId(),
                response
        );

        return response;
    }

    // 🔥 Lấy tất cả thông báo theo userId
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepo
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> {
                    // Đảm bảo lazy relationships được load trong transaction
                    if (notification.getSender() != null) {
                        notification.getSender().getFullName();
                    }
                    return NotificationMapper.toResponse(notification);
                })
                .toList();
    }

    // 🔥 Lấy thông báo chưa đọc theo userId
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepo
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(notification -> {
                    // Đảm bảo lazy relationships được load trong transaction
                    if (notification.getSender() != null) {
                        notification.getSender().getFullName();
                    }
                    return NotificationMapper.toResponse(notification);
                })
                .toList();
    }

    // 🔥 Đánh dấu một thông báo là đã đọc
    @Transactional
    public void markAsRead(Long id) {
        Notification noti = notificationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        noti.setRead(true);
        notificationRepo.save(noti);
    }

    // 🔥 Đánh dấu tất cả là đã đọc - Tối ưu: Dùng bulk update query
    @Transactional
    public void markAllAsRead(Long userId) {
        // ✅ Tối ưu: Dùng bulk update query thay vì load tất cả và saveAll
        // Giảm memory usage và số lượng queries
        int updatedCount = notificationRepo.markAllAsReadByReceiverId(userId);
        // Log để debug (optional)
        if (updatedCount > 0) {
            // Có thể gửi WebSocket notification nếu cần
        }
    }

    // 🔥 Xóa 1 thông báo
    @Transactional
    public void delete(Long id) {
        notificationRepo.deleteById(id);
    }

    // 🔥 Xóa tất cả thông báo của userId
    @Transactional
    public void deleteAll(Long userId) {
        notificationRepo.deleteByReceiverId(userId);
    }
}

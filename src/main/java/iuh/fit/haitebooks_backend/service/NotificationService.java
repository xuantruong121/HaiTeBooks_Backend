package iuh.fit.haitebooks_backend.service;

import iuh.fit.haitebooks_backend.dtos.request.NotificationRequest;
import iuh.fit.haitebooks_backend.dtos.response.NotificationResponse;
import iuh.fit.haitebooks_backend.mapper.NotificationMapper;
import iuh.fit.haitebooks_backend.model.Notification;
import iuh.fit.haitebooks_backend.model.User;
import iuh.fit.haitebooks_backend.repository.NotificationRepository;
import iuh.fit.haitebooks_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // 🔥 Gửi thông báo (Lưu DB + Realtime)
    public NotificationResponse send(NotificationRequest req, Long senderId) {

        User receiver = userRepo.findById(req.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

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
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepo
                .findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    // 🔥 Lấy thông báo chưa đọc theo userId
    public List<NotificationResponse> getUnread(Long userId) {
        return notificationRepo
                .findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    // 🔥 Đánh dấu một thông báo là đã đọc
    public void markAsRead(Long id) {
        Notification noti = notificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        noti.setRead(true);
        notificationRepo.save(noti);
    }

    // 🔥 Đánh dấu tất cả là đã đọc
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepo.findByReceiverIdOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(list);
    }

    // 🔥 Xóa 1 thông báo
    public void delete(Long id) {
        notificationRepo.deleteById(id);
    }

    // 🔥 Xóa tất cả thông báo của userId
    public void deleteAll(Long userId) {
        notificationRepo.deleteByReceiverId(userId);
    }
}

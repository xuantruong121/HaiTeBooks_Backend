package iuh.fit.haitebooks_backend.mapper;

import iuh.fit.haitebooks_backend.dtos.response.NotificationResponse;
import iuh.fit.haitebooks_backend.model.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.isRead(),
                n.getCreatedAt(),
                n.getSender() != null ? n.getSender().getFullName() : "System"
        );
    }
}

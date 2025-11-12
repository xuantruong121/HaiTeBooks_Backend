package iuh.fit.haitebooks_backend.model;

public enum Status_Order {
    PENDING,        // 🕒 Đang chờ xác nhận
    PROCESSING,     // ⚙️ Đang xử lý (đã xác nhận đơn)
    SHIPPING,       // 🚚 Đang giao hàng
    COMPLETED,      // ✅ Hoàn tất giao hàng
    CANCELLED       // ❌ Đã hủy
}

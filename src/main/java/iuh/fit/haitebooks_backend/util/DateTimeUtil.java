package iuh.fit.haitebooks_backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class để xử lý thời gian theo múi giờ Việt Nam (UTC+7)
 */
public class DateTimeUtil {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Lấy thời gian hiện tại theo múi giờ Việt Nam (UTC+7)
     * @return LocalDateTime theo múi giờ Việt Nam
     */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    /**
     * Chuyển đổi LocalDateTime sang ZonedDateTime theo múi giờ Việt Nam
     * @param localDateTime Thời gian cần chuyển đổi
     * @return ZonedDateTime theo múi giờ Việt Nam
     */
    public static ZonedDateTime toVietnamZone(LocalDateTime localDateTime) {
        return localDateTime.atZone(VIETNAM_ZONE);
    }

    /**
     * Lấy ZonedDateTime hiện tại theo múi giờ Việt Nam
     * @return ZonedDateTime theo múi giờ Việt Nam
     */
    public static ZonedDateTime nowVietnamZoned() {
        return ZonedDateTime.now(VIETNAM_ZONE);
    }
}


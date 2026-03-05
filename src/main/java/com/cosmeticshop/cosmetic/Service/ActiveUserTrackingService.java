package com.cosmeticshop.cosmetic.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.ActiveUserTrafficResponse;

@Service
/**
 * Service theo dõi người dùng đang hoạt động (active users) theo thời gian thực.
 *
 * Cách hoạt động:
 * - Mỗi lần client gọi ping, username sẽ được cập nhật "last seen".
 * - User được xem là active nếu lần hoạt động gần nhất nằm trong cửa sổ ACTIVE_WINDOW_MINUTES.
 * - Dữ liệu lưu in-memory bằng ConcurrentHashMap để thread-safe.
 */
public class ActiveUserTrackingService implements IActiveUserTrackingService {

    // Khoảng thời gian để xác định user còn hoạt động (đơn vị: phút)
    private static final int ACTIVE_WINDOW_MINUTES = 5;

    // Map lưu thời điểm hoạt động gần nhất theo username
    private final Map<String, LocalDateTime> lastSeenByUser = new ConcurrentHashMap<>();

    @Override
    /**
     * Đánh dấu một user vừa hoạt động (gọi từ login hoặc endpoint ping).
     */
    public void markUserAsActive(String username) {
        // Bỏ qua input không hợp lệ
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        // Cập nhật thời điểm hoạt động gần nhất của user
        lastSeenByUser.put(username, LocalDateTime.now());

        // Dọn user đã quá hạn để map gọn và dữ liệu luôn mới
        clearInactiveUsers();
    }

    /**
     * Trả về thống kê traffic active users tại thời điểm hiện tại.
     */
    @Override
    public ActiveUserTrafficResponse getActiveUserTraffic() {
        // Dọn dữ liệu hết hạn trước khi tính toán
        clearInactiveUsers();
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(ACTIVE_WINDOW_MINUTES);

        // Đếm số user có lastSeen >= activeThreshold
        int activeUsers = (int) lastSeenByUser.values().stream()
                .filter(lastSeen -> !lastSeen.isBefore(activeThreshold))
                .count();

        // Trả DTO cho dashboard admin
        return new ActiveUserTrafficResponse(
                activeUsers,
                lastSeenByUser.size(),
                ACTIVE_WINDOW_MINUTES,
                LocalDateTime.now());
    }

    /**
     * Xóa các user đã không còn hoạt động trong cửa sổ ACTIVE_WINDOW_MINUTES.
     */
    private void clearInactiveUsers() {
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(ACTIVE_WINDOW_MINUTES);
        lastSeenByUser.entrySet().removeIf(entry -> entry.getValue().isBefore(activeThreshold));
    }
}

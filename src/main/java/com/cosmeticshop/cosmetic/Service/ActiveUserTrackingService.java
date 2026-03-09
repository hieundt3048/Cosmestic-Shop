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

    // Map lưu thời điểm hoạt động gần nhất theo username
    private final Map<String, LocalDateTime> lastSeenByUser = new ConcurrentHashMap<>();
    private final RuntimeSecuritySettingsService runtimeSecuritySettingsService;

    public ActiveUserTrackingService(RuntimeSecuritySettingsService runtimeSecuritySettingsService) {
        this.runtimeSecuritySettingsService = runtimeSecuritySettingsService;
    }

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
        int activeWindowMinutes = runtimeSecuritySettingsService.getSessionTimeoutMinutes();
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(activeWindowMinutes);

        // Đếm số user có lastSeen >= activeThreshold
        int activeUsers = (int) lastSeenByUser.values().stream()
                .filter(lastSeen -> !lastSeen.isBefore(activeThreshold))
                .count();

        // Trả DTO cho dashboard admin
        return new ActiveUserTrafficResponse(
                activeUsers,
                lastSeenByUser.size(),
                activeWindowMinutes,
                LocalDateTime.now());
    }

    /**
     * Xóa các user đã không còn hoạt động trong cửa sổ ACTIVE_WINDOW_MINUTES.
     */
    private void clearInactiveUsers() {
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(runtimeSecuritySettingsService.getSessionTimeoutMinutes());
        lastSeenByUser.entrySet().removeIf(entry -> entry.getValue().isBefore(activeThreshold));
    }
}

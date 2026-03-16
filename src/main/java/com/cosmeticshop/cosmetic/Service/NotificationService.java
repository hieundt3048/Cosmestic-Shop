package com.cosmeticshop.cosmetic.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cosmeticshop.cosmetic.Dto.NotificationResponse;
import com.cosmeticshop.cosmetic.Entity.Notification;
import com.cosmeticshop.cosmetic.Entity.User;
import com.cosmeticshop.cosmetic.Exception.ResourceNotFoundException;
import com.cosmeticshop.cosmetic.Repository.NotificationRepository;
import com.cosmeticshop.cosmetic.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getMyNotifications(String username) {
        User user = findByUsername(username);
        // Trả thông báo mới nhất trước để FE hiển thị đúng thứ tự timeline.
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        User user = findByUsername(username);
        // Dùng cho badge thông báo ở header/account.
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId()).size();
    }

    @Transactional
    public NotificationResponse markAsRead(String username, Long notificationId) {
        User user = findByUsername(username);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với id: " + notificationId));

        // Chặn truy cập chéo: user chỉ được thao tác trên thông báo của chính mình.
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập thông báo này");
        }

        // Idempotent: đã đọc rồi thì không cập nhật lại.
        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = findByUsername(username);
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(item -> item.setRead(true));
        // Batch save để giảm số lần ghi DB khi user có nhiều thông báo chưa đọc.
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public Notification createNotificationForUser(
            User user,
            Notification.Type type,
            String title,
            String content,
            String referenceId) {
        // Hàm nền tảng để các nghiệp vụ khác (order/promotion/system) tái sử dụng.
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void createOrderStatusNotification(User customer, String status, Long orderId) {
        // Chuẩn hóa nội dung trạng thái đơn trước khi gửi tới khách hàng.
        createNotificationForUser(
                customer,
                Notification.Type.ORDER_STATUS,
                "Đơn hàng #" + orderId + " cập nhật trạng thái",
                "Trạng thái mới: " + mapOrderStatus(status),
                "ORDER#" + orderId);
    }

    @Transactional
    public void broadcastPromotionNotification(String title, String content, String referenceId) {
        // Broadcast tới toàn bộ CUSTOMER khi có chương trình khuyến mãi mới.
        List<User> customers = userRepository.findByRole(User.Role.CUSTOMER);
        for (User customer : customers) {
            createNotificationForUser(customer, Notification.Type.PROMOTION, title, content, referenceId);
        }
    }

    private String mapOrderStatus(String status) {
        if (status == null) {
            return "Không xác định";
        }

        // Ánh xạ enum nội bộ sang nhãn tiếng Việt thân thiện cho người dùng.
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PACKING" -> "Đang chuẩn bị";
            case "SHIPPED" -> "Đang giao";
            case "DELIVERED" -> "Đã giao";
            case "CANCELED" -> "Đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản: " + username));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}

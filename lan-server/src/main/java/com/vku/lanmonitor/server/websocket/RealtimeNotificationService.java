package com.vku.lanmonitor.server.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Đẩy thông báo cập nhật danh sách Client online lên Web
     */
    public void notifyClientListUpdated(Object clients) {
        messagingTemplate.convertAndSend("/topic/clients", clients);
        log.info("Đã đẩy danh sách Client mới lên Web qua WebSocket.");
    }

    /**
     * Đẩy ảnh chụp màn hình nhận được từ máy con lên Web
     */
    public void sendScreenshotToWeb(String clientId, String base64Image) {
        messagingTemplate.convertAndSend("/topic/screenshot/" + clientId, base64Image);
        log.info("Đã đẩy ảnh chụp màn hình của client [{}] lên Web.", clientId);
    }

    public void sendSuspiciousAlertToWeb(String clientId, String pcName, String severity, String tabName,
            String imageUrl) {
        java.util.Map<String, String> alertData = new java.util.HashMap<>();
        alertData.put("clientId", clientId);
        alertData.put("pcName", pcName != null ? pcName : "Máy Trạm");
        alertData.put("severity", severity);
        alertData.put("tabName", tabName);
        alertData.put("imageUrl", imageUrl); // Link ảnh để Web load

        // Đẩy dữ liệu vào kênh /topic/alerts
        messagingTemplate.convertAndSend("/topic/alerts", alertData);
        log.info(">>> Đã đẩy cảnh báo {} của {} lên Web.", severity, clientId);
    }
}
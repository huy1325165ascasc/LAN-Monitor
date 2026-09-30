package com.vku.lanmonitor.server.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vku.lanmonitor.server.socket.TcpServerService;

import java.io.File;
import java.awt.Desktop;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientApiController {

    private final TcpServerService tcpServer;

    @GetMapping
    public ResponseEntity<?> getClients() {
        return ResponseEntity.ok(tcpServer.getAllClients());
    }

    @PostMapping("/{clientId}/capture")
    public ResponseEntity<?> requestScreenshot(@PathVariable String clientId) {
        tcpServer.sendCommand(clientId, "CAPTURE_SCREEN");
        return ResponseEntity.ok(Map.of("message", "Đã gửi lệnh chụp màn hình"));
    }

    @PostMapping("/{clientId}/alert")
    public ResponseEntity<?> sendAlert(@PathVariable String clientId, @RequestBody Map<String, String> body) {
        tcpServer.sendCommand(clientId, "ALERT:" + body.getOrDefault("message", ""));
        return ResponseEntity.ok(Map.of("message", "Đã gửi cảnh báo thành công"));
    }

    @PostMapping("/{clientId}/save-frame")
    public ResponseEntity<?> saveLivestreamFrame(@PathVariable String clientId) {
        tcpServer.requestCapture(clientId);
        return ResponseEntity.ok(Map.of("message", "Đã yêu cầu lưu ảnh Livestream"));
    }

    // API GỌI WINDOWS MỞ THƯ MỤC ẢNH
    @PostMapping("/open-images-folder")
    public ResponseEntity<?> openImagesFolder() {
        try {
            File dir = new File("images");
            if (!dir.exists())
                dir.mkdirs(); // Nếu chưa có thì tạo mới

            // Yêu cầu Hệ điều hành mở thư mục bằng File Explorer
            Desktop.getDesktop().open(dir);

            return ResponseEntity.ok(Map.of("message", "Đã mở thư mục ảnh"));
        } catch (Exception e) {
            log.error("Không thể mở thư mục: ", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
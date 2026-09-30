package com.vku.lanmonitor.server.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vku.lanmonitor.server.socket.TcpServerService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final TcpServerService tcpServer; // SỬ DỤNG FILE ĐÃ GỘP
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String STORAGE_DIR = "storage";
    private final String RULES_FILE = STORAGE_DIR + "/exam_rules.json";

    @GetMapping("/whitelist")
    public ResponseEntity<List<String>> getWhitelist() {
        try {
            File file = new File(RULES_FILE);
            if (file.exists()) {
                List<String> whitelist = Arrays.asList(objectMapper.readValue(file, String[].class));
                return ResponseEntity.ok(whitelist);
            }
        } catch (Exception e) {
        }
        return ResponseEntity.ok(Arrays.asList("Bài Thi Trắc Nghiệm", "Word", "Calculator", "Excel"));
    }

    @PostMapping("/whitelist")
    public ResponseEntity<?> updateWhitelist(@RequestBody Map<String, List<String>> body) {
        List<String> newWhitelist = body.get("whitelist");
        if (newWhitelist == null || newWhitelist.isEmpty()) {
            return ResponseEntity.badRequest().body("Danh sách trắng không được để trống");
        }

        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            objectMapper.writeValue(new File(RULES_FILE), newWhitelist);
            String joinedKeywords = String.join(",", newWhitelist);

            tcpServer.broadcast("UPDATE_WHITELIST:" + joinedKeywords); // PHÁT SÓNG LỆNH

            return ResponseEntity.ok(Map.of("message", "Đã cập nhật danh sách và gửi lệnh!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi Server: " + e.getMessage());
        }
    }
}
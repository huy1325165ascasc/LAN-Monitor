package com.vku.lanmonitor.server.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
public class QuizApiController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String DIR = "storage";
    private final String QUIZ_FILE = DIR + "/quizzes.json";

    public QuizApiController() {
        // Tự động tạo sẵn các thư mục cần thiết khi Server khởi động
        try {
            Files.createDirectories(Paths.get(DIR));
            Files.createDirectories(Paths.get(DIR + "/quiz_images")); // Thư mục lưu ảnh trắc nghiệm
            Files.createDirectories(Paths.get(DIR + "/results")); // Thư mục lưu bài làm sinh viên
        } catch (Exception e) {
            log.error("Không thể tạo thư mục storage: ", e);
        }
    }

    // 1. LƯU TOÀN BỘ ĐỀ THI (Dành cho Giám thị)
    @PostMapping
    public ResponseEntity<?> saveQuizzes(@RequestBody List<Object> quizzes) {
        try {
            mapper.writeValue(new File(QUIZ_FILE), quizzes);
            return ResponseEntity.ok(Map.of("message", "Đã lưu đề thi thành công!"));
        } catch (Exception e) {
            log.error("Lỗi lưu đề thi: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 2. LẤY DANH SÁCH ĐỀ THI (Cho cả Thầy và Trò)
    @GetMapping
    public ResponseEntity<List<Object>> getQuizzes() {
        try {
            File file = new File(QUIZ_FILE);
            if (file.exists()) {
                return ResponseEntity.ok(mapper.readValue(file, List.class));
            }
        } catch (Exception e) {
            log.error("Lỗi lấy đề thi: ", e);
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    // 3. SINH VIÊN NỘP BÀI THI
    @PostMapping("/submit")
    public ResponseEntity<?> submitQuizResult(@RequestBody Map<String, Object> resultData) {
        try {
            String studentId = (String) resultData.getOrDefault("studentId", "UNKNOWN");
            String quizId = String.valueOf(resultData.get("quizId"));

            // Tên file: MãSV_MãĐề_ThờiGian.json (VD: 24ITB217_1700123_1710999.json)
            String fileName = DIR + "/results/" + studentId + "_" + quizId + "_" + System.currentTimeMillis() + ".json";

            mapper.writeValue(new File(fileName), resultData);
            log.info(">>> Đã thu bài của sinh viên [{}] cho mã đề [{}]", studentId, quizId);

            return ResponseEntity.ok(Map.of("message", "Nộp bài thành công!"));
        } catch (Exception e) {
            log.error("Lỗi khi lưu bài làm của sinh viên: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 4. XÓA ẢNH ĐÍNH KÈM (Khi Thầy bấm xóa ảnh lúc soạn đề)
    @DeleteMapping("/image/{fileName}")
    public ResponseEntity<?> deleteImage(@PathVariable String fileName) {
        try {
            File img = new File(DIR + "/quiz_images/" + fileName);
            if (img.exists()) {
                img.delete();
            }
            return ResponseEntity.ok(Map.of("message", "Đã xóa ảnh vật lý"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
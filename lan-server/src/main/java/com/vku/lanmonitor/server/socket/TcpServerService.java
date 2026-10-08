package com.vku.lanmonitor.server.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.server.model.ClientSession;
import com.vku.lanmonitor.server.service.QuizService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcpServerService implements CommandLineRunner {

    private static final java.io.PrintStream OUT = new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8);

    private static final String ADMIN_USER = "admin";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String STORAGE_DIR = "storage";
    private static final String RULES_FILE = STORAGE_DIR + "/exam_rules.json";

    private final TcpBroadcastService broadcastService;
    private final QuizService quizService;
    private final ObjectMapper mapper = new ObjectMapper();

    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private final Map<String, ClientSession> activeClients = new ConcurrentHashMap<>();
    private final Map<String, Boolean> captureRequests = new ConcurrentHashMap<>();

    public List<ClientSession> getAllClients() {
        return new ArrayList<>(activeClients.values());
    }

    public void sendCommand(String clientId, String cmd) {
        broadcastService.sendToClient(clientId, cmd);
    }

    public void broadcast(String cmd) {
        broadcastService.broadcastToClients(cmd);
    }

    public void requestCapture(String clientId) {
        captureRequests.put(clientId, true);
    }

    @Override
    public void run(String... args) {
        new Thread(this::startServer).start();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(9999);
            isRunning = true;
            log.info(">>> Đã mở cổng 9999 đón sinh viên vào thi <<<");

            Files.createDirectories(Paths.get("images"));
            Files.createDirectories(Paths.get("images", "webcam_clips"));
            Files.createDirectories(Paths.get("images", "webcam_snapshots"));
            Files.createDirectories(Paths.get(STORAGE_DIR));

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            log.error("Lỗi Server: {}", e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final String connectionId;
        private String role = "UNKNOWN";
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.connectionId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            this.clientId = this.connectionId;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = in.readLine()) != null) {
                    handleLine(line, out);
                }
            } catch (Exception e) {
                log.debug("Kết nối [{}] đóng: {}", connectionId, e.getMessage());
            } finally {
                boolean wasRegistered = !"UNKNOWN".equals(role);
                boolean wasClient = "CLIENT".equals(role);
                
                if (wasRegistered) {
                    activeClients.remove(clientId);
                    captureRequests.remove(clientId);
                    broadcastService.unregister(connectionId);
                    broadcastService.unregister(clientId);
                }
                
                if (wasClient) {
                    broadcastClientList();
                }
            }
        }

        private void handleLine(String line, PrintWriter out) {
            try {
                if (line.startsWith("STUDENT_LOGIN:")) {
                    handleStudentLogin(payload(line, "STUDENT_LOGIN:"), out);
                } else if (line.startsWith("ADMIN_LOGIN:")) {
                    handleAdminLogin(payload(line, "ADMIN_LOGIN:"), out);
                } else if (line.startsWith("CONNECT:")) {
                    handleConnect(payload(line, "CONNECT:"), out);
                } else if (line.startsWith("REQ_QUIZ_LIST")) {
                    out.println("RES_QUIZ_LIST:" + mapper.writeValueAsString(quizService.loadAllQuizzes()));
                } else if (line.startsWith("REQ_QUIZ_DETAIL:")) {
                    Object quiz = quizService.loadQuizById(payload(line, "REQ_QUIZ_DETAIL:"));
                    out.println("RES_QUIZ_DETAIL:" + (quiz == null ? "null" : mapper.writeValueAsString(quiz)));
                } else if (line.startsWith("SUBMIT_QUIZ:")) {
                    handleSubmitQuiz(payload(line, "SUBMIT_QUIZ:"), out);
                } else if (line.startsWith("REQ_CLIENT_LIST")) {
                    List<Map<String, String>> details = new ArrayList<>();
                    for (ClientSession session : activeClients.values()) {
                        Map<String, String> c = new HashMap<>();
                        c.put("id", session.getId());
                        c.put("ip", session.getIpAddress());
                        c.put("pcName", session.getPcName());
                        details.add(c);
                    }
                    out.println("RES_CLIENT_LIST:" + mapper.writeValueAsString(details));
                } else if (line.startsWith("REQ_QUIZ_ALL")) {
                    out.println("RES_QUIZ_ALL:" + mapper.writeValueAsString(quizService.loadAllQuizzes()));
                } else if (line.startsWith("SAVE_QUIZ:")) {
                    handleSaveQuiz(payload(line, "SAVE_QUIZ:"), out);
                } else if (line.startsWith("DELETE_QUIZ:")) {
                    handleDeleteQuiz(payload(line, "DELETE_QUIZ:"), out);
                } else if (line.startsWith("CAPTURE:")) {
                    handleCapture(payload(line, "CAPTURE:"), out);
                } else if (line.startsWith("SEND_ALERT:")) {
                    handleSendAlert(payload(line, "SEND_ALERT:"));
                } else if (line.startsWith("SAVE_FRAME:")) {
                    requestCapture(resolveClientId(payload(line, "SAVE_FRAME:")));
                } else if (line.startsWith("REQ_WHITELIST")) {
                    out.println("RES_WHITELIST:" + String.join(",", loadWhitelist()));
                } else if (line.startsWith("UPDATE_WHITELIST_CONFIG:")) {
                    handleUpdateWhitelist(payload(line, "UPDATE_WHITELIST_CONFIG:"), out);
                } else if (line.startsWith("SCREEN:")) {
                    handleScreen(payload(line, "SCREEN:"));
                } else if (line.startsWith("WEBCAM:")) {
                    handleWebcam(payload(line, "WEBCAM:"));
                } else if (line.startsWith("WEBCAM_CLIP:")) {
                    handleWebcamClip(payload(line, "WEBCAM_CLIP:"));
                } else if (line.startsWith("CAPTURE_WEBCAM:")) {
                    handleCaptureWebcam(payload(line, "CAPTURE_WEBCAM:"), out);
                } else if (line.startsWith("SUSPICIOUS:")) {
                    handleSuspicious(payload(line, "SUSPICIOUS:"));
                } else {
                    log.debug("Lệnh không nhận diện từ [{}]: {}", connectionId, line);
                }
            } catch (Exception e) {
                log.error("Lỗi xử lý lệnh từ [{}]: {}", connectionId, e.getMessage());
                out.println("ERROR:" + e.getMessage());
            }
        }

        private void handleStudentLogin(String payload, PrintWriter out) {
            String[] parts = payload.split(":", 2);
            if (parts.length < 2 || parts[0].isBlank() || !DEFAULT_PASSWORD.equals(parts[1])) {
                out.println("LOGIN_FAIL");
                return;
            }
            role = "STUDENT";
            clientId = parts[0].trim();
            broadcastService.registerStudent(connectionId, out);
            out.println("LOGIN_OK");
        }

        private void handleAdminLogin(String payload, PrintWriter out) {
            String[] parts = payload.split(":", 2);
            if (parts.length < 2 || !ADMIN_USER.equals(parts[0]) || !DEFAULT_PASSWORD.equals(parts[1])) {
                out.println("ADMIN_FAIL");
                return;
            }
            role = "ADMIN";
            clientId = connectionId;
            broadcastService.registerAdmin(connectionId, out);
            out.println("ADMIN_OK");
        }

        private void handleConnect(String pcName, PrintWriter out) throws Exception {
            role = "CLIENT";
            ClientSession session = new ClientSession(connectionId, socket.getInetAddress().getHostAddress(),
                    pcName == null || pcName.isBlank() ? "Chưa nhập tên" : pcName);
            clientId = connectionId;
            activeClients.put(clientId, session);
            broadcastService.registerClient(clientId, out);
            out.println("UPDATE_WHITELIST:" + String.join(",", loadWhitelist()));
            broadcastClientList();
        }

        @SuppressWarnings("unchecked")
        private void handleSubmitQuiz(String json, PrintWriter out) {
            try {
                Map<String, Object> resultData = mapper.readValue(json, Map.class);
                quizService.saveQuizResult(resultData);
                out.println("SUBMIT_OK");
            } catch (Exception e) {
                log.error("Lỗi nộp bài: ", e);
                out.println("SUBMIT_FAIL:" + e.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
        private void handleSaveQuiz(String json, PrintWriter out) {
            try {
                String trimmed = json.trim();
                if (trimmed.startsWith("[")) {
                    quizService.saveAllQuizzes(mapper.readValue(trimmed, List.class));
                } else {
                    Object quiz = mapper.readValue(trimmed, Object.class);
                    List<Object> all = new ArrayList<>(quizService.loadAllQuizzes());
                    String quizId = quiz instanceof Map<?, ?> map ? String.valueOf(map.get("id")) : null;
                    boolean replaced = false;
                    if (quizId != null && !"null".equals(quizId)) {
                        for (int i = 0; i < all.size(); i++) {
                            Object item = all.get(i);
                            if (item instanceof Map<?, ?> map && quizId.equals(String.valueOf(map.get("id")))) {
                                all.set(i, quiz);
                                replaced = true;
                                break;
                            }
                        }
                    }
                    if (!replaced) {
                        all.add(quiz);
                    }
                    quizService.saveAllQuizzes(all);
                }
                out.println("SAVE_QUIZ_OK");
            } catch (Exception e) {
                log.error("Lỗi lưu đề thi: ", e);
                out.println("SAVE_QUIZ_FAIL:" + e.getMessage());
            }
        }

        private void handleDeleteQuiz(String quizId, PrintWriter out) {
            try {
                List<Object> all = new ArrayList<>(quizService.loadAllQuizzes());
                boolean removed = all.removeIf(item -> item instanceof Map<?, ?> map
                        && quizId.equals(String.valueOf(map.get("id"))));
                if (removed) {
                    quizService.saveAllQuizzes(all);
                    out.println("DELETE_QUIZ_OK");
                } else {
                    out.println("DELETE_QUIZ_FAIL:not_found");
                }
            } catch (Exception e) {
                log.error("Lỗi xóa đề thi: ", e);
                out.println("DELETE_QUIZ_FAIL:" + e.getMessage());
            }
        }

        private void handleCapture(String target, PrintWriter out) {
            OUT.println("[SERVER] CAPTURE command received for: " + target);
            String resolved = resolveClientId(target);
            OUT.println("[SERVER] Resolved to: " + resolved);
            if (resolved == null) {
                out.println("CAPTURE_FAIL:not_found");
                return;
            }
            broadcastService.sendToClient(resolved, "CAPTURE_SCREEN");
            OUT.println("[SERVER] Sent CAPTURE_SCREEN to " + resolved);
        }

        private void handleSendAlert(String payload) {
            String[] parts = payload.split(":", 2);
            if (parts.length == 0 || parts[0].isBlank()) {
                return;
            }
            String resolved = resolveClientId(parts[0]);
            if (resolved != null) {
                String message = parts.length > 1 ? parts[1] : "";
                broadcastService.sendToClient(resolved, "ALERT:" + message);
            }
        }

        private void handleUpdateWhitelist(String keywords, PrintWriter out) {
            try {
                List<String> newWhitelist = Arrays.asList(keywords.split(","));
                Files.createDirectories(Paths.get(STORAGE_DIR));
                mapper.writeValue(new File(RULES_FILE), newWhitelist);
                broadcastService.broadcastToClients("UPDATE_WHITELIST:" + String.join(",", newWhitelist));
                out.println("UPDATE_WHITELIST_OK");
            } catch (Exception e) {
                log.error("Lỗi cập nhật whitelist: ", e);
                out.println("UPDATE_WHITELIST_FAIL:" + e.getMessage());
            }
        }

        private void handleScreen(String base64Image) {
            OUT.println("[SERVER] SCREEN received, len=" + base64Image.length());
            broadcastService.broadcastToAdmins("SCREEN_STREAM:" + connectionId + ":" + base64Image);
            if (clientId != null && captureRequests.getOrDefault(clientId, false)) {
                captureRequests.put(clientId, false);
                try {
                    String fName = "cheat_" + clientId.replace(":", "_") + "_" + System.currentTimeMillis() + ".jpg";
                    Files.write(Paths.get("images", fName), Base64.getDecoder().decode(base64Image));
                } catch (Exception e) {
                    log.error("Lỗi lưu ảnh: ", e);
                }
            }
        }

        private void handleSuspicious(String payload) {
            ClientSession session = activeClients.get(clientId);
            String pcName = session != null ? session.getPcName() : clientId;
            broadcastService.broadcastToAdmins("SUSPICIOUS:" + clientId + "|" + pcName + "|" + payload);

            // Tự động yêu cầu client ghi clip webcam 5s khi phát hiện hành vi nghi ngờ
            broadcastService.sendToClient(clientId, "RECORD_WEBCAM");
            OUT.println("[SERVER] Auto-triggered RECORD_WEBCAM for " + clientId + " due to suspicious activity");
        }

        /**
         * Xử lý ảnh webcam chụp định kỳ từ client.
         * Forward ảnh đến tất cả admin để xem realtime.
         * Lưu snapshot vào thư mục images/webcam_snapshots/ (ghi đè mỗi client).
         */
        private void handleWebcam(String base64Image) {
            OUT.println("[SERVER] WEBCAM received from [" + connectionId + "], len=" + base64Image.length());
            // Broadcast ảnh webcam đến tất cả admin (giống SCREEN_STREAM)
            broadcastService.broadcastToAdmins("WEBCAM_STREAM:" + connectionId + ":" + base64Image);

            // Lưu snapshot mới nhất (ghi đè file cũ theo clientId)
            try {
                String safeId = clientId.replace(":", "_").replace("/", "_");
                String fName = "webcam_" + safeId + ".jpg";
                Files.write(Paths.get("images", "webcam_snapshots", fName),
                        Base64.getDecoder().decode(base64Image));
            } catch (Exception e) {
                log.error("Lỗi lưu webcam snapshot: ", e);
            }
        }

        /**
         * Xử lý clip webcam 5s từ client (khi bị trigger bởi hành vi nghi ngờ).
         * Clip gồm nhiều frame JPEG nối bằng "|||".
         * Lưu từng frame vào thư mục images/webcam_clips/<clientId>_<timestamp>/
         */
        private void handleWebcamClip(String clipData) {
            OUT.println("[SERVER] WEBCAM_CLIP received from [" + connectionId + "], total len=" + clipData.length());

            // Thông báo cho admin biết có clip mới
            ClientSession session = activeClients.get(clientId);
            String pcName = session != null ? session.getPcName() : clientId;
            broadcastService.broadcastToAdmins(
                    "WEBCAM_CLIP_READY:" + clientId + "|" + pcName + "|" + System.currentTimeMillis());

            // Lưu từng frame vào disk
            try {
                String safeId = clientId.replace(":", "_").replace("/", "_");
                String dirName = "clip_" + safeId + "_" + System.currentTimeMillis();
                java.nio.file.Path clipDir = Paths.get("images", "webcam_clips", dirName);
                Files.createDirectories(clipDir);

                String[] frames = clipData.split("\\|\\|\\|");
                for (int i = 0; i < frames.length; i++) {
                    if (frames[i] != null && !frames[i].isEmpty()) {
                        String frameName = String.format("frame_%03d.jpg", i);
                        Files.write(clipDir.resolve(frameName),
                                Base64.getDecoder().decode(frames[i]));
                    }
                }
                OUT.println("[SERVER] Saved " + frames.length + " clip frames to " + clipDir);
            } catch (Exception e) {
                log.error("Lỗi lưu webcam clip: ", e);
            }
        }

        /**
         * Admin yêu cầu chụp webcam ngay lập tức cho 1 client cụ thể.
         */
        private void handleCaptureWebcam(String target, PrintWriter out) {
            OUT.println("[SERVER] CAPTURE_WEBCAM command received for: " + target);
            String resolved = resolveClientId(target);
            if (resolved == null) {
                out.println("CAPTURE_WEBCAM_FAIL:not_found");
                return;
            }
            broadcastService.sendToClient(resolved, "CAPTURE_WEBCAM");
            OUT.println("[SERVER] Sent CAPTURE_WEBCAM to " + resolved);
        }
    }

    private void broadcastClientList() {
        try {
            broadcastService.broadcastToAdmins("BROADCAST_CLIENTS:" + mapper.writeValueAsString(clientNames()));
        } catch (Exception e) {
            log.error("Lỗi broadcast danh sách client: ", e);
        }
    }

    private List<String> clientNames() {
        List<String> names = new ArrayList<>();
        for (ClientSession session : activeClients.values()) {
            names.add(session.getPcName());
        }
        return names;
    }

    private String resolveClientId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String trimmed = token.trim();
        if (activeClients.containsKey(trimmed)) {
            return trimmed;
        }
        for (ClientSession session : activeClients.values()) {
            if (trimmed.equals(session.getPcName())) {
                return session.getId();
            }
        }
        return null;
    }

    private List<String> loadWhitelist() {
        try {
            File file = new File(RULES_FILE);
            if (file.exists()) {
                return Arrays.asList(mapper.readValue(file, String[].class));
            }
        } catch (Exception e) {
            log.error("Lỗi đọc whitelist: ", e);
        }
        return Arrays.asList("Bài Thi Trắc Nghiệm", "Word", "Calculator", "Excel");
    }

    private static String payload(String line, String prefix) {
        return line.length() > prefix.length() ? line.substring(prefix.length()) : "";
    }

    @PreDestroy
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
        } catch (Exception ignored) {
        }
    }
}

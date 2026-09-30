package com.vku.lanmonitor.server.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.server.model.ClientSession;
import com.vku.lanmonitor.server.websocket.RealtimeNotificationService;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcpServerService implements CommandLineRunner {

    private final RealtimeNotificationService notificationService;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // Lưu trữ các máy sinh viên đang thi
    private final Map<String, ClientSession> activeClients = new ConcurrentHashMap<>();
    private final Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> captureRequests = new ConcurrentHashMap<>();

    // Các hàm cung cấp cho API gọi
    public List<ClientSession> getAllClients() {
        return new ArrayList<>(activeClients.values());
    }

    public void sendCommand(String clientId, String cmd) {
        if (clientWriters.containsKey(clientId))
            clientWriters.get(clientId).println(cmd);
    }

    public void broadcast(String cmd) {
        clientWriters.values().forEach(out -> out.println(cmd));
    }

    public void requestCapture(String clientId) {
        captureRequests.put(clientId, true);
    }

    @Override
    public void run(String... args) {
        new Thread(this::startServer).start(); // Khởi động Server ngầm
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(9999);
            isRunning = true;
            log.info(">>> Đã mở cổng 9999 đón sinh viên vào thi <<<");

            Files.createDirectories(Paths.get("images")); // Tạo sẵn thư mục lưu ảnh gian lận

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket)); // Có người kết nối -> Đẩy cho 1 luồng xử lý
            }
        } catch (Exception e) {
            log.error("Lỗi Server: {}", e.getMessage());
        }
    }

    // Class con xử lý luồng nhận dữ liệu của MỘT sinh viên
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                // 1. Thêm vào danh sách và gửi luật thi
                ClientSession session = new ClientSession(clientId, socket.getInetAddress().getHostAddress(),
                        "Chưa nhập tên");
                activeClients.put(clientId, session);
                clientWriters.put(clientId, out);
                notificationService.notifyClientListUpdated(getAllClients());

                File ruleFile = new File("storage/exam_rules.json");
                if (ruleFile.exists()) {
                    String[] rules = new ObjectMapper().readValue(ruleFile, String[].class);
                    out.println("UPDATE_WHITELIST:" + String.join(",", rules));
                }

                // 2. Vòng lặp liên tục đọc tin nhắn sinh viên gửi lên
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("CONNECT:")) {
                        // Nhận Mã SV
                        session.setPcName(line.substring(8));
                        notificationService.notifyClientListUpdated(getAllClients());
                    } else if (line.startsWith("SCREEN:")) {
                        // Nhận ảnh livestream
                        String base64Image = line.substring(7);
                        notificationService.sendScreenshotToWeb(clientId, base64Image);

                        if (captureRequests.getOrDefault(clientId, false)) { // Nếu Thầy bấm nút chụp
                            captureRequests.put(clientId, false);
                            String fName = "cheat_" + clientId.replace(":", "_") + "_" + System.currentTimeMillis()
                                    + ".jpg";
                            Files.write(Paths.get("images", fName), Base64.getDecoder().decode(base64Image));
                        }
                    } else if (line.startsWith("SUSPICIOUS:")) {
                        // Nhận cảnh báo (VD: SUSPICIOUS:RED|Chrome)
                        String[] parts = line.substring(11).split("\\|", 2);
                        if (parts.length == 2)
                            notificationService.sendSuspiciousAlertToWeb(clientId, session.getPcName(), parts[0],
                                    parts[1], null);
                    }
                }
            } catch (Exception e) {
            } finally {
                // Rút cáp mạng -> Xóa khỏi danh sách
                activeClients.remove(clientId);
                clientWriters.remove(clientId);
                captureRequests.remove(clientId);
                notificationService.notifyClientListUpdated(getAllClients());
            }
        }
    }

    @PreDestroy
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null)
                serverSocket.close();
            threadPool.shutdown();
        } catch (Exception ignored) {
        }
    }
}
package com.vku.lanmonitor.client.service;

import com.vku.lanmonitor.client.CameraCaptureUtil;
import com.vku.lanmonitor.client.util.ActiveWindowUtil;
import com.vku.lanmonitor.client.util.ScreenCaptureUtil;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SocketClientService {

    private final String serverIp;
    private final int serverPort;
    private final String studentId; // Thêm biến lưu Mã SV

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isRunning = false; // Biến cờ để điều khiển vòng lặp
    private volatile boolean isConnected = false;
    private volatile boolean shouldStop = false;
    private String lastAlertTab = "";
    private ScheduledExecutorService monitorScheduler;
    private ScheduledExecutorService webcamScheduler;

    private List<String> whitelist = new ArrayList<>();
    private boolean cameraAvailable = false;

    private static final int WEBCAM_CAPTURE_INTERVAL_SECONDS = 10;
    private long lastRecordTime = 0;
    private static final long RECORD_COOLDOWN_MS = 30000;

    // Thêm studentId vào Constructor
    public SocketClientService(String serverIp, int serverPort, String studentId) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.studentId = studentId;
    }

    public void start() {
        System.out.println("[CLIENT] === start() BEGIN ===");
        // Kiểm tra webcam 1 lần khi khởi động
        checkCameraAvailability();

        // Start schedulers ONCE, not inside the loop
        startBehaviorMonitor();
        System.out.println("[CLIENT] Monitor scheduler OK");
        startWebcamCapture();
        System.out.println("[CLIENT] Webcam scheduler OK");

        while (!shouldStop) {
            try {
                System.out.println("Connecting to Server [" + serverIp + ":" + serverPort + "]...");
                socket = new Socket(serverIp, serverPort);
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                isConnected = true;
                System.out.println("[CLIENT] TCP connected, isConnected=true");

                // QUAN TRỌNG NHẤT: Báo danh với Server ngay khi vừa kết nối!
                out.println("CONNECT:" + studentId);
                out.flush();
                System.out.println("[CLIENT] CONNECT sent");

                String line;
                // Liên tục lắng nghe lệnh từ Thầy giáo (như Lệnh chụp ảnh, Cập nhật luật thi)
                while (isConnected && !shouldStop && (line = in.readLine()) != null) {
                    handleCommand(line);
                }
            } catch (Exception e) {
                System.err.println("[CLIENT] Connection error: " + e.getMessage());
            } finally {
                isConnected = false;
                System.out.println("[CLIENT] Connection closed");
                cleanupConnection(); // reset socket/out/in
            }

            if (!shouldStop) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void cleanupConnection() {
        try { if (in != null) in.close(); } catch (Exception ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        out = null;
        socket = null;
        in = null;
    }

    /**
     * Kiểm tra webcam có sẵn không khi khởi động.
     * Nếu có, mở sẵn camera để chụp nhanh hơn.
     */
    private void checkCameraAvailability() {
        System.out.println("[WEBCAM] Checking camera availability...");
        cameraAvailable = CameraCaptureUtil.isCameraAvailable();
        if (cameraAvailable) {
            boolean opened = CameraCaptureUtil.openCamera();
            System.out.println("[WEBCAM] Camera found and " + (opened ? "opened" : "failed to open"));
            cameraAvailable = opened;
        } else {
            System.out.println("[WEBCAM] No camera detected - webcam features disabled");
        }
    }

    /**
     * Bật luồng chụp webcam định kỳ mỗi 10 giây.
     * Gửi ảnh mặt sinh viên về server qua lệnh WEBCAM:<base64>.
     */
    private void startWebcamCapture() {
        System.out.println("[WEBCAM] === Scheduler START ===");
        webcamScheduler = Executors.newSingleThreadScheduledExecutor();
        webcamScheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[WEBCAM] Tick - isConnected=" + isConnected
                    + ", out=" + (out != null)
                    + ", socket=" + (socket != null));

                if (!isConnected || out == null || socket == null
                    || socket.isClosed() || !socket.isConnected()) {
                    System.out.println("[WEBCAM] SKIP - connection not ready");
                    return;
                }

                System.out.println("[WEBCAM] Capturing frame...");
                String base64 = CameraCaptureUtil.captureBase64Jpeg();

                if (base64 == null || base64.isEmpty()) {
                    System.out.println("[WEBCAM] Capture returned null/empty");
                    return;
                }

                System.out.println("[WEBCAM] Captured, base64 len=" + base64.length());
                out.println("WEBCAM:" + base64);
                out.flush();
                System.out.println("[WEBCAM] Sent WEBCAM to server");
            } catch (Exception e) {
                System.err.println("[WEBCAM] ERROR: " + e.getClass().getName()
                    + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 5, 10, TimeUnit.SECONDS);
        System.out.println("[WEBCAM] Scheduler started (delay 5s, interval 10s)");
    }

    /**
     * Ghi clip webcam 5 giây trong background thread.
     * Gửi kết quả về server qua lệnh WEBCAM_CLIP:<base64 frames>.
     */
    private void recordAndSendClip() {
        if (!cameraAvailable) {
            System.out.println("[WEBCAM] Cannot record clip - no camera available");
            return;
        }

        // Ghi clip trong thread riêng để không block luồng đọc lệnh
        new Thread(() -> {
            try {
                System.out.println("[WEBCAM] Recording 5s clip...");
                String clipData = CameraCaptureUtil.recordClipAsString();
                if (clipData != null && !clipData.isEmpty() && out != null) {
                    out.println("WEBCAM_CLIP:" + clipData);
                    System.out.println("[WEBCAM] Sent clip data, total len=" + clipData.length());
                } else {
                    System.out.println("[WEBCAM] Clip recording returned empty");
                }
            } catch (Exception e) {
                System.err.println("[WEBCAM] Clip recording error: " + e.getMessage());
            }
        }, "webcam-clip-recorder").start();
    }

    private void startBehaviorMonitor() {
        monitorScheduler = Executors.newSingleThreadScheduledExecutor();
        monitorScheduler.scheduleAtFixedRate(() -> {
            try {
                String activeTitle = ActiveWindowUtil.getActiveWindowTitle();
                if (activeTitle.isEmpty() || activeTitle.equals(lastAlertTab)) {
                    return;
                }

                if (whitelist.isEmpty())
                    return;

                if (containsKeyword(activeTitle, whitelist)) {
                    lastAlertTab = activeTitle;
                    return;
                }

                // Gửi mã cảnh báo màu Đỏ về Server
                out.println("SUSPICIOUS:RED|" + activeTitle);
                System.out.println(">>> [Warning] Switch to invalid tab: " + activeTitle);

                lastAlertTab = activeTitle;
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS); // Mỗi 1 giây quét 1 lần
    }

    private boolean containsKeyword(String title, List<String> keywords) {
        String lowerTitle = title.toLowerCase();
        for (String kw : keywords) {
            if (kw.trim().isEmpty())
                continue;
            if (lowerTitle.contains(kw.toLowerCase().trim()))
                return true;
        }
        return false;
    }

    private void handleCommand(String command) {
        if (command.startsWith("ALERT:")) {
            // Hiển thị Popup thông báo từ Thầy giáo
            String message = command.substring(6);
            new Thread(() -> {
                JOptionPane.showMessageDialog(null, message, "Administrative Notification",
                        JOptionPane.WARNING_MESSAGE);
            }).start();
        } else if ("CAPTURE_SCREEN".equals(command)) {
            System.out.println("[CLIENT] CAPTURE_SCREEN received!");
            byte[] imgBytes = ScreenCaptureUtil.captureCurrentScreen();
            System.out.println("[CLIENT] Screenshot size: " + (imgBytes != null ? imgBytes.length : 0));
            if (imgBytes != null && imgBytes.length > 0) {
                String base64 = Base64.getEncoder().encodeToString(imgBytes);
                System.out.println("[CLIENT] Sending SCREEN, base64 len=" + base64.length());
                out.println("SCREEN:" + base64);
            }
        } else if ("CAPTURE_WEBCAM".equals(command)) {
            // Admin yêu cầu chụp webcam ngay lập tức
            System.out.println("[CLIENT] CAPTURE_WEBCAM received!");
            if (cameraAvailable) {
                String base64 = CameraCaptureUtil.captureBase64Jpeg();
                if (base64 != null) {
                    out.println("WEBCAM:" + base64);
                    System.out.println("[CLIENT] Sent webcam snapshot on demand");
                }
            } else {
                System.out.println("[CLIENT] No camera available for CAPTURE_WEBCAM");
            }
        } else if (command.equals("RECORD_WEBCAM") || command.startsWith("RECORD_WEBCAM:")) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRecordTime;
            if (elapsed < RECORD_COOLDOWN_MS) {
                long remaining = (RECORD_COOLDOWN_MS - elapsed) / 1000;
                System.out.println("[CLIENT] Skipping RECORD_WEBCAM (cooldown, "
                    + remaining + "s remaining)");
                return;
            }
            lastRecordTime = now;
            System.out.println("[CLIENT] RECORD_WEBCAM received! Starting 5s clip...");
            recordAndSendClip();
        } else if (command.startsWith("UPDATE_WHITELIST:")) {
            // Cập nhật luật thi
            String keywordsStr = command.substring(17);
            whitelist = new ArrayList<>(Arrays.asList(keywordsStr.split(",")));
            System.out.println(">>> Update whitelist: " + whitelist);
        }
    }

    private void cleanup() {
        isRunning = false;
        if (monitorScheduler != null)
            monitorScheduler.shutdownNow();
        if (webcamScheduler != null)
            webcamScheduler.shutdownNow();
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception ignored) {
        }
    }
}
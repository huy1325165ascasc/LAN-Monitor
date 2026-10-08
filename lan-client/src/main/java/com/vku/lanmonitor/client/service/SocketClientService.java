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
    private String lastAlertTab = "";
    private ScheduledExecutorService monitorScheduler;
    private ScheduledExecutorService webcamScheduler;

    private List<String> whitelist = new ArrayList<>();
    private boolean cameraAvailable = false;

    private static final int WEBCAM_CAPTURE_INTERVAL_SECONDS = 10;

    // Thêm studentId vào Constructor
    public SocketClientService(String serverIp, int serverPort, String studentId) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.studentId = studentId;
    }

    public void start() {
        // Kiểm tra webcam 1 lần khi khởi động
        checkCameraAvailability();

        while (true) {
            try {
                System.out.println("Connecting to Server [" + serverIp + ":" + serverPort + "]...");
                socket = new Socket(serverIp, serverPort);
                isRunning = true;
                System.out.println(">>> Connect server complete <<<");

                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                // QUAN TRỌNG NHẤT: Báo danh với Server ngay khi vừa kết nối!
                out.println("CONNECT:" + studentId);

                // Bật luồng soi màn hình
                startBehaviorMonitor();

                // Bật luồng chụp webcam định kỳ (nếu có camera)
                startWebcamCapture();

                String line;
                // Liên tục lắng nghe lệnh từ Thầy giáo (như Lệnh chụp ảnh, Cập nhật luật thi)
                while (isRunning && (line = in.readLine()) != null) {
                    handleCommand(line);
                }
            } catch (Exception e) {
                System.err.println("Disconnect: " + e.getMessage() + ". Retry after 5s...");
            } finally {
                cleanup();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }
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
        if (!cameraAvailable) {
            System.out.println("[WEBCAM] Skipping webcam capture - no camera available");
            return;
        }

        webcamScheduler = Executors.newSingleThreadScheduledExecutor();
        webcamScheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning || out == null) return;

                String base64 = CameraCaptureUtil.captureBase64Jpeg();
                if (base64 != null && !base64.isEmpty()) {
                    out.println("WEBCAM:" + base64);
                    System.out.println("[WEBCAM] Sent webcam snapshot, base64 len=" + base64.length());
                }
            } catch (Exception e) {
                System.err.println("[WEBCAM] Capture error: " + e.getMessage());
            }
        }, 2, WEBCAM_CAPTURE_INTERVAL_SECONDS, TimeUnit.SECONDS);

        System.out.println("[WEBCAM] Started periodic capture every " + WEBCAM_CAPTURE_INTERVAL_SECONDS + "s");
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
        } else if ("RECORD_WEBCAM".equals(command)) {
            // Server yêu cầu ghi clip 5 giây (khi phát hiện hành vi nghi ngờ)
            System.out.println("[CLIENT] RECORD_WEBCAM received! Starting 5s clip recording...");
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
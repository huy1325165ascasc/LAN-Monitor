package com.vku.lanmonitor.client.service;

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

    private List<String> whitelist = new ArrayList<>();

    // Thêm studentId vào Constructor
    public SocketClientService(String serverIp, int serverPort, String studentId) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.studentId = studentId;
    }

    public void start() {
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
            // Chụp ảnh và gửi lên
            byte[] imgBytes = ScreenCaptureUtil.captureCurrentScreen();
            if (imgBytes != null && imgBytes.length > 0) {
                String base64 = Base64.getEncoder().encodeToString(imgBytes);
                out.println("SCREEN:" + base64);
            }
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
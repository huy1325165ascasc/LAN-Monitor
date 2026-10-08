package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminMessageDispatcher;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.function.Consumer;

/**
 * Dialog hiển thị webcam realtime của 1 client cụ thể.
 * Nhận ảnh webcam từ server qua lệnh WEBCAM_STREAM và hiển thị liên tục.
 * Admin có thể chụp ảnh webcam ngay lập tức hoặc yêu cầu ghi clip 5s.
 */
public class WebcamDialog extends JDialog {
    private final AdminTcpClient tcpClient;
    private final AdminMessageDispatcher dispatcher;
    private final String clientId;
    private final JLabel imageLabel;
    private final JLabel statusLabel;
    private javax.swing.Timer captureTimer;
    private Consumer<String> messageHandler;

    public WebcamDialog(JFrame parent, AdminTcpClient tcpClient,
                        AdminMessageDispatcher dispatcher,
                        String clientId, String pcName) {
        super(parent, "Webcam - " + pcName, false);
        this.tcpClient = tcpClient;
        this.dispatcher = dispatcher;
        this.clientId = clientId;

        setSize(750, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));

        // === NORTH: Title bar + buttons ===
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(new Color(25, 80, 40));
        north.setPreferredSize(new Dimension(0, 50));

        JLabel title = new JLabel("  📷 WEBCAM: " + pcName);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        north.add(title, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnCapture = new JButton("Chụp ảnh");
        btnCapture.setBackground(new Color(50, 120, 200));
        btnCapture.setForeground(Color.WHITE);
        btnCapture.setOpaque(true);
        btnCapture.setBorderPainted(false);

        JButton btnRecordClip = new JButton("Ghi clip 5s");
        btnRecordClip.setBackground(new Color(200, 50, 50));
        btnRecordClip.setForeground(Color.WHITE);
        btnRecordClip.setOpaque(true);
        btnRecordClip.setBorderPainted(false);

        JButton btnClose = new JButton("Đóng");

        btnPanel.add(btnCapture);
        btnPanel.add(btnRecordClip);
        btnPanel.add(btnClose);
        north.add(btnPanel, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        // === CENTER: Webcam image ===
        imageLabel = new JLabel("Đang chờ ảnh webcam...", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.BLACK);
        imageLabel.setForeground(Color.WHITE);
        imageLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        add(imageLabel, BorderLayout.CENTER);

        // === SOUTH: Status bar ===
        statusLabel = new JLabel("Đang kết nối webcam...");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(220, 255, 220));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        // === Message handler ===
        messageHandler = this::handleServerMessage;
        dispatcher.addHandler(messageHandler);

        // Yêu cầu chụp webcam ngay lập tức để bắt đầu nhận stream
        tcpClient.sendCommand("CAPTURE_WEBCAM:" + clientId);

        // Timer định kỳ gửi lệnh chụp webcam (mỗi 2 giây)
        captureTimer = new javax.swing.Timer(2000, e -> {
            tcpClient.sendCommand("CAPTURE_WEBCAM:" + clientId);
        });
        captureTimer.start();

        // === Button actions ===
        btnCapture.addActionListener(e -> {
            tcpClient.sendCommand("CAPTURE_WEBCAM:" + clientId);
            statusLabel.setText("Đã gửi lệnh chụp webcam");
        });

        btnRecordClip.addActionListener(e -> {
            // Gửi lệnh ghi clip 5s qua server -> client
            tcpClient.sendCommand("CAPTURE_WEBCAM:" + clientId); // trigger thêm 1 frame
            // Server sẽ gửi RECORD_WEBCAM tới client khi có SUSPICIOUS,
            // nhưng admin cũng có thể trigger thủ công
            broadcastRecordCommand();
            statusLabel.setText("Đã gửi lệnh ghi clip 5 giây...");
            btnRecordClip.setEnabled(false);
            // Re-enable after 7 seconds
            new javax.swing.Timer(7000, ev -> {
                btnRecordClip.setEnabled(true);
                ((javax.swing.Timer) ev.getSource()).stop();
            }).start();
        });

        btnClose.addActionListener(e -> dispose());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup();
            }
        });

        setVisible(true);
    }

    /**
     * Gửi lệnh RECORD_WEBCAM thủ công từ admin.
     * Sử dụng CAPTURE_WEBCAM để trigger, nhưng cũng gửi lệnh đặc biệt.
     */
    private void broadcastRecordCommand() {
        // Gửi SAVE_FRAME để server forward RECORD_WEBCAM tới client
        tcpClient.sendCommand("SAVE_FRAME:" + clientId);
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("WEBCAM_STREAM:")) {
            String rest = message.substring("WEBCAM_STREAM:".length());
            int firstColon = rest.indexOf(':');
            if (firstColon < 0) return;
            int secondColon = rest.indexOf(':', firstColon + 1);
            if (secondColon < 0) return;
            String senderId = rest.substring(0, secondColon);
            if (!senderId.equals(clientId)) {
                return;
            }
            String base64 = rest.substring(secondColon + 1);
            SwingUtilities.invokeLater(() -> updateImage(base64));
        } else if (message.startsWith("WEBCAM_CLIP_READY:")) {
            // Thông báo clip đã được lưu trên server
            String payload = message.substring("WEBCAM_CLIP_READY:".length());
            String[] parts = payload.split("\\|");
            if (parts.length >= 2 && parts[0].equals(clientId)) {
                SwingUtilities.invokeLater(() ->
                    statusLabel.setText("✅ Clip webcam đã lưu cho " + parts[1] +
                            " lúc " + new java.text.SimpleDateFormat("HH:mm:ss")
                                    .format(new java.util.Date()))
                );
            }
        }
    }

    private void updateImage(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return;

            int lw = imageLabel.getWidth();
            int lh = imageLabel.getHeight();
            if (lw <= 0 || lh <= 0) {
                imageLabel.setIcon(new ImageIcon(img));
                return;
            }
            double scale = Math.min((double) lw / img.getWidth(),
                                    (double) lh / img.getHeight());
            int nw = (int) (img.getWidth() * scale);
            int nh = (int) (img.getHeight() * scale);
            Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_FAST);
            imageLabel.setIcon(new ImageIcon(scaled));
            imageLabel.setText("");
            statusLabel.setText("📷 Webcam cập nhật: " + new java.text.SimpleDateFormat("HH:mm:ss")
                    .format(new java.util.Date()));
        } catch (Exception e) {
            statusLabel.setText("Lỗi decode ảnh webcam: " + e.getMessage());
        }
    }

    private void cleanup() {
        if (captureTimer != null) captureTimer.stop();
        if (messageHandler != null) dispatcher.removeHandler(messageHandler);
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
}

package com.vku.lanmonitor.student.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.student.net.StudentTcpClient;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StudentHomeFrame extends JFrame {
    private final String studentId;
    private final String serverHost;
    private final int serverPort;
    private final StudentTcpClient tcpClient;
    private final DefaultListModel<String> listModel;
    private final JList<String> quizList;
    private final JLabel lblStatus;
    private List<Map<String, Object>> quizzesData;

    public StudentHomeFrame(String studentId, String serverHost, int serverPort) {
        super("Phòng chờ - LAN Monitor");
        this.studentId = studentId;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.tcpClient = new StudentTcpClient();

        // Frame
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // NORTH — header
        JPanel north = new JPanel();
        north.setBackground(new Color(220, 220, 220));
        north.add(new JLabel("Xin chào: " + studentId));
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.addActionListener(e -> handleLogout());
        north.add(btnLogout);

        // CENTER — JList
        listModel = new DefaultListModel<>();
        listModel.addElement("ĐANG TẢI DANH SÁCH BÀI THI...");
        quizList = new JList<>(listModel);
        quizList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(quizList);

        // SOUTH — status
        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(255, 255, 200));
        lblStatus.setPreferredSize(new Dimension(0, 40));

        // Layout
        setLayout(new BorderLayout());
        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        System.out.println("[DEBUG] === StudentHomeFrame constructor START ===");

        // Kết nối server trong thread nền
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] Thread connect started");
                tcpClient.setMessageListener(this::handleServerMessage);
                System.out.println("[DEBUG] Listener set");
                tcpClient.connect(serverHost, serverPort);
                System.out.println("[DEBUG] TCP connected");
                tcpClient.sendCommand("REQ_QUIZ_LIST");
                System.out.println("[DEBUG] REQ_QUIZ_LIST sent");
            } catch (Exception ex) {
                System.err.println("[DEBUG] connect thread ERROR: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();

        setVisible(true);
        System.out.println("[DEBUG] === Constructor END, visible=" + isVisible() + " ===");
    }

    private void handleServerMessage(String message) {
        System.out.println("[DEBUG] handleServerMessage: " + 
            message.substring(0, Math.min(60, message.length())));
        if (message.startsWith("RES_QUIZ_LIST:")) {
            String json = message.substring("RES_QUIZ_LIST:".length());
            System.out.println("[DEBUG] Got RES_QUIZ_LIST, json len: " + json.length());
            SwingUtilities.invokeLater(() -> updateQuizList(json));
        } else {
            System.out.println("[DEBUG] Ignored (unknown prefix)");
        }
    }

    @SuppressWarnings("unchecked")
    private void updateQuizList(String json) {
        System.out.println("[DEBUG] === updateQuizList START ===");
        System.out.println("[DEBUG] json length: " + json.length());
        System.out.println("[DEBUG] updateQuizList, len=" + json.length());
        try {
            ObjectMapper mapper = new ObjectMapper();
            quizzesData = mapper.readValue(json, List.class);
            System.out.println("[DEBUG] parsed " + quizzesData.size());

            // Xóa "ĐANG TẢI" cũ
            listModel.clear();

            if (quizzesData.isEmpty()) {
                listModel.addElement("Chưa có bài thi nào");
            } else {
                for (int i = 0; i < quizzesData.size(); i++) {
                    Map<String, Object> quiz = quizzesData.get(i);
                    String name = String.valueOf(quiz.get("name"));
                    Object tl = quiz.get("timeLimit");
                    List<?> data = (List<?>) quiz.get("data");
                    int soCau = data != null ? data.size() : 0;
                    listModel.addElement(String.format("[%d] %s — %s phút — %d câu",
                            i + 1, name, tl, soCau));
                }
            }

            setTitle("Phòng chờ - LAN Monitor (" + quizzesData.size() + " bài thi)");
            lblStatus.setText("Đã tải " + quizzesData.size() + " bài thi");
            System.out.println("[DEBUG] updateQuizList DONE");
        } catch (Exception e) {
            e.printStackTrace();
            listModel.clear();
            listModel.addElement("LỖI: " + e.getMessage());
            lblStatus.setText("Lỗi parse");
        }
        System.out.println("[DEBUG] === updateQuizList END ===");
    }

    private void handleLogout() {
        tcpClient.disconnect();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

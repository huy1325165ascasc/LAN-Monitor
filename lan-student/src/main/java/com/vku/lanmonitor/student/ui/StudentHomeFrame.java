package com.vku.lanmonitor.student.ui;

import javax.swing.*;
import java.awt.*;

public class StudentHomeFrame extends JFrame {
    private String studentId;
    private String serverHost;
    private int serverPort;

    public StudentHomeFrame(String studentId, String serverHost, int serverPort) {
        this.studentId = studentId;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        
        initComponents();
    }

    private void initComponents() {
        setTitle("Phòng chờ - LAN Monitor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Xin chào: " + studentId);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        JLabel lblInfo = new JLabel("Server: " + serverHost + ":" + serverPort);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblInfo);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        JLabel lblPlaceholder = new JLabel("StudentHomeFrame - Đang phát triển...");
        lblPlaceholder.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPlaceholder.setFont(new Font("Arial", Font.ITALIC, 14));
        mainPanel.add(lblPlaceholder);

        add(mainPanel);
    }
}
package com.vku.lanmonitor.student.ui;

import com.vku.lanmonitor.student.net.StudentTcpClient;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtStudentId;
    private JPasswordField txtPassword;
    private JLabel lblServerInfo;
    private JButton btnLogin;
    private JButton btnSettings;
    private JLabel lblStatus;
    
    private StudentTcpClient tcpClient;
    private String serverHost = "localhost";
    private int serverPort = 9999;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng nhập - LAN Monitor");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        tcpClient = new StudentTcpClient();
        tcpClient.setMessageListener(this::handleServerMessage);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel lblTitle = new JLabel("Đăng nhập sinh viên");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel panelStudentId = new JPanel(new BorderLayout(5, 5));
        panelStudentId.add(new JLabel("Mã Sinh Viên:"), BorderLayout.NORTH);
        txtStudentId = new JTextField(20);
        panelStudentId.add(txtStudentId, BorderLayout.CENTER);
        mainPanel.add(panelStudentId);
        mainPanel.add(Box.createVerticalStrut(15));

        JPanel panelPassword = new JPanel(new BorderLayout(5, 5));
        panelPassword.add(new JLabel("Mật khẩu:"), BorderLayout.NORTH);
        txtPassword = new JPasswordField(20);
        panelPassword.add(txtPassword, BorderLayout.CENTER);
        mainPanel.add(panelPassword);
        mainPanel.add(Box.createVerticalStrut(15));

        lblServerInfo = new JLabel("Server: " + serverHost + ":" + serverPort);
        lblServerInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblServerInfo.setForeground(Color.GRAY);
        lblServerInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblServerInfo);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnLogin = new JButton("Đăng nhập");
        btnSettings = new JButton("Cấu hình Server");
        
        btnLogin.addActionListener(e -> handleLogin());
        btnSettings.addActionListener(e -> showSettingsDialog());
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnSettings);
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblStatus);

        add(mainPanel);

        txtStudentId.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> handleLogin());
    }

    private void showSettingsDialog() {
        JTextField hostField = new JTextField(serverHost, 15);
        JTextField portField = new JTextField(String.valueOf(serverPort), 5);
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Server IP:"));
        panel.add(hostField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Cấu hình Server", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                serverHost = hostField.getText().trim();
                serverPort = Integer.parseInt(portField.getText().trim());
                lblServerInfo.setText("Server: " + serverHost + ":" + serverPort);
                lblStatus.setText(" ");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Port phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLogin() {
        String studentId = txtStudentId.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (studentId.isEmpty()) {
            showStatus("Vui lòng nhập mã sinh viên!", Color.RED);
            txtStudentId.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showStatus("Vui lòng nhập mật khẩu!", Color.RED);
            txtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnSettings.setEnabled(false);

        if (!tcpClient.isConnected()) {
            try {
                tcpClient.connect(serverHost, serverPort);
            } catch (Exception e) {
                showStatus("Không thể kết nối server: " + e.getMessage(), Color.RED);
                btnLogin.setEnabled(true);
                btnSettings.setEnabled(true);
                return;
            }
        }

        showStatus("Đang đăng nhập...", Color.BLUE);
        tcpClient.sendCommand("STUDENT_LOGIN:" + studentId + ":" + password);
    }

    private void handleServerMessage(String message) {
        System.out.println("[DEBUG] === Nhận từ server: [" + message + "]");
        SwingUtilities.invokeLater(() -> {
            System.out.println("[DEBUG] Lambda đang xử lý: [" + message + "]");
            if (message.startsWith("LOGIN_OK")) {
                showStatus("Đăng nhập thành công!", new Color(0, 150, 0));
                openStudentHome(txtStudentId.getText().trim());
            } else if (message.startsWith("LOGIN_FAIL")) {
                showStatus("Đăng nhập thất bại!", Color.RED);
                btnLogin.setEnabled(true);
                btnSettings.setEnabled(true);
            } else if (message.startsWith("ERROR:")) {
                showStatus(message.substring(6), Color.RED);
                btnLogin.setEnabled(true);
                btnSettings.setEnabled(true);
            }
        });
    }

    private void openStudentHome(String studentId) {
        System.out.println("[DEBUG] openStudentHome: " + studentId);
        try {
            // 1. Tạo frame mới trước
            StudentHomeFrame homeFrame = new StudentHomeFrame(studentId, serverHost, serverPort);
            System.out.println("[DEBUG] Đã tạo StudentHomeFrame");
            
            // 2. Đóng frame login
            this.dispose();
            System.out.println("[DEBUG] LoginFrame đã dispose");
            
            // 3. Hiện frame mới
            homeFrame.setVisible(true);
            System.out.println("[DEBUG] StudentHomeFrame đã hiển thị");
            
            // 4. Disconnect TCP
            tcpClient.disconnect();
            System.out.println("[DEBUG] Đã disconnect TCP");
            
        } catch (Exception e) {
            System.err.println("[DEBUG] LỖI openStudentHome: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }

    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSED) {
            tcpClient.disconnect();
        }
    }
}
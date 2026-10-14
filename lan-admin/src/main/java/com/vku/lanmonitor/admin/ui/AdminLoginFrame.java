package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminTcpClient;
import com.vku.lanmonitor.admin.service.ProcessManager;
import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Admin Login Frame with integrated Server control and real-time status check.
 */
public class AdminLoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtServerHost;
    private JTextField txtServerPort;
    private JButton btnLogin;
    private JButton btnStartServer;
    private JLabel lblStatus;
    private JLabel serverStatusLabel;
    private AdminTcpClient tcpClient;
    private final ProcessManager processManager;
    private javax.swing.Timer statusCheckTimer;

    public AdminLoginFrame() {
        this.processManager = new ProcessManager();
        initComponents();
        initServerStatusCheck();
    }

    private void initComponents() {
        setTitle("Đăng nhập Giám thị - LAN Monitor");
        setSize(450, 490);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tcpClient != null) {
                    tcpClient.disconnect();
                }
                processManager.stopAll();
                dispose();
                System.exit(0);
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Title
        JLabel lblTitle = new JLabel("Đăng nhập Giám thị");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(15));

        // Server Status Panel
        JPanel serverStatusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        serverStatusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        serverStatusLabel = new JLabel("Server: OFFLINE");
        serverStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        serverStatusLabel.setForeground(LanMonitorTheme.RED_BTN);
        serverStatusPanel.add(serverStatusLabel);
        mainPanel.add(serverStatusPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Start Server Button
        btnStartServer = LanMonitorTheme.createPrimaryButton("Khởi động Server", LanMonitorTheme.GREEN_BTN);
        btnStartServer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStartServer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnStartServer.addActionListener(e -> startServerFromLogin());
        mainPanel.add(btnStartServer);
        mainPanel.add(Box.createVerticalStrut(15));

        // Username
        JPanel panelUser = new JPanel(new BorderLayout(5, 3));
        panelUser.add(new JLabel("Tài khoản:"), BorderLayout.NORTH);
        txtUsername = new JTextField("admin", 20);
        panelUser.add(txtUsername, BorderLayout.CENTER);
        mainPanel.add(panelUser);
        mainPanel.add(Box.createVerticalStrut(10));

        // Password
        JPanel panelPass = new JPanel(new BorderLayout(5, 3));
        panelPass.add(new JLabel("Mật khẩu:"), BorderLayout.NORTH);
        txtPassword = new JPasswordField("123456", 20);
        panelPass.add(txtPassword, BorderLayout.CENTER);
        mainPanel.add(panelPass);
        mainPanel.add(Box.createVerticalStrut(10));

        // Server Host
        JPanel panelHost = new JPanel(new BorderLayout(5, 3));
        panelHost.add(new JLabel("Server IP:"), BorderLayout.NORTH);
        txtServerHost = new JTextField("localhost", 20);
        panelHost.add(txtServerHost, BorderLayout.CENTER);
        mainPanel.add(panelHost);
        mainPanel.add(Box.createVerticalStrut(10));

        // Server Port
        JPanel panelPort = new JPanel(new BorderLayout(5, 3));
        panelPort.add(new JLabel("Server Port:"), BorderLayout.NORTH);
        txtServerPort = new JTextField("9999", 20);
        panelPort.add(txtServerPort, BorderLayout.CENTER);
        mainPanel.add(panelPort);
        mainPanel.add(Box.createVerticalStrut(18));

        // Button Login
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(40, 120, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.addActionListener(e -> handleLogin());
        mainPanel.add(btnLogin);
        mainPanel.add(Box.createVerticalStrut(12));

        // Status label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblStatus);

        add(mainPanel);

        // Enter key shortcuts
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
        txtPassword.addActionListener(e -> handleLogin());
        txtServerHost.addActionListener(e -> handleLogin());
        txtServerPort.addActionListener(e -> handleLogin());
    }

    private void initServerStatusCheck() {
        statusCheckTimer = new javax.swing.Timer(2000, e -> refreshServerStatus());
        statusCheckTimer.start();
    }

    private void refreshServerStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return processManager.isPortInUse(9999);
            }

            @Override
            protected void done() {
                try {
                    boolean online = get();
                    SwingUtilities.invokeLater(() -> {
                        serverStatusLabel.setText(online ? "Server: ONLINE" : "Server: OFFLINE");
                        serverStatusLabel.setForeground(online ? LanMonitorTheme.GREEN_BTN : LanMonitorTheme.RED_BTN);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[AdminLoginFrame] Status check interrupted: " + e.getMessage());
                } catch (ExecutionException e) {
                    System.err.println("[AdminLoginFrame] Status check failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private void startServerFromLogin() {
        btnStartServer.setEnabled(false);
        btnStartServer.setText("Đang khởi động...");
        new Thread(() -> {
            try {
                processManager.startServer();
                // Wait and check port via SwingWorker
                javax.swing.Timer checkTimer = new javax.swing.Timer(5000, evt -> {
                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            return processManager.isPortInUse(9999);
                        }

                        @Override
                        protected void done() {
                            try {
                                boolean online = get();
                                SwingUtilities.invokeLater(() -> {
                                    serverStatusLabel.setText(online ? "Server: ONLINE" : "Server: OFFLINE");
                                    serverStatusLabel.setForeground(online ? LanMonitorTheme.GREEN_BTN : LanMonitorTheme.RED_BTN);
                                    btnStartServer.setEnabled(true);
                                    btnStartServer.setText("Khởi động Server");
                                    if (online) {
                                        enableLoginButton();
                                    }
                                });
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                System.err.println("[AdminLoginFrame] Port check interrupted: " + e.getMessage());
                                resetStartButton();
                            } catch (ExecutionException e) {
                                System.err.println("[AdminLoginFrame] Port check failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                                resetStartButton();
                            }
                        }
                    }.execute();
                });
                checkTimer.setRepeats(false);
                checkTimer.start();
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Lỗi khởi động server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    resetStartButton();
                });
            }
        }, "LoginServerStartThread").start();
    }

    private void resetStartButton() {
        SwingUtilities.invokeLater(() -> {
            btnStartServer.setEnabled(true);
            btnStartServer.setText("Khởi động Server");
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String host = txtServerHost.getText().trim();
        String portStr = txtServerPort.getText().trim();

        if (username.isEmpty()) {
            showStatus("Vui lòng nhập tài khoản!", Color.RED);
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showStatus("Vui lòng nhập mật khẩu!", Color.RED);
            txtPassword.requestFocus();
            return;
        }
        if (host.isEmpty()) {
            showStatus("Vui lòng nhập Server IP!", Color.RED);
            txtServerHost.requestFocus();
            return;
        }
        if (portStr.isEmpty()) {
            showStatus("Vui lòng nhập Server Port!", Color.RED);
            txtServerPort.requestFocus();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            showStatus("Port phải là số nguyên!", Color.RED);
            txtServerPort.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        showStatus("Đang đăng nhập...", Color.BLUE);

        new Thread(() -> {
            try {
                if (tcpClient == null || !tcpClient.isConnected()) {
                    tcpClient = new AdminTcpClient();
                    tcpClient.setMessageListener(this::handleServerMessage);
                    tcpClient.connect(host, port);
                }
                tcpClient.sendCommand("ADMIN_LOGIN:" + username + ":" + password);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showStatus("Không thể kết nối server: " + ex.getMessage(), Color.RED);
                    btnLogin.setEnabled(true);
                });
            }
        }).start();
    }

    private void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("ADMIN_OK")) {
                showStatus("Đăng nhập thành công!", new Color(0, 150, 0));
                dispose();
                AdminMainFrame mainFrame = new AdminMainFrame(tcpClient);
            } else if (message.startsWith("ADMIN_FAIL")) {
                showStatus("Sai tài khoản hoặc mật khẩu", Color.RED);
                btnLogin.setEnabled(true);
            } else if (message.startsWith("ERROR:")) {
                showStatus(message.substring(6), Color.RED);
                btnLogin.setEnabled(true);
            }
        });
    }

    private void enableLoginButton() {
        btnLogin.setEnabled(true);
        lblStatus.setText("Server đã sẵn sàng. Bạn có thể đăng nhập.");
        lblStatus.setForeground(LanMonitorTheme.GREEN_BTN);
    }

    private void showStatus(String msg, Color color) {
        lblStatus.setText(msg);
        lblStatus.setForeground(color);
    }
}

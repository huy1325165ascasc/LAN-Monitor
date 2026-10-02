package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminTcpClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminMainFrame extends JFrame {
    private final AdminTcpClient tcpClient;
    private final String username;
    private JLabel lblStatus;

    public AdminMainFrame(AdminTcpClient tcpClient) {
        this(tcpClient, "admin");
    }

    public AdminMainFrame(AdminTcpClient tcpClient, String username) {
        super("Admin Dashboard - LAN Monitor");
        this.tcpClient = tcpClient;
        this.username = username;

        initComponents();
    }

    private void initComponents() {
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem itemExit = new JMenuItem("Thoát");
        itemExit.addActionListener(e -> handleExit());
        menuFile.add(itemExit);
        menuBar.add(menuFile);
        setJMenuBar(menuBar);

        setLayout(new BorderLayout());

        // JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

        // Tab 1: Dashboard
        JPanel pnlDashboard = createPlaceholderPanel("Coming soon: Phase 4B");
        tabbedPane.addTab("Dashboard", pnlDashboard);

        // Tab 2: Quản lý đề thi
        JPanel pnlQuizManage = createPlaceholderPanel("Coming soon: Phase 4C");
        tabbedPane.addTab("Quản lý đề thi", pnlQuizManage);

        // Tab 3: Cấu hình
        JPanel pnlConfig = createPlaceholderPanel("Coming soon: Phase 4D");
        tabbedPane.addTab("Cấu hình", pnlConfig);

        add(tabbedPane, BorderLayout.CENTER);

        // SOUTH: Status bar
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        southPanel.setBackground(new Color(235, 238, 242));
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 205, 210)));

        lblStatus = new JLabel("Admin: " + username + " | Đã kết nối server");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        southPanel.add(lblStatus);

        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }

    private void handleExit() {
        if (tcpClient != null) {
            tcpClient.disconnect();
        }
        dispose();
        System.exit(0);
    }
}

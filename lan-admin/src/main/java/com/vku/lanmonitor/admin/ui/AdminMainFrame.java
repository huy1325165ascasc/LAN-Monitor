package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminTcpClient;
import com.vku.lanmonitor.admin.net.AdminMessageDispatcher;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminMainFrame extends JFrame {
    private final AdminTcpClient tcpClient;
    private final AdminMessageDispatcher dispatcher;

    public AdminMainFrame(AdminTcpClient tcpClient) {
        super("Admin Dashboard - LAN Monitor");
        this.tcpClient = tcpClient;
        this.dispatcher = new AdminMessageDispatcher();
        
        tcpClient.setMessageListener(dispatcher::dispatch);

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

        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem itemExit = new JMenuItem("Thoát");
        itemExit.addActionListener(e -> handleExit());
        menuFile.add(itemExit);
        menuBar.add(menuFile);
        setJMenuBar(menuBar);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

        DashboardPanel dashboardPanel = new DashboardPanel(tcpClient, dispatcher);
        tabbedPane.addTab("Dashboard", dashboardPanel);

        QuizEditorPanel quizEditorPanel = new QuizEditorPanel(tcpClient, dispatcher);
        tabbedPane.addTab("Quản lý đề thi", quizEditorPanel);

        ConfigPanel configPanel = new ConfigPanel(tcpClient, dispatcher);
        tabbedPane.addTab("Cấu hình", configPanel);

        add(tabbedPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        southPanel.setBackground(new Color(235, 238, 242));
        southPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 205, 210)));

        JLabel lblStatus = new JLabel("Admin: admin | Đã kết nối server");
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

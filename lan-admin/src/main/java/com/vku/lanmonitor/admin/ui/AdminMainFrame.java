package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminMessageDispatcher;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import com.vku.lanmonitor.admin.service.ProcessManager;
import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Admin Dashboard frame with title bar, purple header, sidebar,
 * tabbed content, server log panel, and status bar.
 */
public class AdminMainFrame extends JFrame {

    private final AdminTcpClient tcpClient;
    private final AdminMessageDispatcher dispatcher;
    private final ProcessManager processManager;
    private final ServerLogPanel logPanel;
    private final LeftSidebarPanel leftSidebarPanel;

    private JLabel statusBadge;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public AdminMainFrame(AdminTcpClient tcpClient) {
        super("Admin Dashboard - LAN Monitor");
        this.tcpClient = tcpClient;
        this.dispatcher = new AdminMessageDispatcher();
        tcpClient.setMessageListener(dispatcher::dispatch);

        // Initialize ProcessManager, ServerLogPanel, and LeftSidebarPanel
        this.processManager = new ProcessManager();
        this.logPanel = new ServerLogPanel();
        this.leftSidebarPanel = new LeftSidebarPanel(processManager);
        this.leftSidebarPanel.setClearLogAction(this.logPanel::clear);

        processManager.setLogListener(line -> {
            logPanel.appendLine(line);
        });

        LanMonitorTheme.applyGlobalStyle();

        setLayout(new BorderLayout());
        initComponents();

        setSize(1520, 900);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        setVisible(true);
    }

    private void initComponents() {
        // Title Bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(1520, 28));
        titleBar.setBackground(new Color(0xF0, 0xF3, 0xF6));

        JPanel iconPanel = new JPanel();
        iconPanel.setBackground(LanMonitorTheme.PURPLE_HEADER);
        iconPanel.setPreferredSize(new Dimension(16, 16));
        JLabel iconLbl = new JLabel("LM", SwingConstants.CENTER);
        iconLbl.setForeground(Color.WHITE);
        iconLbl.setFont(LanMonitorTheme.FONT_SMALL);
        iconPanel.add(iconLbl);

        JLabel titleLbl = new JLabel("  Admin Dashboard - LAN Monitor");
        titleLbl.setFont(LanMonitorTheme.FONT_BASE);

        JPanel westTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 6));
        westTitle.setOpaque(false);
        westTitle.add(iconPanel);
        westTitle.add(titleLbl);
        titleBar.add(westTitle, BorderLayout.WEST);
        add(titleBar, BorderLayout.NORTH);

        // Header (purple)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(LanMonitorTheme.PURPLE_HEADER);
        headerPanel.setPreferredSize(new Dimension(1520, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel headerWest = new JPanel(new GridLayout(2, 1));
        headerWest.setOpaque(false);
        JLabel mainTitle = new JLabel("LAN MONITOR");
        mainTitle.setFont(LanMonitorTheme.FONT_TITLE.deriveFont(22f));
        mainTitle.setForeground(Color.WHITE);
        JLabel subTitle = new JLabel("Control Center v1.0");
        subTitle.setFont(LanMonitorTheme.FONT_BASE.deriveFont(12f));
        subTitle.setForeground(new Color(255, 255, 255, 180));
        headerWest.add(mainTitle);
        headerWest.add(subTitle);
        headerPanel.add(headerWest, BorderLayout.WEST);

        statusBadge = new JLabel("OFFLINE", SwingConstants.CENTER);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(LanMonitorTheme.RED_BTN);
        statusBadge.setForeground(Color.WHITE);
        statusBadge.setFont(LanMonitorTheme.FONT_BOLD);
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 16, 0, 0),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        headerPanel.add(statusBadge, BorderLayout.EAST);

        // Menu Bar
        JPanel menuBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        menuBar.setBackground(new Color(0xE4, 0xE9, 0xEF));
        menuBar.setPreferredSize(new Dimension(1520, 30));

        menuBar.add(createMenuLabel("File"));
        menuBar.add(new JLabel(" | "));
        menuBar.add(createTabButton("Dashboard", "dashboard"));
        menuBar.add(createTabButton("Quản lý đề thi", "quan-ly-de-thi"));
        menuBar.add(createTabButton("Cấu hình", "cau-hinh"));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.CENTER);
        topContainer.add(menuBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Main Workspace: Sidebar (left) + Content (center)
        JPanel workspace = new JPanel(new BorderLayout(6, 0));
        leftSidebarPanel.setPreferredSize(new Dimension(380, 0));
        workspace.add(leftSidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        DashboardPanel dashboardPanel = new DashboardPanel(tcpClient, dispatcher);
        QuizEditorPanel quizEditorPanel = new QuizEditorPanel(tcpClient, dispatcher);
        ConfigPanel configPanel = new ConfigPanel(tcpClient, dispatcher);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(quizEditorPanel, "quan-ly-de-thi");
        contentPanel.add(configPanel, "cau-hinh");

        workspace.add(contentPanel, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);

        // Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xF0, 0xF3, 0xF6));
        statusBar.setPreferredSize(new Dimension(1520, 24));
        statusBar.add(new JLabel("  Admin: admin | Đã kết nối server"), BorderLayout.WEST);

        JPanel statusEast = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        statusEast.setOpaque(false);
        JLabel timeLbl = new JLabel();
        timeLbl.setFont(LanMonitorTheme.FONT_SMALL);
        statusEast.add(timeLbl);
        statusEast.add(new JLabel("F:\\Do_An_Mang"));
        statusBar.add(statusEast, BorderLayout.EAST);

        // Place status bar after log so it stays at very bottom
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(logPanel, BorderLayout.CENTER);
        bottomContainer.add(statusBar, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        // Timer check status every 2 seconds on background thread
        new javax.swing.Timer(2000, e -> {
            new Thread(() -> {
                boolean online = processManager.isPortInUse(9999);
                SwingUtilities.invokeLater(() -> {
                    leftSidebarPanel.setServerOnline(online);
                    statusBadge.setText(online ? "ONLINE" : "OFFLINE");
                    statusBadge.setBackground(online ? LanMonitorTheme.GREEN_BTN : LanMonitorTheme.RED_BTN);
                });
            }, "StatusCheckThread").start();
        }).start();

        new Timer(1000, e -> timeLbl.setText("Cập nhật: " + new SimpleDateFormat("HH:mm:ss").format(new Date())))
                .start();
    }

    private JLabel createMenuLabel(String text) {
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(LanMonitorTheme.FONT_BASE);
        return lbl;
    }

    private JButton createTabButton(String text, String id) {
        JButton btn = new JButton(text);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(LanMonitorTheme.FONT_BASE);
        btn.addActionListener(e -> switchTab(id));
        return btn;
    }

    private void switchTab(String tabId) {
        cardLayout.show(contentPanel, tabId);
    }

    private void handleExit() {
        if (tcpClient != null) {
            tcpClient.disconnect();
        }
        processManager.stopAll();
        dispose();
        System.exit(0);
    }
}

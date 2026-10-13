package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.service.ProcessManager;
import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Left sidebar panel containing Server control and App launcher cards.
 * Uses {@link ProcessManager} for server lifecycle and app launching.
 */
public class LeftSidebarPanel extends JPanel {

    private static final int SIDEBAR_WIDTH = 380;

    private final ProcessManager processManager;
    private JLabel serverStatusBadge;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnClearLog;
    private JButton btnStartAll;

    /**
     * Creates a new LeftSidebarPanel.
     *
     * @param processManager the process manager for server/app lifecycle
     */
    public LeftSidebarPanel(ProcessManager processManager) {
        this.processManager = processManager;
        setBackground(LanMonitorTheme.DARK_SIDEBAR);
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(LanMonitorTheme.DARK_SIDEBAR);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        content.add(createSectionHeader("SERVER CONTROL"));
        content.add(Box.createVerticalStrut(8));
        content.add(createServerControlCard());
        content.add(Box.createVerticalStrut(16));
        content.add(createSectionHeader("APP LAUNCHER"));
        content.add(Box.createVerticalStrut(8));
        content.add(createAppCard("Student App", "Sinh vien lam bai thi trac nghiem", "lan-student", "lan-student-1.0.0-jar-with-dependencies.jar", LanMonitorTheme.GREEN_BTN, "STU"));
        content.add(Box.createVerticalStrut(6));
        content.add(createAppCard("Anti-cheat Client", "Giam sat may, chup man hinh", "lan-client", "lan-client-1.0.0-jar-with-dependencies.jar", LanMonitorTheme.ORANGE_BTN, "SEC"));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(LanMonitorTheme.DARK_SIDEBAR);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Returns whether the managed server process is currently running.
     *
     * @return true if the server is alive
     */
    public boolean isServerRunning() {
        return processManager.isServerRunning();
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LanMonitorTheme.FONT_BOLD);
        label.setForeground(LanMonitorTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createServerControlCard() {
        JPanel card = LanMonitorTheme.createDarkCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel("LM");
        iconLabel.setOpaque(true);
        iconLabel.setBackground(LanMonitorTheme.PURPLE_HEADER);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(LanMonitorTheme.FONT_SMALL);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(28, 28));

        JLabel serverLabel = new JLabel(">> SERVER");
        serverLabel.setFont(LanMonitorTheme.FONT_BOLD);
        serverLabel.setForeground(LanMonitorTheme.TEXT_MUTED);

        JLabel portLabel = new JLabel("port 9999");
        portLabel.setFont(LanMonitorTheme.FONT_SMALL);
        portLabel.setForeground(LanMonitorTheme.TEXT_MUTED);

        serverStatusBadge = new JLabel("OFFLINE");
        serverStatusBadge.setFont(LanMonitorTheme.FONT_BOLD);
        serverStatusBadge.setForeground(LanMonitorTheme.RED_BTN);

        headerRow.add(iconLabel);
        headerRow.add(serverLabel);
        headerRow.add(portLabel);
        headerRow.add(serverStatusBadge);
        card.add(headerRow);
        card.add(Box.createVerticalStrut(10));

        btnStart = LanMonitorTheme.createPrimaryButton("Start Server", LanMonitorTheme.GREEN_BTN);
        btnStop = LanMonitorTheme.createPrimaryButton("Stop Server", LanMonitorTheme.RED_BTN);
        btnClearLog = LanMonitorTheme.createSecondaryButton("X  Xoa log", LanMonitorTheme.DARK_CARD_HOVER);
        btnClearLog.setPreferredSize(new Dimension(340, 40));
        btnStartAll = createStartAllButton();

        btnStart.addActionListener(e -> startServerFlow());
        btnStop.addActionListener(e -> stopServerFlow());
        btnClearLog.addActionListener(e -> clearLog());
        btnStartAll.addActionListener(e -> startAllFlow());

        JPanel btnRow = new JPanel(new GridLayout(3, 1, 0, 6));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        btnRow.add(btnStartAll);
        JPanel subRow = new JPanel(new GridLayout(1, 3, 4, 0));
        subRow.setOpaque(false);
        subRow.add(btnStart);
        subRow.add(btnStop);
        subRow.add(btnClearLog);
        btnRow.add(subRow);

        card.add(btnRow);

        return card;
    }

    private JButton createStartAllButton() {
        JButton btn = new JButton(">> KHOI DONG TAT CA <<") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, LanMonitorTheme.PURPLE_HEADER, getWidth(), getHeight(), new Color(0x8B5CF6));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(LanMonitorTheme.FONT_BOLD.deriveFont(14f));
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(340, 50));
        btn.setMinimumSize(new Dimension(340, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createAppCard(String appName, String subtitle, String moduleName, String jarName, Color accentColor, String iconText) {
        JPanel card = LanMonitorTheme.createDarkCard();
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel iconLbl = new JLabel(iconText, SwingConstants.CENTER);
        iconLbl.setOpaque(true);
        iconLbl.setBackground(accentColor);
        iconLbl.setForeground(Color.WHITE);
        iconLbl.setFont(LanMonitorTheme.FONT_BOLD);
        iconLbl.setPreferredSize(new Dimension(48, 48));
        card.add(iconLbl, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        JLabel titleLbl = new JLabel(appName);
        titleLbl.setFont(LanMonitorTheme.FONT_BOLD);
        titleLbl.setForeground(LanMonitorTheme.TEXT_LIGHT);
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(LanMonitorTheme.FONT_SMALL);
        subLbl.setForeground(LanMonitorTheme.TEXT_MUTED);
        centerPanel.add(titleLbl);
        centerPanel.add(subLbl);
        card.add(centerPanel, BorderLayout.CENTER);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        eastPanel.setOpaque(false);
        JLabel statusLbl = new JLabel("○ Chua mo");
        statusLbl.setFont(LanMonitorTheme.FONT_SMALL);
        statusLbl.setForeground(LanMonitorTheme.TEXT_MUTED);

        JButton openBtn = LanMonitorTheme.createPrimaryButton("Mo", accentColor);
        openBtn.setPreferredSize(new Dimension(70, 32));
        openBtn.addActionListener(e -> {
            launchApp(appName, moduleName, jarName);
            statusLbl.setText("● Dang chay");
            statusLbl.setForeground(LanMonitorTheme.GREEN_BTN);
        });

        eastPanel.add(statusLbl);
        eastPanel.add(openBtn);
        card.add(eastPanel, BorderLayout.EAST);

        return card;
    }

    private void startServerFlow() {
        btnStart.setEnabled(false);
        new Thread(() -> {
            try {
                processManager.startServer();
                SwingUtilities.invokeLater(this::updateServerStatus);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                            "Loi khoi dong server: " + ex.getMessage(),
                            "Loi", JOptionPane.ERROR_MESSAGE);
                    btnStart.setEnabled(true);
                });
            }
        }, "ServerStartThread").start();
    }

    private void stopServerFlow() {
        processManager.stopServer();
        updateServerStatus();
    }

    private void startAllFlow() {
        btnStartAll.setEnabled(false);
        new Thread(() -> {
            try {
                // Bước 1: Kiểm tra và khởi động server
                if (!processManager.isServerRunning()) {
                    processManager.startServer();
                }

                // Bước 2: Đợi 5 giây bằng Timer (không block EDT)
                final javax.swing.Timer[] tHolder = new javax.swing.Timer[1];
                tHolder[0] = new javax.swing.Timer(5000, e -> {
                    new Thread(() -> {
                        boolean inUse = processManager.isPortInUse(9999);
                        if (inUse) {
                            tHolder[0].stop();
                            try {
                                processManager.launchApp("lan-student", "lan-student-1.0.0-jar-with-dependencies.jar");
                                processManager.launchApp("lan-client", "lan-client-1.0.0-jar-with-dependencies.jar");
                                SwingUtilities.invokeLater(() -> {
                                    btnStartAll.setEnabled(true);
                                    JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                                            "Đã khởi động thành công Server + Student + Client!",
                                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                                });
                            } catch (IOException ex) {
                                SwingUtilities.invokeLater(() -> {
                                    btnStartAll.setEnabled(true);
                                    JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                                            "Lỗi khởi động app: " + ex.getMessage(),
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        } else {
                            tHolder[0].stop();
                            SwingUtilities.invokeLater(() -> {
                                btnStartAll.setEnabled(true);
                                JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                                        "Server chưa sẵn sàng sau 5 giây!",
                                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                            });
                        }
                    }, "StartAllPortCheckThread").start();
                });
                tHolder[0].start();
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                            "Loi trong startAll: " + ex.getMessage(),
                            "Loi", JOptionPane.ERROR_MESSAGE);
                    btnStartAll.setEnabled(true);
                });
            }
        }, "StartAllThread").start();
    }

    private void updateServerStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean running = processManager.isServerRunning();
            if (running) {
                serverStatusBadge.setText("[DANG CHAY]");
                serverStatusBadge.setForeground(LanMonitorTheme.GREEN_BTN);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            } else {
                serverStatusBadge.setText("[DUNG]");
                serverStatusBadge.setForeground(LanMonitorTheme.RED_BTN);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
    }

    private void clearLog() {
        SwingUtilities.invokeLater(() -> {
            // TODO: Xoa log
        });
    }

    /**
     * Sets whether the server is online.
     *
     * @param online true if online
     */
    public void setServerOnline(boolean online) {
        SwingUtilities.invokeLater(() -> {
            serverStatusBadge.setText(online ? "ONLINE" : "OFFLINE");
            serverStatusBadge.setForeground(online ? LanMonitorTheme.GREEN_BTN : LanMonitorTheme.RED_BTN);
            btnStart.setEnabled(!online);
            btnStop.setEnabled(online);
            btnStartAll.setEnabled(!online);
        });
    }

    /**
     * Refreshes server online status asynchronously using SwingWorker.
     */
    public void refreshServerStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return processManager.isPortInUse(9999);
            }

            @Override
            protected void done() {
                try {
                    boolean online = get();
                    setServerOnline(online);
            } catch (Exception e) {
                // Ignore silent port check failure on close
                setServerOnline(false);
            }
            }
        }.execute();
    }

    private JLabel getAppStatusLabel(String appName) {
        // Implementation would need to track status labels
        return null;
    }

    private void launchApp(String appName, String moduleName, String jarName) {
        new Thread(() -> {
            try {
                processManager.launchApp(moduleName, jarName);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Loi khoi dong " + appName + ": " + ex.getMessage(),
                                "Loi", JOptionPane.ERROR_MESSAGE));
            }
        }, "AppLaunchThread-" + moduleName).start();
    }
}
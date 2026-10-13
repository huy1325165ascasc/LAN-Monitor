package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.service.ProcessManager;
import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.IOException;

/**
 * Left sidebar panel containing Server control box and App launcher cards.
 * Uses {@link ProcessManager} for server lifecycle and app launching.
 */
public class LeftSidebarPanel extends JPanel {

    private static final int SIDEBAR_WIDTH = 380;

    private final ProcessManager processManager;

    // Server control fields
    private JLabel serverStatusBadge;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnClearLog;
    private JButton btnStartAll;

    // App status labels
    private JLabel studentStatusLabel;
    private JLabel antiCheatStatusLabel;

    // Callback for clear log
    private Runnable onClearLog;

    public LeftSidebarPanel(ProcessManager processManager) {
        this.processManager = processManager;
        setBackground(LanMonitorTheme.DARK_SIDEBAR);
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        setLayout(new BorderLayout());

        add(buildContent(), BorderLayout.CENTER);

        updateServerStatus();
    }

    /**
     * Returns whether the managed server process is currently running.
     */
    public boolean isServerRunning() {
        return processManager.isServerRunning();
    }

    /**
     * Sets callback to invoke when user clicks "Xoa log" button.
     * Typically wired to {@code ServerLogPanel::clearLogs}.
     */
    public void setClearLogAction(Runnable action) {
        this.onClearLog = action;
    }

    /**
     * Updates server ONLINE/OFFLINE badge and enables/disables buttons.
     * Must be called on EDT.
     */
    public void setServerOnline(boolean online) {
        SwingUtilities.invokeLater(() -> {
            if (serverStatusBadge != null) {
                serverStatusBadge.setText(online ? "ONLINE" : "OFFLINE");
                serverStatusBadge.setForeground(online ? LanMonitorTheme.GREEN_BTN : LanMonitorTheme.RED_BTN);
            }
            if (btnStart != null)
                btnStart.setEnabled(!online);
            if (btnStop != null)
                btnStop.setEnabled(online);
        });
    }

    /**
     * Refreshes server online status asynchronously via SwingWorker.
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[LeftSidebarPanel] refreshServerStatus interrupted: " + e.getMessage());
                    setServerOnline(false);
                } catch (java.util.concurrent.ExecutionException e) {
                    System.err.println("[LeftSidebarPanel] refreshServerStatus failed: "
                            + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                    setServerOnline(false);
                }
            }
        }.execute();
    }

    // ============================== BUILD CONTENT ==============================

    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(LanMonitorTheme.DARK_SIDEBAR);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // -------- Server Control Section --------
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        content.add(createSectionHeader("SERVER CONTROL"), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 18, 0);
        content.add(createServerControlCard(), gbc);

        // -------- App Launcher Section --------
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        content.add(createSectionHeader("APP LAUNCHER"), gbc);

        JPanel studentCard = createAppCard(
                "Student App",
                "Sinh viên làm bài thi trắc nghiệm",
                "lan-student",
                "lan-student-1.0.0-jar-with-dependencies.jar",
                LanMonitorTheme.GREEN_BTN,
                "STU");
        studentStatusLabel = findStatusLabel(studentCard);

        JPanel antiCheatCard = createAppCard(
                "Anti-cheat Client",
                "Giám sát máy, chụp màn hình",
                "lan-client",
                "lan-client-1.0.0-jar-with-dependencies.jar",
                LanMonitorTheme.ORANGE_BTN,
                "SEC");
        antiCheatStatusLabel = findStatusLabel(antiCheatCard);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        content.add(studentCard, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(antiCheatCard, gbc);

        // Push everything to top
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(Box.createGlue(), gbc);

        return content;
    }

    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LanMonitorTheme.FONT_BOLD);
        label.setForeground(LanMonitorTheme.TEXT_MUTED);
        return label;
    }

    // ============================== SERVER CONTROL CARD
    // ==============================

    private JPanel createServerControlCard() {
        JPanel card = LanMonitorTheme.createDarkCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // ---- Header row: [LM] >> SERVER port 9999 [BADGE] ----
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        headerRow.setOpaque(false);

        JLabel icon = new JLabel("LM", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(LanMonitorTheme.PURPLE_HEADER);
        icon.setForeground(Color.WHITE);
        icon.setFont(LanMonitorTheme.FONT_SMALL);
        icon.setPreferredSize(new Dimension(28, 28));

        JLabel serverLabel = new JLabel(">> SERVER");
        serverLabel.setFont(LanMonitorTheme.FONT_BOLD);
        serverLabel.setForeground(LanMonitorTheme.TEXT_LIGHT);

        JLabel portLabel = new JLabel("port 9999");
        portLabel.setFont(LanMonitorTheme.FONT_SMALL);
        portLabel.setForeground(LanMonitorTheme.TEXT_MUTED);

        serverStatusBadge = new JLabel("OFFLINE");
        serverStatusBadge.setFont(LanMonitorTheme.FONT_BOLD);
        serverStatusBadge.setForeground(LanMonitorTheme.RED_BTN);

        headerRow.add(icon);
        headerRow.add(serverLabel);
        headerRow.add(portLabel);
        headerRow.add(serverStatusBadge);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(headerRow, c);

        // ---- Start All button ----
        btnStartAll = createStartAllButton();
        c.gridy = 1;
        c.insets = new Insets(0, 0, 8, 0);
        card.add(btnStartAll, c);

        // ---- Sub-row: Start / Stop / Clear Log ----
        JPanel subRow = new JPanel(new GridLayout(1, 3, 6, 0));
        subRow.setOpaque(false);

        btnStart = LanMonitorTheme.createPrimaryButton("Start", LanMonitorTheme.GREEN_BTN);
        btnStart.setPreferredSize(new Dimension(0, 38));
        btnStop = LanMonitorTheme.createPrimaryButton("Stop", LanMonitorTheme.RED_BTN);
        btnStop.setPreferredSize(new Dimension(0, 38));
        btnClearLog = LanMonitorTheme.createSecondaryButton("Xóa log", LanMonitorTheme.DARK_CARD_HOVER);
        btnClearLog.setPreferredSize(new Dimension(0, 38));

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        btnStart.addActionListener(e -> startServerFlow());
        btnStop.addActionListener(e -> stopServerFlow());
        btnClearLog.addActionListener(e -> clearLog());
        btnStartAll.addActionListener(e -> startAllFlow());

        subRow.add(btnStart);
        subRow.add(btnStop);
        subRow.add(btnClearLog);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        card.add(subRow, c);

        return card;
    }

    private JButton createStartAllButton() {
        JButton btn = new JButton(">>  KHỞI ĐỘNG TẤT CẢ  <<") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, LanMonitorTheme.PURPLE_HEADER,
                        getWidth(), getHeight(), new Color(0x8B5CF6));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(LanMonitorTheme.FONT_BOLD.deriveFont(13f));
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(0, 48));
        btn.setMinimumSize(new Dimension(200, 48));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ============================== APP CARD ==============================

    private JPanel createAppCard(String appName, String subtitle,
            String moduleName, String jarName,
            Color accentColor, String iconText) {
        JPanel card = LanMonitorTheme.createDarkCard();
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(0, 72));

        // WEST: icon
        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(accentColor);
        icon.setForeground(Color.WHITE);
        icon.setFont(LanMonitorTheme.FONT_BOLD.deriveFont(13f));
        icon.setPreferredSize(new Dimension(48, 48));
        card.add(icon, BorderLayout.WEST);

        // CENTER: title + subtitle
        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);

        JLabel title = new JLabel(appName);
        title.setFont(LanMonitorTheme.FONT_BOLD);
        title.setForeground(LanMonitorTheme.TEXT_LIGHT);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(LanMonitorTheme.FONT_SMALL);
        sub.setForeground(LanMonitorTheme.TEXT_MUTED);

        center.add(title);
        center.add(sub);
        card.add(center, BorderLayout.CENTER);

        // EAST: status + Open button
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        east.setOpaque(false);

        JLabel status = new JLabel("Chưa mở");
        status.setName("statusLabel");
        status.setFont(LanMonitorTheme.FONT_SMALL);
        status.setForeground(LanMonitorTheme.TEXT_MUTED);

        JButton open = LanMonitorTheme.createPrimaryButton("Mở", accentColor);
        open.setPreferredSize(new Dimension(60, 32));
        open.setFont(LanMonitorTheme.FONT_BOLD.deriveFont(12f));
        open.addActionListener(e -> {
            launchApp(appName, moduleName, jarName);
            status.setText("Đang chạy");
            status.setForeground(LanMonitorTheme.GREEN_BTN);
        });

        east.add(status);
        east.add(open);
        card.add(east, BorderLayout.EAST);

        return card;
    }

    /** Helper to find the status JLabel inside a card (by name). */
    private JLabel findStatusLabel(JPanel card) {
        return findLabelByName(card, "statusLabel");
    }

    private JLabel findLabelByName(java.awt.Container container, String name) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel && name.equals(c.getName())) {
                return (JLabel) c;
            }
            if (c instanceof java.awt.Container) {
                JLabel found = findLabelByName((java.awt.Container) c, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    // ============================== ACTIONS ==============================

    private void startServerFlow() {
        btnStart.setEnabled(false);
        new Thread(() -> {
            try {
                processManager.startServer();
                SwingUtilities.invokeLater(this::updateServerStatus);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khởi động server: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    btnStart.setEnabled(true);
                });
            }
        }, "ServerStartThread").start();
    }

    private void stopServerFlow() {
        processManager.stopServer();
        updateServerStatus();
        if (studentStatusLabel != null) {
            studentStatusLabel.setText("Chưa mở");
            studentStatusLabel.setForeground(LanMonitorTheme.TEXT_MUTED);
        }
        if (antiCheatStatusLabel != null) {
            antiCheatStatusLabel.setText("Chưa mở");
            antiCheatStatusLabel.setForeground(LanMonitorTheme.TEXT_MUTED);
        }
    }

    private void startAllFlow() {
        btnStartAll.setEnabled(false);
        btnStartAll.setText("ĐANG KHỞI ĐỘNG...");

        new Thread(() -> {
            try {
                // Step 1: Start server if not running
                if (!processManager.isServerRunning()) {
                    processManager.startServer();
                    // Wait up to 10s for port 9999
                    boolean ready = false;
                    for (int i = 0; i < 20 && !ready; i++) {
                        Thread.sleep(500);
                        ready = processManager.isPortInUse(9999);
                    }
                    if (!ready) {
                        throw new IOException("Server không sẵn sàng sau 10 giây");
                    }
                }

                // Step 2: Launch apps
                processManager.launchApp("lan-student", "lan-student-1.0.0-jar-with-dependencies.jar");
                processManager.launchApp("lan-client", "lan-client-1.0.0-jar-with-dependencies.jar");

                SwingUtilities.invokeLater(() -> {
                    btnStartAll.setEnabled(true);
                    btnStartAll.setText(">>  KHỞI ĐỘNG TẤT CẢ  <<");
                    if (studentStatusLabel != null) {
                        studentStatusLabel.setText("Đang chạy");
                        studentStatusLabel.setForeground(LanMonitorTheme.GREEN_BTN);
                    }
                    if (antiCheatStatusLabel != null) {
                        antiCheatStatusLabel.setText("Đang chạy");
                        antiCheatStatusLabel.setForeground(LanMonitorTheme.GREEN_BTN);
                    }
                    JOptionPane.showMessageDialog(this,
                            "Đã khởi động Server + Student + Anti-cheat!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> {
                    btnStartAll.setEnabled(true);
                    btnStartAll.setText(">>  KHỞI ĐỘNG TẤT CẢ  <<");
                    System.err.println("[LeftSidebarPanel] startAll interrupted: " + ie.getMessage());
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    btnStartAll.setEnabled(true);
                    btnStartAll.setText(">>  KHỞI ĐỘNG TẤT CẢ  <<");
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khởi động: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "StartAllThread").start();
    }

    private void clearLog() {
        if (onClearLog != null) {
            SwingUtilities.invokeLater(onClearLog);
        }
    }

    private void updateServerStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean running = processManager.isServerRunning();
            if (running) {
                serverStatusBadge.setText("ONLINE");
                serverStatusBadge.setForeground(LanMonitorTheme.GREEN_BTN);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            } else {
                serverStatusBadge.setText("OFFLINE");
                serverStatusBadge.setForeground(LanMonitorTheme.RED_BTN);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
    }

    private void launchApp(String appName, String moduleName, String jarName) {
        new Thread(() -> {
            try {
                processManager.launchApp(moduleName, jarName);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                        "Lỗi khởi động " + appName + ": " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }, "AppLaunchThread-" + moduleName).start();
    }
}
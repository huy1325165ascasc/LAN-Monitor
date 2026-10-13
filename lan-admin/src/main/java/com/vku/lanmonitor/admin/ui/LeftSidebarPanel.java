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

    private static final int SIDEBAR_WIDTH = 280;
    private static final int MAX_LOG_LINES = 500;

    private final ProcessManager processManager;
    private JLabel serverStatusLabel;
    private JButton btnStart;
    private JButton btnStop;

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
        content.add(createAppCard("Admin Dashboard", "lan-admin", "lan-admin-1.0.0-jar-with-dependencies.jar"));
        content.add(Box.createVerticalStrut(6));
        content.add(createAppCard("Student Client", "lan-client", "lan-client-1.0.0-jar-with-dependencies.jar"));
        content.add(Box.createVerticalStrut(6));
        content.add(createAppCard("Quiz Editor", "lan-quiz", "lan-quiz-1.0.0-jar-with-dependencies.jar"));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(LanMonitorTheme.DARK_SIDEBAR);
        add(scrollPane, BorderLayout.CENTER);

        updateServerStatus();
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

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel("LM");
        iconLabel.setOpaque(true);
        iconLabel.setBackground(LanMonitorTheme.PURPLE_HEADER);
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setFont(LanMonitorTheme.FONT_SMALL);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(28, 28));

        serverStatusLabel = new JLabel("[DUNG]");
        serverStatusLabel.setFont(LanMonitorTheme.FONT_BOLD);
        serverStatusLabel.setForeground(LanMonitorTheme.RED_BTN);

        statusRow.add(iconLabel);
        statusRow.add(new JLabel(" ") {{ setForeground(LanMonitorTheme.TEXT_LIGHT); setFont(LanMonitorTheme.FONT_BOLD); }});
        statusRow.add(serverStatusLabel);
        card.add(statusRow);
        card.add(Box.createVerticalStrut(10));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        btnStart = LanMonitorTheme.createPrimaryButton("Start", LanMonitorTheme.GREEN_BTN);
        btnStop = LanMonitorTheme.createPrimaryButton("Stop", LanMonitorTheme.RED_BTN);

        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());

        btnRow.add(btnStart);
        btnRow.add(btnStop);
        card.add(btnRow);

        return card;
    }

    private JPanel createAppCard(String appName, String moduleName, String jarName) {
        JPanel card = LanMonitorTheme.createDarkCard();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel(appName);
        nameLabel.setFont(LanMonitorTheme.FONT_BASE);
        nameLabel.setForeground(LanMonitorTheme.TEXT_LIGHT);
        card.add(nameLabel, BorderLayout.CENTER);

        JLabel arrow = new JLabel(">");
        arrow.setFont(LanMonitorTheme.FONT_BOLD);
        arrow.setForeground(LanMonitorTheme.TEXT_MUTED);
        card.add(arrow, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                launchApp(appName, moduleName, jarName);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(LanMonitorTheme.DARK_CARD_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(LanMonitorTheme.DARK_CARD);
            }
        });

        return card;
    }

    private void startServer() {
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

    private void stopServer() {
        processManager.stopServer();
        updateServerStatus();
    }

    private void launchApp(String appName, String moduleName, String jarName) {
        new Thread(() -> {
            try {
                processManager.launchApp(moduleName, jarName);
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(LeftSidebarPanel.this,
                                "Loi khoi dong " + appName + ": " + ex.getMessage(),
                                "Loi", JOptionPane.ERROR_MESSAGE));
            }
        }, "AppLaunchThread-" + moduleName).start();
    }

    private void updateServerStatus() {
        SwingUtilities.invokeLater(() -> {
            boolean running = processManager.isServerRunning();
            if (running) {
                serverStatusLabel.setText("[DANG CHAY]");
                serverStatusLabel.setForeground(LanMonitorTheme.GREEN_BTN);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            } else {
                serverStatusLabel.setText("[DUNG]");
                serverStatusLabel.setForeground(LanMonitorTheme.RED_BTN);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
    }
}

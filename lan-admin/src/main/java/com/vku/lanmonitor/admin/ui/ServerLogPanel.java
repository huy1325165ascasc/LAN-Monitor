package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Dark-themed console panel for displaying real-time server logs.
 * Thread-safe: {@link #appendLine(String)} marshals updates to the EDT.
 */
public class ServerLogPanel extends JPanel {

    private static final int MAX_LOG_LINES = 1000;
    private static final int PANEL_HEIGHT = 180;

    private final JTextArea logArea;
    private int lineCount;

    /**
     * Creates a new ServerLogPanel with dark console styling.
     */
    public ServerLogPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, PANEL_HEIGHT));
        setBackground(LanMonitorTheme.LOG_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LanMonitorTheme.LOG_BG);
        header.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel titleLabel = new JLabel("SERVER LOG");
        titleLabel.setFont(LanMonitorTheme.FONT_BOLD);
        titleLabel.setForeground(LanMonitorTheme.TEXT_MUTED);
        header.add(titleLabel, BorderLayout.WEST);

        JButton clearBtn = LanMonitorTheme.createSecondaryButton("Clear");
        clearBtn.setPreferredSize(new Dimension(60, 24));
        clearBtn.addActionListener(e -> clear());
        header.add(clearBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(LanMonitorTheme.LOG_BG);
        logArea.setForeground(LanMonitorTheme.LOG_TEXT);
        logArea.setFont(LanMonitorTheme.FONT_MONO);
        logArea.setCaretColor(LanMonitorTheme.LOG_TEXT);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LanMonitorTheme.DARK_BORDER));
        scrollPane.getViewport().setBackground(LanMonitorTheme.LOG_BG);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Appends a log line to the console. Safe to call from any thread.
     *
     * @param line the log line to append
     */
    public void appendLine(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(line + "\n");
            lineCount++;
            if (lineCount > MAX_LOG_LINES) {
                trimLog();
            }
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Clears all log content.
     */
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
            lineCount = 0;
        });
    }

    private void trimLog() {
        String text = logArea.getText();
        int cutoff = 0;
        int linesToRemove = lineCount - MAX_LOG_LINES;
        for (int i = 0; i < linesToRemove && cutoff < text.length(); i++) {
            int idx = text.indexOf('\n', cutoff);
            if (idx < 0) break;
            cutoff = idx + 1;
        }
        logArea.setText(text.substring(cutoff));
        lineCount = MAX_LOG_LINES;
    }
}

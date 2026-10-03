package com.vku.lanmonitor.launcher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LauncherFrame extends JFrame {
    private static final Color BG = new Color(0x1e1e2e);
    private static final Color PANEL = new Color(0x2d2d44);
    private static final Color CARD = new Color(0x3d3d5c);
    private static final Color SUCCESS = new Color(0x22c55e);
    private static final Color DANGER = new Color(0xef4444);
    private static final Color INFO = new Color(0x3b82f6);
    private static final Color WARNING = new Color(0xf59e0b);
    private static final Color TEXT_PRIMARY = new Color(0xe4e4e7);
    private static final Color TEXT_SECONDARY = new Color(0xa1a1aa);
    private static final Color MUTED = new Color(0x71717a);

    private final ProcessManager processManager = new ProcessManager();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private JTextArea logArea;
    private JButton btnStartServer;
    private JButton btnStopServer;
    private JLabel lblServerStatus;
    private Timer statusTimer;

    public LauncherFrame() {
        super("LAN Monitor - Launcher");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        initUI();
        startStatusTimer();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
    }

    private void initUI() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));

        JPanel serverPanel = createServerPanel();
        JPanel appsPanel = createAppsPanel();
        JPanel top = new JPanel(new BorderLayout(14, 0));
        top.setOpaque(false);
        top.add(serverPanel, BorderLayout.WEST);
        top.add(appsPanel, BorderLayout.CENTER);

        JPanel logPanel = createLogPanel();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, logPanel);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(8);
        split.setResizeWeight(0.57);
        split.setDividerLocation(350);
        content.add(split, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        log("[LAUNCHER] Sẵn sàng. Nhấn 'Khởi động Server' để bắt đầu.");
        log("[LAUNCHER] Thư mục gốc: " + ProcessManager.getBaseDir());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x4c1d95), getWidth(), 0,
                        new Color(0x6d28d9)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 78));
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("LAN MONITOR");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Control Center v1.0");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(0xd8b4fe));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(1));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        lblServerStatus = new JLabel("O OFFLINE", SwingConstants.CENTER);
        lblServerStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblServerStatus.setForeground(new Color(0xfca5a5));
        lblServerStatus.setOpaque(true);
        lblServerStatus.setBackground(new Color(0x7f1d1d));
        lblServerStatus.setBorder(BorderFactory.createEmptyBorder(7, 15, 7, 15));
        header.add(lblServerStatus, BorderLayout.EAST);
        return header;
    }

    private JPanel createServerPanel() {
        JPanel panel = createRoundPanel(PANEL, 12);
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3d3d5c)),
                BorderFactory.createEmptyBorder(18, 20, 20, 20)));
        panel.setLayout(new BorderLayout(0, 14));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = createLabel(">> SERVER", 14, TEXT_SECONDARY, true);
        JLabel port = createLabel("port 9999", 11, MUTED, false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(port);
        panel.add(titlePanel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 10));
        buttons.setOpaque(false);
        btnStartServer = createStyledButton(">  Khởi động Server", SUCCESS, Color.WHITE);
        btnStartServer.addActionListener(e -> startServer());
        btnStopServer = createStyledButton("[] Dừng Server", DANGER, Color.WHITE);
        btnStopServer.setEnabled(false);
        btnStopServer.setForeground(new Color(255, 255, 255, 180));
        btnStopServer.addActionListener(e -> stopServer());
        JButton clear = createStyledButton("X Xóa log", CARD, Color.WHITE);
        clear.setEnabled(true);
        clear.addActionListener(e -> logArea.setText(""));
        buttons.add(btnStartServer);
        buttons.add(btnStopServer);
        buttons.add(clear);
        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAppsPanel() {
        JPanel panel = createRoundPanel(PANEL, 12);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3d3d5c)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        panel.setLayout(new BorderLayout(0, 12));

        JLabel title = createLabel(">> ỨNG DỤNG", 14, TEXT_SECONDARY, true);
        panel.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(3, 1, 0, 12));
        cards.setOpaque(false);
        cards.add(createAppCard("ADM", "Admin Dashboard",
                "Quản lý đề thi, máy trạm và livestream", INFO,
                "lan-admin", "lan-admin-1.0.0-jar-with-dependencies.jar"));
        cards.add(createAppCard("STU", "Student App",
                "Sinh viên làm bài thi trắc nghiệm", SUCCESS,
                "lan-student", "lan-student-1.0.0-jar-with-dependencies.jar"));
        cards.add(createAppCard("SEC", "Anti-cheat Client",
                "Giám sát máy, chụp màn hình, phát hiện gian lận", WARNING,
                "lan-client", "lan-client-1.0.0-jar-with-dependencies.jar"));
        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAppCard(String emoji, String titleText, String description,
                                 Color accentColor, String module, String jar) {
        JPanel card = createRoundPanel(CARD, 10);
        card.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
        card.setLayout(new BorderLayout(14, 0));
        card.setPreferredSize(new Dimension(0, 90));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(accentColor);
        icon.setPreferredSize(new Dimension(55, 40));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
        icon.setBorder(BorderFactory.createLineBorder(accentColor, 2));
        card.add(icon, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel appTitle = createLabel(titleText, 14, TEXT_PRIMARY, true);
        JLabel desc = new JLabel("<html><div style='width:230px'>" + description + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(TEXT_SECONDARY);
        center.add(appTitle);
        center.add(Box.createVerticalStrut(4));
        center.add(desc);
        card.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        right.setOpaque(false);
        JLabel status = createLabel("O Chưa mở", 11, MUTED, false);
        JButton open = createStyledButton("> Mở", accentColor, Color.WHITE);
        open.setPreferredSize(new Dimension(80, 36));
        open.addActionListener(e -> openApplication(module, jar, status));
        right.add(status);
        right.add(open);
        card.add(right, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x4d4d6c)),
                        BorderFactory.createEmptyBorder(13, 16, 13, 16)));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(13, 16, 13, 16));
                card.repaint();
            }
        });
        return card;
    }

    private JPanel createLogPanel() {
        JPanel panel = createRoundPanel(new Color(0x18181b), 12);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x27272a)),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)));
        panel.setLayout(new BorderLayout(0, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = createLabel(">> SERVER LOG", 12, TEXT_SECONDARY, true);
        JButton clear = createStyledButton("Xóa", CARD, TEXT_PRIMARY);
        clear.setPreferredSize(new Dimension(58, 28));
        clear.addActionListener(e -> logArea.setText(""));
        header.add(title, BorderLayout.WEST);
        header.add(clear, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(0x18181b));
        logArea.setForeground(new Color(0xa3e635));
        logArea.setCaretColor(TEXT_PRIMARY);
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x27272a)));
        scroll.getViewport().setBackground(new Color(0x18181b));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PANEL);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel accounts = createLabel("admin/123456  ·  24ITB217/123456", 11, TEXT_SECONDARY, false);
        JLabel directory = createLabel(ProcessManager.getBaseDir(), 11, MUTED, false);
        footer.add(accounts, BorderLayout.WEST);
        footer.add(directory, BorderLayout.EAST);
        return footer;
    }

    private void openApplication(String module, String jar, JLabel statusLabel) {
        if (!processManager.isPortInUse(9999)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Server chưa chạy!\n\nApp này cần kết nối tới Server port 9999.\n"
                            + "Bạn có muốn khởi động Server trước không?",
                    "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            startServer();
            Timer delayTimer = new Timer(5000, event -> {
                ((Timer) event.getSource()).stop();
                launchApplication(module, jar, statusLabel, true);
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
            return;
        }
        launchApplication(module, jar, statusLabel, false);
    }

    private void launchApplication(String module, String jar, JLabel statusLabel, boolean afterServerStart) {
        try {
            processManager.launchApp(module, jar);
            statusLabel.setText("O Đang chạy");
            statusLabel.setForeground(SUCCESS);
            log("[LAUNCHER] Đã mở " + module + (afterServerStart ? " sau khi start Server" : ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi mở " + module + ":\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            log("[ERROR] " + ex.getMessage());
        }
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color buttonBackground = getBackground();
                if (!isEnabled()) {
                    buttonBackground = new Color(buttonBackground.getRed(), buttonBackground.getGreen(),
                            buttonBackground.getBlue(), 100);
                }
                g2.setColor(buttonBackground);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 10, 0, 10));
        Color hover = bg.brighter();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    private JPanel createRoundPanel(Color color, int radius) {
        return new JPanel() {
            {
                setBackground(color);
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    private JLabel createLabel(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private void startServer() {
        try {
            processManager.startServer(this::log);
            btnStartServer.setEnabled(false);
            btnStopServer.setEnabled(true);
            log("[LAUNCHER] Đang khởi động Server...");
            log("[LAUNCHER] Chờ 5 giây cho Server sẵn sàng...");
            Timer readyTimer = new Timer(5000, event -> {
                log("[LAUNCHER] Server đã sẵn sàng. Giờ có thể mở các app.");
                log("[LAUNCHER] Tài khoản: admin/123456 (giám thị) · 24ITB217/123456 (sinh viên)");
                ((Timer) event.getSource()).stop();
            });
            readyTimer.setRepeats(false);
            readyTimer.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khởi động Server:\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            log("[ERROR] " + e.getMessage());
        }
    }

    private void stopServer() {
        processManager.stopServer();
        btnStartServer.setEnabled(true);
        btnStopServer.setEnabled(false);
        log("[LAUNCHER] Đã dừng Server");
    }

    private void startStatusTimer() {
        statusTimer = new Timer(2000, event -> updateStatuses());
        statusTimer.start();
    }

    private void updateStatuses() {
        boolean serverOnline = processManager.isPortInUse(9999);
        if (serverOnline) {
            lblServerStatus.setText("O ONLINE");
            lblServerStatus.setForeground(new Color(0x86efac));
            lblServerStatus.setBackground(new Color(0x14532d));
            btnStartServer.setEnabled(false);
            btnStopServer.setEnabled(true);
        } else {
            lblServerStatus.setText("O OFFLINE");
            lblServerStatus.setForeground(new Color(0xfca5a5));
            lblServerStatus.setBackground(new Color(0x7f1d1d));
            btnStartServer.setEnabled(true);
            btnStopServer.setEnabled(false);
        }
    }

    private void log(String message) {
        if (logArea == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + timeFormat.format(new Date()) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Đóng Launcher và tắt Server?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            processManager.stopAll();
            if (statusTimer != null) {
                statusTimer.stop();
            }
            System.exit(0);
        }
    }
}

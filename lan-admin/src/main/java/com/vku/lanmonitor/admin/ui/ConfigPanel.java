package com.vku.lanmonitor.admin.ui;

import com.vku.lanmonitor.admin.net.AdminMessageDispatcher;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ConfigPanel extends JPanel {
    private final AdminTcpClient tcpClient;
    private final JTextArea txtWhitelist;
    private final JLabel lblStatus;

    public ConfigPanel(AdminTcpClient tcpClient, AdminMessageDispatcher dispatcher) {
        this.tcpClient = tcpClient;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Cấu hình Whitelist - Danh sách app được phép");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitle, BorderLayout.NORTH);

        txtWhitelist = new JTextArea();
        txtWhitelist.setFont(new Font("Arial", Font.PLAIN, 14));
        txtWhitelist.setLineWrap(true);
        txtWhitelist.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtWhitelist);
        scroll.setBorder(BorderFactory.createTitledBorder(
            "Nhập các keyword, cách nhau bởi dấu phẩy (,)"));
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnLoad = new JButton("Tải lại");
        JButton btnSave = new JButton("Lưu & Áp dụng");
        btnSave.setBackground(new Color(50, 150, 50));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(new Color(0, 100, 200));
        lblStatus.setPreferredSize(new Dimension(200, 25));
        south.add(btnLoad);
        south.add(btnSave);
        south.add(lblStatus);
        add(south, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> tcpClient.sendCommand("REQ_WHITELIST"));
        btnSave.addActionListener(e -> saveWhitelist());

        dispatcher.addHandler(this::handleServerMessage);
        tcpClient.sendCommand("REQ_WHITELIST");
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("RES_WHITELIST:")) {
            String csv = message.substring("RES_WHITELIST:".length());
            SwingUtilities.invokeLater(() -> {
                txtWhitelist.setText(csv);
                lblStatus.setText("Đã tải " + csv.split(",").length + " mục");
                lblStatus.setForeground(new Color(0, 100, 200));
            });
        } else if (message.startsWith("UPDATE_WHITELIST_OK")) {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Đã lưu thành công");
                lblStatus.setForeground(new Color(0, 150, 0));
            });
        } else if (message.startsWith("UPDATE_WHITELIST_FAIL")) {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Lỗi: " + message.substring(22));
                lblStatus.setForeground(Color.RED);
            });
        }
    }

    private void saveWhitelist() {
        String text = txtWhitelist.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách không được để trống!");
            return;
        }
        String csv = String.join(",", Arrays.stream(text.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new));
        int c = JOptionPane.showConfirmDialog(this,
                "Lưu và áp dụng whitelist?\nNội dung:\n" + csv,
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            tcpClient.sendCommand("UPDATE_WHITELIST_CONFIG:" + csv);
        }
    }
}

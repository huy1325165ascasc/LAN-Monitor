package com.vku.lanmonitor.admin.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final AdminTcpClient tcpClient;
    private final DefaultListModel<String> clientListModel;
    private final JList<String> clientList;
    private final JLabel lblStatus;
    private final JLabel lblCount;
    private javax.swing.Timer refreshTimer;

    public DashboardPanel(AdminTcpClient tcpClient) {
        this.tcpClient = tcpClient;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblTitle = new JLabel("Danh sách máy trạm đang online");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        JButton btnRefresh = new JButton("Làm mới");
        lblCount = new JLabel("0 máy");
        lblCount.setFont(new Font("Arial", Font.BOLD, 14));
        lblCount.setForeground(new Color(0, 100, 200));
        north.add(lblTitle);
        north.add(btnRefresh);
        north.add(lblCount);
        add(north, BorderLayout.NORTH);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setFont(new Font("Arial", Font.PLAIN, 14));
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(clientList);
        add(scroll, BorderLayout.CENTER);

        lblStatus = new JLabel("Chưa cập nhật");
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(255, 255, 200));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblStatus, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> tcpClient.sendCommand("REQ_CLIENT_LIST"));

        tcpClient.setMessageListener(this::handleServerMessage);

        tcpClient.sendCommand("REQ_CLIENT_LIST");

        refreshTimer = new javax.swing.Timer(3000, e -> tcpClient.sendCommand("REQ_CLIENT_LIST"));
        refreshTimer.start();
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("RES_CLIENT_LIST:")) {
            String json = message.substring("RES_CLIENT_LIST:".length());
            SwingUtilities.invokeLater(() -> updateClientList(json));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateClientList(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> clients = mapper.readValue(json, List.class);
            clientListModel.clear();
            if (clients.isEmpty()) {
                clientListModel.addElement("(Chưa có máy trạm nào kết nối)");
                lblCount.setText("0 máy");
            } else {
                for (Map<String, Object> c : clients) {
                    String id = String.valueOf(c.get("id"));
                    String ip = String.valueOf(c.get("ip"));
                    String pcName = String.valueOf(c.get("pcName"));
                    clientListModel.addElement("PC " + pcName + " - " + ip + " [" + id + "]");
                }
                lblCount.setText(clients.size() + " máy");
            }
            lblStatus.setText("Cập nhật: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi parse: " + e.getMessage());
        }
    }

    @Override
    public void removeNotify() {
        if (refreshTimer != null) refreshTimer.stop();
        super.removeNotify();
    }
}

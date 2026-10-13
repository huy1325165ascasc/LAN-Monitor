package com.vku.lanmonitor.admin.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.admin.net.AdminMessageDispatcher;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import com.vku.lanmonitor.admin.ui.theme.LanMonitorTheme;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Dashboard panel showing connected client workstations.
 * Uses {@link LanMonitorTheme} for consistent styling.
 */
public class DashboardPanel extends JPanel {

    private final AdminTcpClient tcpClient;
    private final AdminMessageDispatcher dispatcher;
    private final DefaultListModel<String> clientListModel;
    private final JList<String> clientList;
    private final JLabel lblStatus;
    private final JLabel lblCount;
    private javax.swing.Timer refreshTimer;
    private List<Map<String, Object>> currentClients;

    public DashboardPanel(AdminTcpClient tcpClient, AdminMessageDispatcher dispatcher) {
        this.tcpClient = tcpClient;
        this.dispatcher = dispatcher;
        setLayout(new BorderLayout(5, 5));
        setBackground(LanMonitorTheme.LIGHT_BG);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        north.setOpaque(false);
        JLabel lblTitle = new JLabel("Danh sach may tram dang online");
        lblTitle.setFont(LanMonitorTheme.FONT_TITLE);
        JButton btnRefresh = LanMonitorTheme.createSecondaryButton("Lam moi");
        JButton btnLive = LanMonitorTheme.createSecondaryButton("Xem Live");
        JButton btnWebcam = LanMonitorTheme.createPrimaryButton("Xem Webcam", LanMonitorTheme.GREEN_BTN);
        JButton btnSaveFrame = LanMonitorTheme.createSecondaryButton("Chup anh");
        lblCount = new JLabel("0 may");
        lblCount.setFont(LanMonitorTheme.FONT_BOLD);
        lblCount.setForeground(LanMonitorTheme.BLUE_BTN);
        north.add(lblTitle);
        north.add(btnRefresh);
        north.add(lblCount);
        north.add(btnLive);
        north.add(btnWebcam);
        north.add(btnSaveFrame);
        add(north, BorderLayout.NORTH);

        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setFont(LanMonitorTheme.FONT_BASE);
        clientList.setBackground(LanMonitorTheme.WHITE);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(clientList);
        scroll.setBorder(BorderFactory.createLineBorder(LanMonitorTheme.LIGHT_CONTENT));
        add(scroll, BorderLayout.CENTER);

        lblStatus = new JLabel("Chua cap nhat");
        lblStatus.setOpaque(true);
        lblStatus.setBackground(LanMonitorTheme.PURPLE_LIGHT);
        lblStatus.setFont(LanMonitorTheme.FONT_SMALL);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblStatus, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> tcpClient.sendCommand("REQ_CLIENT_LIST"));

        btnLive.addActionListener(e -> {
            int idx = clientList.getSelectedIndex();
            if (idx < 0 || currentClients == null || idx >= currentClients.size()) {
                JOptionPane.showMessageDialog(this, "Chon 1 client!");
                return;
            }
            Map<String, Object> c = currentClients.get(idx);
            String id = String.valueOf(c.get("id"));
            String pcName = String.valueOf(c.get("pcName"));
            new LivestreamDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    tcpClient, dispatcher, id, pcName);
        });

        btnSaveFrame.addActionListener(e -> {
            int idx = clientList.getSelectedIndex();
            if (idx < 0 || currentClients == null || idx >= currentClients.size()) {
                JOptionPane.showMessageDialog(this, "Chon 1 client!");
                return;
            }
            Map<String, Object> c = currentClients.get(idx);
            String id = String.valueOf(c.get("id"));
            tcpClient.sendCommand("SAVE_FRAME:" + id);
            lblStatus.setText("Da gui lenh luu anh cho " + c.get("pcName"));
        });

        btnWebcam.addActionListener(e -> {
            int idx = clientList.getSelectedIndex();
            if (idx < 0 || currentClients == null || idx >= currentClients.size()) {
                JOptionPane.showMessageDialog(this, "Chon 1 client!");
                return;
            }
            Map<String, Object> c = currentClients.get(idx);
            String id = String.valueOf(c.get("id"));
            String pcNameVal = String.valueOf(c.get("pcName"));
            new WebcamDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    tcpClient, dispatcher, id, pcNameVal);
        });

        dispatcher.addHandler(this::handleServerMessage);

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
            currentClients = clients;
            clientListModel.clear();
            if (clients.isEmpty()) {
                clientListModel.addElement("(Chua co may tram nao ket noi)");
                lblCount.setText("0 may");
            } else {
                for (Map<String, Object> c : clients) {
                    String id = String.valueOf(c.get("id"));
                    String ip = String.valueOf(c.get("ip"));
                    String pcName = String.valueOf(c.get("pcName"));
                    clientListModel.addElement("PC " + pcName + " - " + ip + " [" + id + "]");
                }
                lblCount.setText(clients.size() + " may");
            }
            lblStatus.setText("Cap nhat: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        } catch (Exception e) {
            lblStatus.setText("Loi parse: " + e.getMessage());
        }
    }

    @Override
    public void removeNotify() {
        if (refreshTimer != null) refreshTimer.stop();
        super.removeNotify();
    }
}

package com.vku.lanmonitor.admin.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vku.lanmonitor.admin.net.AdminTcpClient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class QuizEditorPanel extends JPanel {
    private final AdminTcpClient tcpClient;
    private final JTable quizTable;
    private final DefaultTableModel tableModel;
    private final JLabel lblStatus;
    private List<Map<String, Object>> quizzesData;

    public QuizEditorPanel(AdminTcpClient tcpClient) {
        this.tcpClient = tcpClient;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel lblTitle = new JLabel("Quản lý đề thi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));

        JButton btnCreate = new JButton("Tạo đề mới");
        btnCreate.setBackground(new Color(50, 150, 50));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setOpaque(true);
        btnCreate.setBorderPainted(false);

        JButton btnEdit = new JButton("Sửa");
        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(200, 50, 50));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setOpaque(true);
        btnDelete.setBorderPainted(false);

        JButton btnRefresh = new JButton("Làm mới");

        north.add(lblTitle);
        north.add(Box.createHorizontalStrut(20));
        north.add(btnCreate);
        north.add(btnEdit);
        north.add(btnDelete);
        north.add(btnRefresh);
        add(north, BorderLayout.NORTH);

        String[] columns = {"ID", "Tên đề thi", "Thời gian (phút)", "Số câu", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        quizTable = new JTable(tableModel);
        quizTable.setFont(new Font("Arial", Font.PLAIN, 13));
        quizTable.setRowHeight(25);
        quizTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        quizTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        JScrollPane scroll = new JScrollPane(quizTable);
        add(scroll, BorderLayout.CENTER);

        lblStatus = new JLabel("Chưa tải danh sách");
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(255, 255, 200));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblStatus, BorderLayout.SOUTH);

        btnCreate.addActionListener(e -> openQuizDialog(null));
        btnEdit.addActionListener(e -> {
            int row = quizTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn 1 đề thi để sửa!");
                return;
            }
            if (quizzesData == null || row >= quizzesData.size()) return;
            openQuizDialog(quizzesData.get(row));
        });
        btnDelete.addActionListener(e -> deleteSelectedQuiz());
        btnRefresh.addActionListener(e -> tcpClient.sendCommand("REQ_QUIZ_ALL"));

        tcpClient.setMessageListener(this::handleServerMessage);

        tcpClient.sendCommand("REQ_QUIZ_ALL");
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("RES_QUIZ_ALL:")) {
            String json = message.substring("RES_QUIZ_ALL:".length());
            SwingUtilities.invokeLater(() -> updateQuizTable(json));
        } else if (message.startsWith("SAVE_QUIZ_OK")) {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Đã lưu đề thi thành công");
                tcpClient.sendCommand("REQ_QUIZ_ALL");
            });
        } else if (message.startsWith("SAVE_QUIZ_FAIL")) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, "Lỗi lưu: " + message.substring(14)));
        } else if (message.startsWith("DELETE_QUIZ_OK")) {
            SwingUtilities.invokeLater(() -> {
                lblStatus.setText("Đã xóa đề thi");
                tcpClient.sendCommand("REQ_QUIZ_ALL");
            });
        } else if (message.startsWith("DELETE_QUIZ_FAIL")) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + message.substring(16)));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateQuizTable(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            quizzesData = mapper.readValue(json, List.class);
            tableModel.setRowCount(0);
            for (Map<String, Object> quiz : quizzesData) {
                String id = String.valueOf(quiz.get("id"));
                String name = String.valueOf(quiz.get("name"));
                Object tlObj = quiz.get("timeLimit");
                int timeLimit = tlObj instanceof Number ? ((Number) tlObj).intValue() : 0;
                List<?> data = (List<?>) quiz.get("data");
                int soCau = data != null ? data.size() : 0;
                Boolean valid = (Boolean) quiz.get("isValid");
                String status = Boolean.TRUE.equals(valid) ? "Hợp lệ" : "Chưa có câu hỏi";
                tableModel.addRow(new Object[]{id, name, timeLimit, soCau, status});
            }
            lblStatus.setText("Đã tải " + quizzesData.size() + " đề thi");
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Lỗi parse: " + e.getMessage());
        }
    }

    private void deleteSelectedQuiz() {
        int row = quizTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 đề thi để xóa!");
            return;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int c = JOptionPane.showConfirmDialog(this,
                "Xóa đề thi \"" + name + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            tcpClient.sendCommand("DELETE_QUIZ:" + id);
        }
    }

    private void openQuizDialog(Map<String, Object> existingQuiz) {
        QuizEditDialog dialog = new QuizEditDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                tcpClient, existingQuiz);
        dialog.setVisible(true);
    }
}

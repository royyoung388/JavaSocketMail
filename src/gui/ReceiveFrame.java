package gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ReceiveFrame {
    private JFrame frame;
    private JPanel panelReceive;
    private JTable tableMail;
    private JScrollPane panelScroll;

    public ReceiveFrame() {
        tableMail.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // 双击事件
                if (e.getClickCount() == 2) {
                    int row = tableMail.getSelectedRow();
                    jumpToMail(row);
                }
            }
        });
    }

    private void jumpToMail(int row) {
        System.out.println("JUMP TO:" + row);
        // TODO : 加载第row行的邮件信息，并进行跳转

        frame.setVisible(false);
        MailFrame mailFrame = new MailFrame();
        mailFrame.show(frame);
    }

    public void show() {
        frame = new JFrame("接收");
        frame.setContentPane(panelReceive);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        initTable();
    }

    private Object[][] getMailInfo() {
        // TODO : 在此获取所有邮件信息
        Object[][] data = {
                {"2019-5-21", "453814685@qq.com", "你好1..."},
                {"2019-5-22", "453814685@qq.com", "你好2..."},
                {"2019-5-23", "453814685@qq.com", "你好3..."},
        };
        return data;
    }

    private void initTable() {
        // 设置表格
        Object[] column = {"时间", "来自", "内容"};
        Object[][] data = getMailInfo();
        TableModel dataModel = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMail.setModel(dataModel);
    }
}

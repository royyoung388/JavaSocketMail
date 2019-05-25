package gui;

import pop3.Mail;
import pop3.MyPOP3;
import pop3.POP3Exception;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ReceiveFrame {
    private JFrame frame;
    private JPanel panelReceive;
    private JTable tableMail;
    private JScrollPane panelScroll;

    private MyPOP3 pop3;
    private Mail[] currentPageMails;

    ReceiveFrame(MyPOP3 p) {
        pop3 = p;
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
        if(currentPageMails ==null){
            return;
        }
        Mail selectedMail  = currentPageMails[row];

        frame.setVisible(false);
        MailFrame mailFrame = new MailFrame(selectedMail);
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

    private Object[][] getMailInfo(int start, int end) {
        // TODO : 在此获取所有邮件信息
        Object[][] result = new Object[end - start+1][4];
        try {
            Mail[] mailInfos = pop3.getMails(start, end);
            for(int i =0; i<= end -start; i++){
                result[i][0] = mailInfos[i].getDate();
                result[i][1] = mailInfos[i].getFrom();
                result[i][2] = mailInfos[i].getContent();
            }

        } catch (POP3Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void initTable() {
        // 设置表格
        Object[] column = {"时间", "来自", "内容"};
        Object[][] data = getMailInfo(16, 16);
        try {
            currentPageMails = pop3.getMails(16,16);
        } catch (POP3Exception e) {
            e.printStackTrace();
        }
        TableModel dataModel = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMail.setModel(dataModel);
    }

}

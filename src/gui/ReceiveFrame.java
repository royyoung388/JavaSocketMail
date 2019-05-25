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
    private JButton button_lastPage;
    private JButton button_nextPage;
    private JTextField text_page;

    private MyPOP3 pop3;
    private Mail[] currentPageMails;
    private int mailCount;
    private int currentPage;
    private int pageLastMail;
    private int pageFirstMail;

    ReceiveFrame(MyPOP3 p) {
        pop3 = p;
        currentPage = 0;
        try {
            mailCount = pop3.mailCount();
        } catch (POP3Exception e) {
            String message = e.getMessage();
            JOptionPane.showMessageDialog(
                    frame,
                    message,
                    "获取邮件失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        button_nextPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(pageLastMail == mailCount){
                    return;
                }
                pageFirstMail = pageLastMail+1;
                if(pageLastMail+10>=mailCount){
                    pageLastMail = mailCount;
                }else {
                    pageLastMail += 10;
                }
                initTable(pageFirstMail,pageLastMail);
            }
        });
        button_lastPage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(currentPage ==0){
                    return;
                }
                pageLastMail = pageFirstMail-1;
                pageFirstMail = pageLastMail-10;
            }
        });

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
        if(1<= mailCount&& mailCount <10){
            initTable(1,mailCount);
            pageLastMail = mailCount;
        } else if (mailCount >= 10) {
            initTable(1,10);
            pageLastMail = 10;
        }
        pageFirstMail =1;
    }

    private Object[][] getMailInfo(int start, int end) {
        // TODO : 在此获取所有邮件信息
        Object[][] result = new Object[end - start+1][4];
        try {
            currentPageMails = pop3.getMails(start, end);
            for(int i =0; i<= end -start; i++){
                result[i][0] = currentPageMails[i].getDate();
                result[i][1] = currentPageMails[i].getFromEmail();
                result[i][2] = currentPageMails[i].getContent();
            }

        } catch (POP3Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void initTable(int s, int e) {
        // 设置表格
        Object[] column = {"时间", "来自", "内容"};
        Object[][] data = getMailInfo(s, e);
        TableModel dataModel = new DefaultTableModel(data, column) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMail.setModel(dataModel);
    }

}

package gui;

import pop3.Mail;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MailFrame {
    private JFrame frame;
    private JLabel fromLabel;
    private JLabel subjectLabel;
    private JTextArea textAreaMain;
    private JButton buttonBack;
    private JPanel panelMail;
    private JFrame receiveFrame;

    private Mail mail;

    MailFrame(Mail mail) {
        this.mail = mail;
        buttonBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (receiveFrame != null) {
                    frame.dispose();
                    receiveFrame.setVisible(true);
                }
            }
        });
    }

    void show(JFrame receiveFrame) {
        frame = new JFrame("邮件");
        frame.setContentPane(panelMail);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        textAreaMain.setEditable(false);
        this.receiveFrame = receiveFrame;

        // TODO : 测试代码，信息需要在上一个界面获取并传到这个界面
        setInfo();
    }

    void setInfo(){
        String from = mail.getFrom();
        String subject = mail.getSubject();
        String content = mail.getContent();

        fromLabel.setText(from);
        subjectLabel.setText(subject);
        textAreaMain.setText(content);
    }


}

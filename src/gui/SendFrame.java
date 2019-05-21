package gui;

import smtp.MySMTP;
import smtp.SMTPException;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendFrame {
    private JFrame frame;
    private String email;
    private String password;

    private JTextField textFieldTo;
    private JTextField textFieldSubject;
    private JTextArea textAreaMain;
    private JButton buttonSend;
    private JPanel panelSend;

    public SendFrame() {
        buttonSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                String toEmail = textFieldTo.getText();
                String subject = textFieldSubject.getText();
                String content = textAreaMain.getText();

                if (checkEnter(toEmail, subject, content)) {
                    send(toEmail, subject, content);
                }
            }
        });
    }

    // 检查输入
    private boolean checkEnter(String email, String subject, String content) {
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p = Pattern.compile(regEx1);
        Matcher m = p.matcher(email);
        String error;
        if (email.equals("") || !m.matches()) {
            error = "请输入正确的邮箱";
        } else if (subject.equals("")) {
            error = "请输入邮件主题";
        } else if (content.equals("")) {
            error = "请输入邮件内容";
        } else {
            return true;
        }

        JOptionPane.showMessageDialog(
                frame,
                error,
                "发送失败",
                JOptionPane.WARNING_MESSAGE
        );
        return false;
    }

    // 发送
    public void send(String toEmail, String subject, String content) {
        MySMTP smtp = new MySMTP();
        try {
            smtp.send(email, password, toEmail, subject, content);
            // 发送成功
            JOptionPane.showMessageDialog(
                    frame,
                    "发送成功！",
                    "发送成功",
                    JOptionPane.PLAIN_MESSAGE
            );
            // 重置页面内容
            textFieldTo.setText("");
            textFieldSubject.setText("");
            textAreaMain.setText("");
        } catch (SMTPException e) {
            // 发送失败
            JOptionPane.showMessageDialog(
                    frame,
                    "错误码: " + e.getStatus() + "\n错误信息: " + e.getMessage(),
                    "发送失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
    }

    public void show(String email, String password) {
        this.email = email;
        this.password = password;

        frame = new JFrame("发送");
        frame.setContentPane(panelSend);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
    }
}

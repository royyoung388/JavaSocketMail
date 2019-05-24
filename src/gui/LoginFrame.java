package gui;

import smtp.MySMTP;
import smtp.SMTPException;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFrame {
    private JFrame frame;
    private JPanel panelLogin;
    private JTextField textFieldEmail;
    private JButton buttonReceive;
    private JPasswordField passwordFieldPasswd;
    private JButton buttonSend;

    public LoginFrame() {
        buttonSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //lLlL
                String email = textFieldEmail.getText();
                String password = new String(passwordFieldPasswd.getPassword());

                if (checkEnter(email, password))
                    loginSend(email, password);
            }
        });

        buttonReceive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String email = textFieldEmail.getText();
                String password = new String(passwordFieldPasswd.getPassword());

                if (checkEnter(email, password))
                    loginReceive();
            }
        });
    }

    // 检查输入
    private boolean checkEnter(String email, String password) {
        String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p = Pattern.compile(regEx1);
        Matcher m = p.matcher(email);
        String error;
        if (email.equals("") || !m.matches()) {
            error = "请输入正确的邮箱";
        } else if (password.equals("")) {
            error = "请输入密码";
        } else {
            return true;
        }

        JOptionPane.showMessageDialog(
                frame,
                error,
                "登陆失败",
                JOptionPane.WARNING_MESSAGE
        );
        return false;
    }

    // SMTP登陆
    private void loginSend(String email, String password) {
        MySMTP smtp = new MySMTP();
        try {
            smtp.login(email, password);
        } catch (SMTPException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "错误码: " + e.getStatus() + "\n错误信息: " + e.getMessage(),
                    "登陆失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 跳转
        SendFrame sendFrame = new SendFrame();
        sendFrame.show(email, password);
        frame.dispose();
    }

    // TODO : POP3登陆
    private void loginReceive() {
        // 跳转
        ReceiveFrame receiveFrame = new ReceiveFrame();
        receiveFrame.show();
        frame.dispose();
    }

    public void show() {
        frame = new JFrame("登陆");
        frame.setContentPane(panelLogin);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.show();
    }
}

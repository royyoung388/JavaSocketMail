package gui;

import pop3.MyPOP3;
import pop3.POP3Exception;
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
    private JPasswordField passwordFieldPasswd;
    private JButton button_login;

    LoginFrame() {
        textFieldEmail.setText("17316600635@163.com");
        passwordFieldPasswd.setText("victorinox");
        button_login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                String email = textFieldEmail.getText();
                String password = new String(passwordFieldPasswd.getPassword());

                if (checkEnter(email, password))
                    if(loginReceive(email,password)){
                        HomePage homePage = new HomePage(email, password);
                    };
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

    // TODO : POP3登陆
    private boolean loginReceive(String user, String pw) {
        // 跳转
        MyPOP3 pop3 = new MyPOP3(user, pw);
        boolean result = pop3.login();
        if(result){
            return true;
        }
        else {
            JOptionPane.showMessageDialog(
                    frame,
                    "pop3登录出错",
                    "登陆失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return true;
        }
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

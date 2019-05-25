package gui;

import pop3.MyPOP3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFrame extends JFrame{
    private JFrame frame;
    private JPanel panelLogin;

    private JPanel bg= new JPanel();
    private JPanel panel = new JPanel();
    private JButton button_login = new JButton();

    private JTextField textFieldEmail = new JTextField(10);
    private JPasswordField passwordFieldPasswd = new JPasswordField(10);

    LoginFrame() {
        textFieldEmail.setText("17316600635@163.com");
        passwordFieldPasswd.setText("victorinox");

        ImageIcon img = new ImageIcon("resource/loginPage.png");
        JLabel imgLabel = new JLabel(img);
        this.getLayeredPane().add(panel, Integer.MIN_VALUE);
        imgLabel.setBounds(0,0,img.getIconWidth(), img.getIconHeight());
        bg.setBounds(0, 0, 950, 490);
        bg.setBackground(Color.white);

        panel.setBounds(0, 0, 950, 490);
        panel.setOpaque(false);
        panel.add(imgLabel);

        button_login.setBounds(627,275, 285, 40);
        button_login.setBorderPainted(false);
        button_login.setBackground(Color.white);
        button_login.setOpaque(false);

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

        textFieldEmail.setBounds(630,125,285,45);
        passwordFieldPasswd.setBounds(630, 175, 285, 45);

        this.add(button_login);
        this.add(textFieldEmail);
        this.add(passwordFieldPasswd);
        this.add(panel);
        this.add(bg);
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

    public void showFrame() {
        this.setLayout(null);
        this.setSize(950, 500);
        this.setLocation(400, 300);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.showFrame();
    }
}

package gui;

import pop3.MyPOP3;
import pop3.POP3Exception;
import smtp.MySMTP;
import smtp.SMTPException;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class HomePage {
    private JFrame frame;
    private JTextField text_userEmail;
    private JButton button_receive;
    private JButton button_send;
    private JLabel label_user;
    private JTextField text_memo;
    private JPanel mainPanel;
    private JLabel label_bg;

    private String user;
    private String password;

    HomePage(String user, String password){
        this.user =user;
        this.password =password;
        text_userEmail.setText(user);
        int num = (int)(Math.random()*10);
        String path = "resource/memo.txt";
        String memo = "";
        try{
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            int i = 0;
            while((memo = br.readLine())!=null){//使用readLine方法，一次读一行
                if(i == num) {
                    break;
                }
                i++;
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        text_memo.setText(memo);

        button_send.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                loginSend(user, password);
            }
        });

        button_receive.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                loginReceive(user, password);
            }
        });
        ImageIcon img = new ImageIcon("resource/write.jpg");
        label_bg.setIcon(img);
        label_bg.setBounds(0,0,img.getIconWidth(), img.getIconHeight());

        show();
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
    private void loginReceive(String user, String pw) {
        // 跳转
        MyPOP3 pop3 = new MyPOP3(user, pw);
        if(pop3.login()){
            ReceiveFrame receiveFrame = new ReceiveFrame(pop3);
            receiveFrame.show();
            frame.dispose();
        } else{
            JOptionPane.showMessageDialog(
                    frame,
                    "pop3登录出错",
                    "登陆失败",
                    JOptionPane.WARNING_MESSAGE
            );
        }


    }

    void show() {
        frame = new JFrame("主页");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(1005, 730);
        frame.setLocationRelativeTo(null);
    }
}

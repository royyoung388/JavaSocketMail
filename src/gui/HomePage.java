package gui;

import pop3.MyPOP3;
import smtp.MySMTP;
import smtp.SMTPException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class HomePage extends JFrame{
    private JFrame frame;
    private JTextField text_userEmail;
    private JLabel label_user;
    private JTextArea text_memo;
    private JPanel mainPanel;
    private JLabel label_bg;

    private String user;
    private String password;

    private JButton button_receive = new JButton();
    private JButton button_send = new JButton();
    private JLabel title = new JLabel();
    private JPanel bg= new JPanel();
    private JPanel panel = new JPanel();

    HomePage(String user, String password){
        ImageIcon img = new ImageIcon("resource/homepage.png");
        JLabel imgLabel = new JLabel(img);
        this.getLayeredPane().add(panel, new Integer(Integer.MIN_VALUE));
        imgLabel.setBounds(0,0,img.getIconWidth(), img.getIconHeight());

        bg.setBounds(0, 0, 1000, 700);
        bg.setBackground(Color.white);

        panel.setBounds(0, 0, 1000, 700);
        panel.add(imgLabel);
        panel.setOpaque(false);

        title.setText("hi! "+ user);
        title.setFont(new java.awt.Font("Dialog",   3,   35));
        title.setForeground(Color.white);
        title.setBounds(350, 55, 500, 50);

        text_memo.setFont(new java.awt.Font("Dialog",   3,   30));
        text_memo.setForeground(Color.white);
        text_memo.setBounds(350, 150, 500, 400);
        text_memo.setLineWrap(true);
        text_memo.setOpaque(false);

        button_receive.setBounds(72,300, 123, 50);
        button_receive.setBorderPainted(false);
        button_receive.setBackground(Color.white);
        button_receive.setOpaque(false);
        //button_receive.setText("收邮件");


        button_send.setBounds(72,134, 123, 50);
        button_send.setBorderPainted(false);
        button_send.setBackground(Color.white);
        button_send.setOpaque(false);
        //button_send.setText("发邮件");


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
        text_memo.setText("Today's motto:"+"\r\n"+memo);

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

        this.add(title);
        this.add(text_memo);
        this.add(button_receive);
        this.add(button_send);
        this.add(panel);
        this.add(bg);
    }

    // SMTP登陆
    private void loginSend(String email, String password) {
        MySMTP smtp = new MySMTP();
        try {
            smtp.login(email, password);
        } catch (SMTPException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "错误码: " + e.getStatus() + "\n错误信息: " + e.getMessage(),
                    "登陆失败",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 跳转
        SendFrame sendFrame = new SendFrame();
        sendFrame.show(email, password);
        //this.dispose();
    }

    // TODO : POP3登陆
    private void loginReceive(String user, String pw) {
        // 跳转
        MyPOP3 pop3 = new MyPOP3(user, pw);
        if(pop3.login()){
            ReceiveFrame receiveFrame = new ReceiveFrame(pop3);
            receiveFrame.show();
            //this.dispose();
        } else{
            JOptionPane.showMessageDialog(
                    this,
                    "pop3登录出错",
                    "登陆失败",
                    JOptionPane.WARNING_MESSAGE
            );
        }


    }

    void showPage() {
        this.setLayout(null);
        this.setSize(1000, 700);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }
    public static void main(String[] args) {
        HomePage homePage= new HomePage("","");
        homePage.showPage();
    }
}

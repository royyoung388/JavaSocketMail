package gui;

import smtp.MySMTP;
import smtp.SMTPException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendFrame extends JFrame{
    private JFrame frame;
    private String email;
    private String password;

    private JTextArea textAreaMain;
    private JButton sendButton = new JButton("发送");
    private JPanel panelSend;

    private JButton timeSend = new JButton("定时发送");
    private JButton draftbtn1 = new JButton("存草稿");
    private JButton closeButton = new JButton("关闭");
    private JLabel enclosure = new JLabel();
    private JButton addenclosure = new JButton("添加附件");
    private JLabel receiver = new JLabel("收件人");
    private JLabel theme = new JLabel("主题");
    private JLabel mainbody = new JLabel("正文");
    private JLabel addresser = new JLabel("发件人:");
    private JTextField textFieldTo = new JTextField(20);
    private JTextField textFieldSubject = new JTextField(20);
    private JTextField addressertxt = new JTextField(20);
    private JPanel bg= new JPanel();
    private JPanel panel = new JPanel();
    private List<String> strings = new ArrayList<>();


    public SendFrame() {
        ImageIcon img = new ImageIcon("resource/writeMail.png");
        JLabel imgLabel = new JLabel(img);
        this.getLayeredPane().add(panel, new Integer(Integer.MIN_VALUE));
        imgLabel.setBounds(0,0,img.getIconWidth(), img.getIconHeight());
        bg.setBounds(0, 0, 1000, 700);
        bg.setBackground(Color.white);
        //
        panel.setBounds(0, 0, 1000, 700);
        panel.setOpaque(false);
        panel.add(imgLabel);
        //
        sendButton.setBounds(20, 55, 70, 30);
        sendButton.setFont(new Font("微软雅黑",1,16));
        timeSend.setBounds(100, 55, 100, 30);
        timeSend.setFont(new Font("微软雅黑",1,16));
        draftbtn1.setBounds(210, 55, 90, 30);
        draftbtn1.setFont(new Font("微软雅黑",1,16));
        closeButton.setBounds(310, 55, 70, 30);
        closeButton.setFont(new Font("微软雅黑",1,16));

        receiver.setBounds(50, 105, 50, 20);
        receiver.setForeground(Color.BLUE);
        textFieldTo.setBounds(100, 100, 700, 30);
        theme.setBounds(60, 150, 50, 20);
        textFieldSubject.setBounds(100, 145, 700, 30);
        enclosure.setBounds(190, 190,1000,30);
        addenclosure.setBounds(100, 190, 90, 30);
        mainbody.setBounds(60, 240, 50, 20);
        textAreaMain.setBounds(100, 235, 700, 340);
        textAreaMain.setLineWrap(true);
        textAreaMain.setBorder(new LineBorder(new java.awt.Color(85,86,86), 1, false));
        addresser.setBounds(100, 590, 50, 20);
        addressertxt.setBounds(150, 585, 300, 30);
        addressertxt.setEditable(false);

        this.add(sendButton);
        this.add(timeSend);
        this.add(draftbtn1);
        this.add(closeButton);
        this.add(receiver);
        this.add(textFieldTo);
        this.add(theme);
        this.add(textFieldSubject);
        addenclosure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(null);
                File file = fc.getSelectedFile();
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    strings.add(file.getAbsolutePath());
                    enclosure.setText(enclosure.getText()+"  |  "+file.getName());
                }
            }
        });
        this.add(addenclosure);
        this.add(enclosure);
        this.add(mainbody);
        this.add(textAreaMain);
        this.add(addresser);
        this.add(addressertxt);
        this.add(panel);
        this.add(bg);


        sendButton.addMouseListener(new MouseAdapter() {
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

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                setVisible(false);
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

        this.setLayout(null);
        this.setSize(1005, 730);
        this.setLocation(400, 300);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        addressertxt.setText(email);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SendFrame sendFrame = new SendFrame();
        sendFrame.show("","");
    }

}

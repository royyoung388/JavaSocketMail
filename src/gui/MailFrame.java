package gui;

import pop3.Mail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MailFrame extends JFrame{
    private JFrame frame = this;
    private JPanel panelMail;
    private JFrame receiveFrame;
    private Mail mail;
    private JButton buttonBack = new JButton();
    private JButton lookbtn = new JButton();
    private JTextArea textAreaMain = new JTextArea();

    private JPanel bg= new JPanel();
    private JPanel panel = new JPanel();
    private JPanel tablePane = new JPanel();
    private JPanel areaPane = new JPanel();
    private JLabel subjectLabel = new JLabel("//");
    private JLabel fromLabel = new JLabel("//");

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
        ImageIcon img = new ImageIcon("resource/lookmail.png");
        JLabel imgLabel = new JLabel(img);
        this.getLayeredPane().add(panel, new Integer(Integer.MIN_VALUE));
        imgLabel.setBounds(0,0,img.getIconWidth(), img.getIconHeight());
        bg.setBounds(0, 0, 1100, 700);
        bg.setBackground(Color.white);

        panel.setBounds(0, -5, 1100, 700);
        panel.add(imgLabel);
        panel.setOpaque(false);

        buttonBack.setBounds(5, 5, 66, 32);
        buttonBack.setBorderPainted(false);
        buttonBack.setBackground(Color.white);
        buttonBack.setOpaque(false);
        buttonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        subjectLabel.setBounds(120, 50, 300, 30);
        fromLabel.setBounds(120, 88, 300, 30);
        textAreaMain.setBounds(5, 185, 985, 470);
        textAreaMain.setLineWrap(true);
        textAreaMain.setEditable(false);

        areaPane.setLayout(null);
        areaPane.setLayout(null);
        JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setViewportView(textAreaMain);
        scrollPane2.setBounds(0, 0, 985, 470);
        areaPane.add(scrollPane2);
        areaPane.setBounds(5, 185, 985, 470);

        this.add(subjectLabel);
        this.add(fromLabel);
        this.add(buttonBack);
        this.add(lookbtn);
        this.add(areaPane);
        this.add(tablePane);
        this.add(panel);
        this.add(bg);
    }

    void show(JFrame receiveFrame) {
        this.setLayout(null);
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setVisible(true);
        textAreaMain.setEditable(false);
        this.receiveFrame = receiveFrame;

        // TODO : 测试代码，信息需要在上一个界面获取并传到这个界面
        setInfo();
    }

    void setInfo(){
        String from = mail.getFrom()+"<"+mail.getFromEmail()+">";
        String subject = mail.getSubject();
        String content = mail.getContent();

        fromLabel.setText(from);
        subjectLabel.setText(subject);
        textAreaMain.setText(content);
    }

    //测试
    public static void main(String[] args) {
        MailFrame mailFrame= new MailFrame(null) ;
        mailFrame.show(null);
    }
}



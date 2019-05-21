package smtp;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class MySMTP {
    final static String host = "fishingkingczjhost";
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;

    // 初始化socket连接
    private void init(String server) throws IOException, SMTPException {
        System.out.println("INIT CONN");

        // socket 建立连接
        socket = new Socket("smtp." + server, 25);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(outputStream, true);
        String resp = reader.readLine();
        System.out.println("CONN: " + resp);
        if (getStatus(resp) != 220) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }

        // ehlo
        writer.println("ehlo " + host);
        resp = reader.readLine();
        while (resp.contains("-")) {
            resp = reader.readLine();
        }
        System.out.println("EHLO: " + resp);
        if (getStatus(resp) != 250) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }
    }

    // 退出smtp会话
    private void quit() throws IOException, SMTPException {
        System.out.println("QUIT CONN");

        // quit
        writer.println("quit");
        String resp = reader.readLine();
        System.out.println("QUIT: " + resp);
    }

    // 登陆校验，不发送内容
    private void authorize(String email, String password) throws IOException, SMTPException {
        System.out.println("AUTH");

        String encodeUser = Base64.getEncoder().encodeToString(email.substring(0, email.indexOf("@")).getBytes());
        String encodePass = Base64.getEncoder().encodeToString(password.getBytes());
        // auth login
        writer.println("auth login");
        String resp = reader.readLine();
        System.out.println("AUTH: " + resp);
        if (getStatus(resp) != 334) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }

        // login
        writer.println(encodeUser);
        resp = reader.readLine();
        System.out.println("USER: " + resp);
        if (getStatus(resp) != 334) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }
        writer.println(encodePass);
        resp = reader.readLine();
        System.out.println("PASS: " + resp);
        if (getStatus(resp) != 235) {
            if (getStatus(resp) == 535)
                throw new SMTPException(getStatus(resp), "用户名或密码错误");
            else
                throw new SMTPException(getStatus(resp), getReason(resp));
        }
    }

    // 清空之前的stmp对话
    private void rset() throws IOException, SMTPException {
        writer.println("rset");
        String resp = reader.readLine();
        System.out.println("AUTH: " + resp);
        if (getStatus(resp) != 250) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }
    }

    // 发送内容
    private void send(String sendFrom, String sendTo, String subject, String content) throws SMTPException, IOException {
        // send and rcpt
        writer.println("mail from:<" + sendFrom + ">");
        String resp = reader.readLine();
        System.out.println("FROM: " + resp);
        if (getStatus(resp) != 250) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }

        writer.println("rcpt to:<" + sendTo + ">");
        resp = reader.readLine();
        System.out.println("RCPT: " + resp);
        if (getStatus(resp) != 250) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }

        // data
        writer.println("data");
        resp = reader.readLine();
        System.out.println("DATA: " + resp);
        if (getStatus(resp) != 354) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }

        // head
        writer.println("subject:" + subject);
        writer.println("from:" + sendFrom);
        writer.println("to:" + sendTo);
        writer.println("Content-Type: text/plain;charset=\"utf-8\"");
        writer.println();
        // content
        writer.println(content);
        writer.println(".");
        resp = reader.readLine();
        System.out.println("SEND: " + resp);
        if (getStatus(resp) != 250) {
            throw new SMTPException(getStatus(resp), getReason(resp));
        }
    }

    // 登陆
    public void login(String email, String password) throws SMTPException {
        try {
            String server = email.substring(email.indexOf("@") + 1);
            System.out.println(server);
            init(server);
            authorize(email, password);
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 登陆并发送内容
    public void send(String email, String password, String sendTo, String subject, String content) throws SMTPException {
        try {
            if (socket == null || socket.isClosed()) {
                String server = email.substring(email.indexOf("@") + 1);
                init(server);
            } else {
                rset();
            }
            authorize(email, password);
            send(email, sendTo, subject, content);
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从回复字符串获取回复状态
    private int getStatus(String resp) {
        return Integer.parseInt(resp.substring(0, resp.indexOf(" ")));
    }

    // 从回复字符串获取回复信息
    private String getReason(String resp) {
        return resp.substring(resp.indexOf(" "));
    }
}

package pop3;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyPOP3 {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String server, user, pwd;

    public MyPOP3(String user, String pwd) {
        this.server = user.substring(user.indexOf("@") + 1);
        this.user = user;
        this.pwd = pwd;
    }

    //测试是否可以登录
    public boolean login() {
        try {
            init(server);
            login(user, pwd);
            quit();
        } catch (IOException | POP3Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //获取总邮件数
    public int mailCount() throws POP3Exception {
        int count = 0;
        try {
            init(server);
            login(user, pwd);
            count = stat();
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    //获取start到end的邮件
    public Mail[] getMails(int start, int end) throws POP3Exception {
        Mail[] mails = new Mail[end - start + 1];
        try {
            init(server);
            login(user, pwd);
            for (int i = start; i <= end; i++) {
                mails[i - start] = getMail(i);
            }
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mails;
    }

    //删除邮件
    public void deleteMails(int[] indexes) throws POP3Exception {
        try {
            init(server);
            login(user, pwd);
            for (int i : indexes) {
                deleteMail(i);
            }
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init(String server) throws IOException, POP3Exception {
        System.out.println("INIT POP3");

        // socket 建立连接
        socket = new Socket("pop3." + server, 110);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = new PrintWriter(outputStream, true);

        String resp = reader.readLine();
        System.out.println("CONN: " + resp);
        if (!isOK(resp)) {
            throw new POP3Exception(getMessage(resp));
        }
    }

    //登录
    private void login(String user, String pwd) throws IOException, POP3Exception {
        shell("user " + user);
        shell("pass " + pwd);
    }

    //查询统计信息，返回总邮件数
    private int stat() throws IOException, POP3Exception {
        String resp = shell("stat");
        return Integer.parseInt(resp.split(" ")[1]);
    }

    //获取指定序号的邮件。序号从1开始编码。越新的邮件序号越大
    private Mail getMail(int index) throws IOException, POP3Exception {
        String resp = shell("retr " + index);
        int size = Integer.parseInt(getMessage(resp).split(" ")[0]) + 1;

        char[] chars = new char[size];
        int readLen = 0;
        int getLen = 0;
        while (getLen < size) {
            readLen = reader.read(chars, getLen, size - getLen);
            if (readLen == -1) {
                break;
            }
            getLen += readLen;
        }
        System.out.println(chars);

        //确保最后一个字符是 . 表示结束
        while (chars[chars.length - 1] != '.') {
            String a = reader.readLine();
            System.out.println(a);
            if (a.equals("."))
                break;
        }
        return new Mail(new String(chars));
    }

    //删除指定序列的邮件
    private void deleteMail(int index) throws IOException, POP3Exception {
        shell("dele " + index);
    }

    private void quit() throws IOException, POP3Exception {
        shell("quit");
    }

    private boolean isOK(String response) {
        if (response.startsWith("+OK")) {
            return true;
        } else {
            return false;
        }
    }

    private String shell(String shell) throws IOException, POP3Exception {
        writer.println(shell);
        String resp = reader.readLine();
        //如果读出来是空的，则继续读
        while (resp.isEmpty()) {
            resp = reader.readLine();
        }

        System.out.println(shell + " : " + resp);
        if (!isOK(resp)) {
            throw new POP3Exception(getMessage(resp));
        }
        return resp;
    }

    private String getMessage(String response) {
        return response.substring(response.indexOf(" ") + 1);
    }

    private String md5(String pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] secretBytes = MessageDigest.getInstance("md5").digest(pwd.getBytes());
        return new BigInteger(1, secretBytes).toString(16);
    }

    public static void main(String[] args) {
        MyPOP3 pop3 = new MyPOP3("13297990330@163.com", "ypc19980501.");
        try {
            pop3.getMails(1, 10);
        } catch (POP3Exception e) {
            e.printStackTrace();
        }
    }
}

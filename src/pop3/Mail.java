package pop3;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail {
    private String from;
    private String fromEmail;
    private String to;
    private String toEmail;
    private Date date;
    private String subject;
    private String content;
    private String mail;

    public Mail(String mail) {
        this.mail = mail;
        parseFrom();
        parseTo();
        parseDate();
        parseSubject();
        parseContent();
    }

    //解析编码
    private String decode(String s) {
        if (s.startsWith("=?")) {
            try {
                Pattern pattern = Pattern.compile("=\\?(.*)?\\?([B|Q])\\?(.*)\\?=");
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    switch (matcher.group(2)) {
                        case "B":
                            byte[] source = Base64.getDecoder().decode(matcher.group(3));
                            return new String(source, matcher.group(1));
                        case "Q":
                            return QuotedPrintable.decode(matcher.group(3).getBytes(), matcher.group(1));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return s;
    }

    private void parseFrom() {
        Pattern pattern = Pattern.compile("From: (.|\\n)*?\\n(?!\\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, -1);
                fromEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, -1);
                from = decode(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, -1);
                fromEmail = temp[1];
            }
        }
    }

    private void parseTo() {
        Pattern pattern = Pattern.compile("To: (.|\\n)*?\\n(?!\\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, -1);
                toEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, -1);
                to = decode(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, -1);
                toEmail = temp[1];
            }
        }
    }

    private void parseDate() {
        Pattern pattern = Pattern.compile("Date: (.|\\n)*?\\n(?!\\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            date = new Date(matcher.group(1));
        }
    }

    private void parseSubject() {
        Pattern pattern = Pattern.compile("Subject: (.|\\n)*?\\n(?!\\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            subject = decode(matcher.group(1));
        }
    }

    private void parseContent() {
        Pattern pattern = Pattern.compile("Content-Type: (.|\\n)*?\\n(?!\\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String type = matcher.group(1);

            if (type.contains("boundary")) {
                String boundary = null;
                Matcher matcher1 = Pattern.compile("boundary=\"(.)*?\";").matcher(matcher.group(1));
                boundary = matcher1.group(1);
                parsePart(boundary);
            } else {
                parsePart(null);
            }
        }
    }

    private void parsePart(String boundary) {
        String body = null;
        Matcher matcher = Pattern.compile("\\n\\n(.|\\n)*").matcher(mail);
        if (matcher.find()) {
            body = matcher.group(1);
        }

        if (boundary == null) {
            content = body;
            return;
        }
    }
}

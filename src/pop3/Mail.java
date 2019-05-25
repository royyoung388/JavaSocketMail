package pop3;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail {
    private String from;
    private String fromEmail;
    private String to;
    private String toEmail;
    private Date date;
    private String subject;
    private String content = "";
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
        Pattern pattern = Pattern.compile("From: ((.|\n|\r)*?)\n(?!\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                fromEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                from = decode(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, temp[1].length() - 1);
                fromEmail = temp[1];
            }
        }
    }

    private void parseTo() {
        Pattern pattern = Pattern.compile("To: ((.|\n|\r)*?)\n(?!\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                toEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                to = decode(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, temp[1].length() - 1);
                toEmail = temp[1];
            }
        }
    }

    private void parseDate() {
        Pattern pattern = Pattern.compile("Date: ((.|\n|\r)*?)\n(?!\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
                Date date = sdf.parse(matcher.group(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseSubject() {
        Pattern pattern = Pattern.compile("Subject: ((.|\n|\r)*?)\n(?!\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            subject = decode(matcher.group(1));
        }
    }

    private void parseContent() {
        Pattern pattern = Pattern.compile("Content-Type: ((.|\n|\r)*?)\n(?!\t)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String type = matcher.group(1);
            String body = null;

            //获取body
            Matcher matcher1 = Pattern.compile("\r\n\r\n(.*)", Pattern.DOTALL).matcher(mail);
            if (matcher1.find()) {
                body = matcher1.group(1);
            }

            //寻找最外层的boundary
            if (type.contains("boundary")) {
                String boundary = null;
                Matcher matcher2 = Pattern.compile("boundary=\"(.*)?\"").matcher(type);
                if (matcher2.find())
                    boundary = matcher2.group(1);
                parsePart(body, boundary);
            } else {
                content = body;
            }
        }
    }

    private void parsePart(String part, String boundary) {
        String re = String.format("(?<=--%s)((.)*?)(?=--%s)", boundary, boundary);
        Matcher matcher = Pattern.compile(re, Pattern.DOTALL).matcher(part);

        while (matcher.find()) {

            Matcher matcherType = Pattern.compile("Content-Type: (.*?)\n(?!\t)", Pattern.DOTALL).matcher(matcher.group(1));
            String type = null;
            if (matcherType.find())
                type = matcherType.group(1);

            if (type.contains("boundary")) {
                String bound = null;
                Matcher matcher1 = Pattern.compile("boundary=\"(.*)?\"").matcher(type);
                if (matcher1.find())
                    boundary = matcher1.group(1);
                parsePart(matcher.group(1), boundary);
            } else if (type.contains("text")) {
                Matcher matcherContent = Pattern.compile("\r\n\r\n(.*)", Pattern.DOTALL).matcher(matcher.group(1));
                if (matcherContent.find())
                    content += matcherContent.group(1);
            }
        }
    }

    public String getFrom() {
        return from;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public String getTo() {
        return to;
    }

    public String getToEmail() {
        return toEmail;
    }

    public Date getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
}

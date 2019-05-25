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

    public Mail() {

    }

    public Mail(String mail) {
        this.mail = mail;
        parseFrom();
        parseTo();
        parseDate();
        parseSubject();
        parseContent();
    }

    //解析编码
    private String decodeMime(String s) {
        if (s.startsWith("=?")) {
            Pattern pattern = Pattern.compile("=\\?(.*)?\\?([B|Q])\\?(.*)\\?=");
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                try {
                    switch (matcher.group(2)) {
                        case "B":
                            return decode(matcher.group(3), "base64", matcher.group(1));
                        case "Q":
                            return decode(matcher.group(3), "quoted-printable", matcher.group(1));
                    }
                } catch (IllegalArgumentException e) {
                    return "";
                }
            }
        }
        return s;
    }

    //解析编码
    private String decode(String content, String encoding, String charset) {
        switch (encoding) {
            case "quoted-printable":
                return QuotedPrintable.decode(content.trim().getBytes(), charset);
            case "base64":
                try {
                    byte[] source = Base64.getDecoder().decode(content.trim());
                    return new String(source, charset);
                } catch (IllegalArgumentException e) {
                    try {
                        byte[] source = Base64.getDecoder().decode(content.trim().replace("\r\n", ""));
                        return new String(source, charset);
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    private void parseFrom() {
        Matcher matcher = Pattern.compile("^(?i)From:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE)
                .matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).trim().split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                fromEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                from = decodeMime(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, temp[1].length() - 1);
                fromEmail = temp[1];
            }
        }
    }

    private void parseTo() {
        Pattern pattern = Pattern.compile("^(?i)To:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String[] temp = matcher.group(1).trim().split(" ");
            if (temp.length == 1) {
                //只有邮件地址
                if (temp[0].startsWith("<"))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                toEmail = temp[0];
            } else {
                //有名称和地址
                if (temp[0].startsWith("\""))
                    temp[0] = temp[0].substring(1, temp[0].length() - 1);
                to = decodeMime(temp[0]);
                if (temp[1].startsWith("<"))
                    temp[1] = temp[1].substring(1, temp[1].length() - 1);
                toEmail = temp[1];
            }
        }
    }

    private void parseDate() {
        Pattern pattern = Pattern.compile("^(?i)Date:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            System.out.println(matcher.group(1).trim());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
                date = sdf.parse(matcher.group(1).trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseSubject() {
        Pattern pattern = Pattern.compile("^(?i)Subject:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            subject = decodeMime(matcher.group(1).trim());
        }
    }

    private void parseContent() {
        Pattern pattern = Pattern.compile("^Content-Type:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String type = matcher.group(1).trim();
            String body = null;

            //获取body
            Matcher matcher1 = Pattern.compile("\r\n\r\n(.*)", Pattern.DOTALL).matcher(mail);
            if (matcher1.find()) {
                body = matcher1.group(1).trim();
                if (body.endsWith(".")) {
                    body = body.substring(0, body.length() - 1);
                }
            }

            //寻找最外层的boundary
            if (type.contains("boundary")) {
                String boundary = null;
                Matcher matcher2 = Pattern.compile("boundary=\"(.*)?\"").matcher(type);
                if (matcher2.find())
                    boundary = matcher2.group(1);
                parsePart(body, boundary);
            } else {
                //没有boundary

                //获取字符编码
                Matcher matcherCharset = Pattern.compile("charset=(.*)?").matcher(type);
                String charset = "utf-8";
                if (matcherCharset.find()) {
                    charset = matcherCharset.group(1);
                    if (charset.startsWith("\""))
                        charset = charset.substring(1, charset.length() - 1);
                }
                System.out.println("charset = " + charset);

                //获取传输编码
                Matcher matcherEncoding = Pattern.compile("Content-Transfer-Encoding: (.*?)\r\n(?!\\s)", Pattern.DOTALL)
                        .matcher(mail);
                if (matcherEncoding.find()) {
                    String encoding = matcherEncoding.group(1);
                    System.out.println("encoding = " + encoding);
                    content += decode(body, encoding, charset);
                } else {
                    content += body;
                }
            }
        }
    }

    private void parsePart(String part, String boundary) {
        String re = String.format("(?<=--%s)((.)*?)(?=--%s)", boundary, boundary);
        Matcher matcherBoundary = Pattern.compile(re, Pattern.DOTALL).matcher(part);

        while (matcherBoundary.find()) {

            Matcher matcherType = Pattern.compile("^Content-Type:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE)
                    .matcher(matcherBoundary.group(1));
            String type = null;
            if (matcherType.find())
                type = matcherType.group(1).trim();

            if (type.contains("boundary")) {
                String bound = null;
                Matcher matcher1 = Pattern.compile("boundary=\"(.*)?\"").matcher(type);
                if (matcher1.find())
                    boundary = matcher1.group(1);
                parsePart(matcherBoundary.group(1), boundary);
            } else if (type.contains("text")) {
                //获取字符编码
                Matcher matcherCharset = Pattern.compile("charset=(.*)?").matcher(type);
                String charset = "utf-8";
                if (matcherCharset.find()) {
                    charset = matcherCharset.group(1);
                    if (charset.startsWith("\""))
                        charset = charset.substring(1, charset.length() - 1);
                }
                System.out.println("charset = " + charset);

                //获取传输编码
                Matcher matcherEncoding = Pattern.compile("^Content-Transfer-Encoding:(.*?)\r\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE)
                        .matcher(matcherBoundary.group(1));
                String encoding = "base64";
                if (matcherEncoding.find()) {
                    encoding = matcherEncoding.group(1).trim();
                }
                System.out.println("encoding = " + encoding);

                //获取content
                Matcher matcherContent = Pattern.compile("\r\n\r\n(.*)", Pattern.DOTALL).matcher(matcherBoundary.group(1));
                if (matcherContent.find()) {
                    System.out.println("matcherContent = " + matcherContent.group(1));
                    content += decode(matcherContent.group(1), encoding, charset);
                }
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

    public static void main(String[] args) {
        String s = "Received: from qq.com (unknown [59.36.132.123])\n" +
                "\tby mx15 (Coremail) with SMTP id QcCowAC3Om0lx+hcu4I9CA--.16703S3;\n" +
                "\tSat, 25 May 2019 12:40:05 +0800 (CST)\n" +
                "DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/relaxed; d=qq.com; s=s201512;\n" +
                "\tt=1558759204; bh=2+uSLjZcajHFNgJbsB+VIwYlqZ+yeaJVfblBSMK++fw=;\n" +
                "\th=subject:from:to:Message-ID;\n" +
                "\tb=kqf4cco2137TJnw6A5mP49QGnsDet/9jIBkhDPeD/DKzf1J2tH0nWh1cLO6aJgAc9\n" +
                "\t XzRZnBEQ4bb53myAf6BNnoEzjpozXFvXpfRr46kMzeEPzOYoLugTeokKU3AipSAHop\n" +
                "\t jyErn8D8UxrH4Hhn7T2/reFidfZW1DyBPg6jmk+A=\n" +
                "X-QQ-mid: esmtp6t1558759204twos5h5wf\n" +
                "Received: from fishingkingczjhost (unknown [58.19.5.163])\n" +
                "\tby esmtp4.qq.com (ESMTP) with SMTP id 0\n" +
                "\tfor <17316600635@163.com>; Sat, 25 May 2019 12:40:03 +0800 (CST)\n" +
                "X-QQ-SSF: 00010000000000F0F5301000000000Y\n" +
                "X-QQ-FEAT: d9mkU8f/UdJD7fRE7QP24DcBoXrNBCYG+KZnPcakY4hmIaHLMZK0JhQZfEPOG\n" +
                "\tlZ0I2bXRehIk6nfn73RpEXhui9pMnpgVLKqHFfjlBo/noK3ioG/ZzwNlEiwKY50SI5Ggczl\n" +
                "\tOZq7bz0w8GUsDZk3RFzS8gh7GMuFzCKA2Xlo1AzBwpXA0kDSAUWe/jVIE0QEg1CjgSM1dbf\n" +
                "\tQuZLLGAel/dqLaD3TYj0cBLZskA+MfMDRlDKnbZv6S5cWUvUuV2UM\n" +
                "X-QQ-GoodBg: 0\r\n" +
                "subject:啦啦啦啦\r\n" +
                "from:1041381617@qq.com\r\n" +
                "to:17316600635@163.com\r\n" +
                "Content-Type: text/plain;charset=\"utf-8\"\n" +
                "X-QQ-SENDSIZE: 520\n" +
                "Feedback-ID: esmtp:qq.com:bgweb:bgweb10\n" +
                "Message-ID: mis_4DE76B4A1EAF2DBE1D742779@qq.com\n" +
                "X-CM-TRANSID:QcCowAC3Om0lx+hcu4I9CA--.16703S3\n" +
                "Authentication-Results: mx15; spf=pass smtp.mail=1041381617@qq.com; dk\n" +
                "\tim=pass header.i=@qq.com\n" +
                "X-Coremail-Antispam: 1Uf129KBjDUn29KB7ZKAUJUUUUU529EdanIXcx71UUUUU7v73\n" +
                "\tVFW2AGmfu7bjvjm3AaLaJ3UbIYCTnIWIevJa73UjIFyTuYvjxUs8sqDUUUU\n" +
                "Date: Sat, 25 May 2019 12:40:05 +0800 (CST)\n" +
                "\n" +
                "啦啦啦啦\n" +
                ".\n" +
                "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";

        Matcher matcher = Pattern.compile("^(?i)From:(.*?)\n(?!\\s)", Pattern.DOTALL | Pattern.MULTILINE).matcher(s);
        System.out.println(matcher.find());
    }
}

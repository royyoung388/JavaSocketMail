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
                            byte[] source = Base64.getDecoder().decode(matcher.group(3).replace("\r\n", ""));
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
        Pattern pattern = Pattern.compile("From: ((.|\n|\r)*?)\n(?!\\s)");
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
        Pattern pattern = Pattern.compile("To: ((.|\n|\r)*?)\n(?!\\s)");
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
        Pattern pattern = Pattern.compile("Date: ((.|\n|\r)*?)\n(?!\\s)");
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
                date = sdf.parse(matcher.group(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseSubject() {
        Pattern pattern = Pattern.compile("Subject: (.*?)\n(?!\\s)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            subject = decode(matcher.group(1));
        }
    }

    private void parseContent() {
        Pattern pattern = Pattern.compile("Content-Type: (.*?)\n(?!\\s)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(mail);
        if (matcher.find()) {
            String type = matcher.group(1);
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
                String encoding = "base64";
                if (matcherEncoding.find()) {
                    encoding = matcherEncoding.group(1);
                }
                System.out.println("encoding = " + encoding);

                switch (encoding) {
                    case "quoted-printable":
                        content += QuotedPrintable.decode(body.trim().getBytes(), charset);
                        break;
                    case "base64":
                        try {
                            byte[] source = Base64.getDecoder().decode(body.trim());
                            content += new String(source, charset);
                        } catch (IllegalArgumentException e) {
                            try {
                                byte[] source = Base64.getDecoder().decode(body.trim().replace("\r\n", ""));
                                content += new String(source, charset);
                            } catch (UnsupportedEncodingException ex) {
                                ex.printStackTrace();
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }

    private void parsePart(String part, String boundary) {
        String re = String.format("(?<=--%s)((.)*?)(?=--%s)", boundary, boundary);
        Matcher matcherBoundary = Pattern.compile(re, Pattern.DOTALL).matcher(part);

        while (matcherBoundary.find()) {

            Matcher matcherType = Pattern.compile("Content-Type: (.*?)\n(?!\\s)", Pattern.DOTALL).matcher(matcherBoundary.group(1));
            String type = null;
            if (matcherType.find())
                type = matcherType.group(1);

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
                Matcher matcherEncoding = Pattern.compile("Content-Transfer-Encoding: (.*?)\r\n(?!\\s)", Pattern.DOTALL)
                        .matcher(matcherBoundary.group(1));
                String encoding = "base64";
                if (matcherEncoding.find()) {
                    encoding = matcherEncoding.group(1);
                }
                System.out.println("encoding = " + encoding);

                //获取content
                Matcher matcherContent = Pattern.compile("\r\n\r\n(.*)", Pattern.DOTALL).matcher(matcherBoundary.group(1));
                if (matcherContent.find()) {
                    System.out.println("matcherContent = " + matcherContent.group(1));
                    switch (encoding) {
                        case "quoted-printable":
                            content += QuotedPrintable.decode(matcherContent.group(1).trim().getBytes(), charset);
                            break;
                        case "base64":
                            try {
                                byte[] source = Base64.getDecoder().decode(matcherContent.group(1).trim());
                                content += new String(source, charset);
                            } catch (IllegalArgumentException e) {
                                try {
                                    byte[] source = Base64.getDecoder().decode(matcherContent.group(1).trim().replace("\r\n", ""));
                                    content += new String(source, charset);
                                } catch (UnsupportedEncodingException ex) {
                                    ex.printStackTrace();
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
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
        String a = "VGhpcyBpcyBhbiBhdXRvbWF0aWNhbGx5IGdlbmVyYXRlZCBlLW1haWwgZnJvbSBOaW50ZW5kby4K\r\n" +
                "Ci0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0KVGhhbmsgeW91IGZvciB5b3VyIGludGVyZXN0IGlu\r\n" +
                "IHNldHRpbmcgdXAgYSBOaW50ZW5kbyBBY2NvdW50IHRvIHVzZSB3aXRoIHRoZSBOaW50ZW5kbyBT\r\n" +
                "d2l0Y2ggc3lzdGVtLgoKTGlua2luZyBhIE5pbnRlbmRvIEFjY291bnQgdG8gdGhlIHVzZXIgYWNj\r\n" +
                "b3VudCBvbiB5b3VyIHN5c3RlbSBwcm92aWRlcyBhY2Nlc3MgdG8gdmFyaW91cyBmZWF0dXJlcyBv\r\n" +
                "biBOaW50ZW5kbyBTd2l0Y2gsIGluY2x1ZGluZyBhY2Nlc3MgdG8gTmludGVuZG8gZVNob3AuIFlv\r\n" +
                "dSBjYW4gYWxzbyBlYXJuIHBvaW50cyBmb3IgdGhlIE15IE5pbnRlbmRvIHJld2FyZHMgcHJvZ3Jh\r\n" +
                "bSBvbiBlbGlnaWJsZSBwdXJjaGFzZXMgbWFkZSBvbiBOaW50ZW5kbyBTd2l0Y2guCgpQbGVhc2Ug\r\n" +
                "dXNlIHRoZSBVUkwgYmVsb3cgdG8gY3JlYXRlIGEgTmludGVuZG8gQWNjb3VudC4gT25jZSB5b3Ug\r\n" +
                "aGF2ZSBjcmVhdGVkIHlvdXIgTmludGVuZG8gQWNjb3VudCwgeW91IGNhbiBhbHNvIGNyZWF0ZSBO\r\n" +
                "aW50ZW5kbyBBY2NvdW50cyBmb3IgY2hpbGRyZW4gKHVuZGVyIDEzIHllYXJzIG9mIGFnZSkgYnkg\r\n" +
                "c2VsZWN0aW5nICJQYXJlbnRhbCBjb250cm9scyIgaW4geW91ciBOaW50ZW5kbyBBY2NvdW50IHNl\r\n" +
                "dHRpbmdzIGFuZCB0aGVuICJDcmVhdGUgYW4gYWNjb3VudCBmb3IgYSBjaGlsZC4iCgpUbyBsaW5r\r\n" +
                "IHlvdXIgTmludGVuZG8gQWNjb3VudCB0byBOaW50ZW5kbyBTd2l0Y2gsIHBsZWFzZSBkbyB0aGUg\r\n" +
                "Zm9sbG93aW5nOgoKICAxLiBBY2Nlc3MgU3lzdGVtIFNldHRpbmdzIG9uIE5pbnRlbmRvIFN3aXRj\r\n" +
                "aCwgYW5kIHNlbGVjdCB0aGUgdXNlciBhY2NvdW50IHlvdSB3b3VsZCBsaWtlIHRvIGxpbmsuCgog\r\n" +
                "IDIuIFNlbGVjdCAiTGluayBOaW50ZW5kbyBBY2NvdW50LiIKCiAgMy4gU2VsZWN0ICJTaWduIElu\r\n" +
                "IGFuZCBMaW5rLiIKCiAgNC4gRm9sbG93IHRoZSBvbi1zY3JlZW4gaW5zdHJ1Y3Rpb25zIHRvIGxp\r\n" +
                "bmsgeW91ciBOaW50ZW5kbyBBY2NvdW50IHRvIHlvdXIgdXNlciBhY2NvdW50LgoKSWYgeW91IHdv\r\n" +
                "dWxkIGxpa2UgdG8gbGluayB5b3VyIE5pbnRlbmRvIEFjY291bnQgdXNpbmcgdGhlIDUtZGlnaXQg\r\n" +
                "Y29uZmlybWF0aW9uIGNvZGUsIGxvY2F0ZSB0aGUgY29kZSBmb3IgdGhlIGFwcHJvcHJpYXRlIGFj\r\n" +
                "Y291bnQgYWZ0ZXIgdGhlIE5pbnRlbmRvIEFjY291bnQgaXMgY3JlYXRlZC4KCmh0dHBzOi8vYWNj\r\n" +
                "b3VudHMubmludGVuZG8uY29tL2xvZ2luL2VtYWlsL2xhbmRpbmc/Y2hhbGxlbmdlX2lkPTAzYzQ3\r\n" +
                "OTU2LWM4NmQtNDE4My04ODJkLTI0OGQ3MjEwMDFkMgoKUGxlYXNlIGJlIGF3YXJlIHRoYXQgaWYg\r\n" +
                "eW91IGRvIG5vdCBjb21wbGV0ZSB0aGlzIHByb2Nlc3Mgd2l0aGluIDI0IGhvdXJzLCB0aGUgYWJv\r\n" +
                "dmUgVVJMIHdpbGwgYmVjb21lIGludmFsaWQuCgpJZiB5b3UgZG8gbm90IGtub3cgd2h5IHlvdSBo\r\n" +
                "YXZlIHJlY2VpdmVkIHRoaXMgZS1tYWlsLCBwbGVhc2UgZGVsZXRlIGl0LgoKLS0tLS0tLS0tLS0t\r\n" +
                "LS0tLS0tLS0tLS0tLQpTaW5jZXJlbHksCk5pbnRlbmRvIENvLiwgTHRkLgoKWW91IGNhbm5vdCBy\r\n" +
                "ZXBseSB0byB0aGlzIGUtbWFpbCBhZGRyZXNzLgpJZiB5b3UgaGF2ZSBhbnkgcXVlc3Rpb25zLCBw\r\n" +
                "bGVhc2Ugc2VlIHRoZSBpbmZvcm1hdGlvbiBiZWxvdyBvbiBob3cgdG8gY29udGFjdCB1cy4KCuKW\r\n" +
                "vU5pbnRlbmRvIEFjY291bnQgfCBGQVEKaHR0cHM6Ly9hY2NvdW50cy5uaW50ZW5kby5jb20vY29t\r\n" +
                "bW9uX2hlbHAKCg==\r\n";
        String content = "";
        String charset = "utf-8";

        try {
            byte[] source = Base64.getDecoder().decode(a);
            content += new String(source, charset);
        } catch (IllegalArgumentException e) {
            try {
                byte[] source = Base64.getDecoder().decode(a.replace("\r\n", "").trim());
                content += new String(source, charset);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

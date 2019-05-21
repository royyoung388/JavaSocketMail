package pop3;

public class POP3Exception extends Throwable {
    private String message;

    public POP3Exception(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

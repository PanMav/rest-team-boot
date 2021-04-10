package my.approach.team.error;

public class UnauthorizedApiAccessException extends RuntimeException {
    private String reason;

    public UnauthorizedApiAccessException(String reason) {
        super("Access forbidden: " + reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

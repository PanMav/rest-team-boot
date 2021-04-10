package my.approach.team.error;

public class PaginationNotSupportedException extends UnsupportedOperationException {
    public PaginationNotSupportedException(String message) {
        super(message);
    }
}

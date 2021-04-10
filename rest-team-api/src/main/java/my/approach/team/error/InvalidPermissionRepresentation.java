package my.approach.team.error;

public class InvalidPermissionRepresentation extends RuntimeException {
    public InvalidPermissionRepresentation(String message) {
        super(message);
    }
}

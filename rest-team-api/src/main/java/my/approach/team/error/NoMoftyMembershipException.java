package my.approach.team.error;

public class NoMoftMembershipException extends RuntimeException {
    public NoMoftMembershipException(String username) {
        super(String.format("User `%s` does not have MOFT membership", username));
    }
}

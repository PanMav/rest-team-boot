package my.approach.team.error;

public class InconsistentEntityIdException extends RuntimeException {
    public InconsistentEntityIdException(Long pathId, Long memberedId) {
        super(String.format("Path and request body have different entity ids: %d / %d", pathId, memberedId));
    }
}

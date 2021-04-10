package my.approach.team.model.dto.s2s;

import lombok.Data;

import java.util.List;

@Data
public class IdmUser {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<IdmUserMembership> memberships;
    private String status;
    private boolean deleted;

    public IdmUserMembership getMembership() {
        return memberships.stream().findFirst().get();
    }
}

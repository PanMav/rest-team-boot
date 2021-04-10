package my.approach.team.model.dto.s2s;

import lombok.Data;

import java.util.List;

@Data
public class IdmUserMembership {
    private String legalEntityType;
    private String legalEntityUuid;
    private String legalEntityName;
    private List<String> roles;
    private boolean primary;
}

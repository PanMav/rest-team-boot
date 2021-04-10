package my.approach.team.model.domain.entities;

import my.approach.team.util.Util;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//This class is intended to be used for filtering teams (Search RestAPI)
public class TeamFilter {
    private String teamId;
    private String teamCode;
    private String teamName;
    private String teamType;
    private String teamReferenceIDs;//Many teams foreach
    private String memberId;//Many members foreach
    private String collectionOfMembersHash; //Unique

    @ApiModelProperty(hidden= true)
    public boolean isFilterEmpty() {
        return Util.isAllNull
                (teamId,teamCode,teamName,teamType, teamReferenceIDs,memberId,collectionOfMembersHash);
        }
}

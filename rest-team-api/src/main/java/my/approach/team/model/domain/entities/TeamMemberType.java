package my.approach.team.model.domain.entities;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "THH_GRP_MEMB_TEAM_TYPES")
public class TeamMemberType {
    @Id
    @Column(name = "GRP_MEMB_TEAM_TYPE_ID")
    @ApiModelProperty(hidden= true)
    private int id;

    @Column(name = "MEMB_TEAM_TYPE")
    private String memberTeamType;

}

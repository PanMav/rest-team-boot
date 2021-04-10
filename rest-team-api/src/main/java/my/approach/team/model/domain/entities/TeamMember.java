package my.approach.team.model.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Entity
@Setter
@Getter
@Table(name = "THH_GRP_TEAM_MEMBERS")
@SequenceGenerator(name = "SEQ_GRP_TEAM_MEMBERS", sequenceName = "SEQ_GRP_TEAM_MEMBERS", allocationSize = 1)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TeamMember {
    @Id
    @Column(name = "GRP_TEAM_MEMBER_ID")
    @ApiModelProperty(hidden= true)
    @GeneratedValue(generator = "SEQ_GRP_TEAM_MEMBERS")
    private Long id;

    @JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Team teamId;

    @Column(name = "MEMBER_ID")
    @EqualsAndHashCode.Include
    @JsonView({Views.SubstanceTeamsList.class})
    private String memberId;

    @Column(name = "MEMB_TEAM_TYPE_ID")
    @ApiModelProperty(hidden= true)
    private int memberTypeId;

    @EqualsAndHashCode.Include
    @Transient
    @JsonView({Views.SubstanceTeamsList.class})
    private String memberType;
}

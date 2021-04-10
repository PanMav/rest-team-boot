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
@Table(name = "THH_GRP_TEAM_EXTENDED_INFO")
@SequenceGenerator(name = "SEQ_GRP_TEAM_EXTENDED_INFO", sequenceName = "SEQ_GRP_TEAM_EXTENDED_INFO", allocationSize = 1)
public class ExtendedInfo {
    @Id
    @ApiModelProperty(hidden= true)
    @Column(name = "GRP_TEAM_EXTENDED_INFO_ID")
    @GeneratedValue(generator = "SEQ_GRP_TEAM_EXTENDED_INFO")
    @EqualsAndHashCode.Include
    private Long id;

    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID", referencedColumnName = "TEAM_ID")
    @JsonBackReference
    private Team extendedInfoTeamId;

    @EqualsAndHashCode.Include
    @Column(name = "KEY")
    @JsonView({Views.SubstanceTeamsList.class})
    private String key;

    @EqualsAndHashCode.Include
    @Column(name = "VALUE")
    @JsonView({Views.SubstanceTeamsList.class})
    private String value;

}

package my.approach.team.model.domain.entities;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "THH_GRP_ACTION_HIST")
@SequenceGenerator(name = "SEQ_GRP_TEAM_ACTION_HIST", sequenceName = "SEQ_GRP_TEAM_ACTION_HIST", allocationSize = 1)
@EntityListeners(AuditingEntityListener.class)
public class TeamHistory {

    @Id
    @Column(name = "GRP_ACTION_HIST_ID")
    @GeneratedValue(generator = "SEQ_GRP_TEAM_ACTION_HIST")
    private Long id;

    @Column(name = "TEAM_ID")
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamId;

    @Column(name = "SRC_BUSINESS_PROC_CODE_ORIGIN")
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamType;

    @CreatedBy
    @Column(name = "USERNAME")
    @JsonView({Views.SubstanceTeamsList.class})
    private String userName;

    @Column(name = "ROLE_NAME")
    @JsonView({Views.SubstanceTeamsList.class})
    private String roleName;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @JsonView({Views.SubstanceTeamsList.class})
    private LocalDateTime createdDate;

    @Column(name = "ACTION_TYPE")
    @JsonView({Views.SubstanceTeamsList.class})
    private String actionType;

    @Column(name = "ACTION_PARAMS")
    @JsonView({Views.SubstanceTeamsList.class})
    private String actionParams;

    @Column(name = "ACTION_RESPONSE")
    @JsonView({Views.SubstanceTeamsList.class})
    private String actionResponse;

    @Column(name = "ACTION_RESULT")
    @JsonView({Views.SubstanceTeamsList.class})
    private String actionResult;
}

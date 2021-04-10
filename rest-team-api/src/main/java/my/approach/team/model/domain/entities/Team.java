package my.approach.team.model.domain.entities;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import my.approach.team.serialization.Views;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "THH_GRP_TEAM")
@SequenceGenerator(name = "SEQ_GRP_TEAM", sequenceName = "SEQ_GRP_TEAM", allocationSize = 1)
public class Team implements Serializable {

    @Id
    @Column(name = "GRP_TEAM_ID")
    @GeneratedValue(generator = "SEQ_GRP_TEAM")
    @ApiModelProperty(hidden= true)
    private Long id;

    @ApiModelProperty(hidden = true, required = true, notes = "A unique identifier assigned by the Team Registry. " +
            "By adopting a unique ID in the level of the Team enables to monitor " +
            "the evolution of the team within a system / process and across systems (i.e. GGT, GMT and OPP/SIGMA).")
    @Column(name = "TEAM_ID")
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamId;

    @ApiModelProperty(required = true, notes = " The ID of the team assigned by the business process/system. " +
            "Only distinct pairs <TeamCode,TeamType> are allowed in the system.")
    @Column(name = "TEAM_CODE")
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamCode;


    @ApiModelProperty(required = true, notes = "The name of the team assigned by the business process/system. " +
            "Only distinct pairs <TeamName, TeamType> are allowed in the system.")
    @Column(name = "TEAM_NAME")
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamName;

    @Column(name = "TEAM_REFERENCES")
    @ApiModelProperty(hidden= true)
    private String teamReferenceValues;

    @Transient
    @ApiModelProperty(notes = " Used for referencing one or more teams, such as for example in case a GMT team references one or more GGT teams." +
            "The reference is always made upon one or more TeamIDs, which must be always checked for correct formatting and existence.")
    @JsonView({Views.SubstanceTeamsList.class})
    private List<String> teamReferenceIDs = new ArrayList<>();


    @ApiModelProperty(required = true, notes = "Defines the system/business process which created and/or edited the Team.")
    @Transient
    @JsonView({Views.SubstanceTeamsList.class})
    private String teamType;

    @ApiModelProperty(hidden= true)
    @Column(name = "BUSINESS_SRC_PROC_CODE_ID")
    private int teamTypeId;

    @ApiModelProperty(required = true, notes = "Timestamp for recording the creation of a Team in the registry. (Autofilled)")
    @Column(name = "CREATED_DATE", updatable = false)
    @JsonView({Views.SubstanceTeamsList.class})
    private LocalDateTime createdDate;


    @ApiModelProperty(required = true, notes = "Timestamp for recording the editing of a Team in the registry. (Autofilled)")
    @Column(name = "UPDATED_DATE")
    @JsonView({Views.SubstanceTeamsList.class})
    private LocalDateTime updateDate;

    @JsonView({Views.SubstanceTeamsList.class})
    @Column(name = "COLLECTION_OF_MEMBERS_HASH")
    @ApiModelProperty(notes = "Used for storing a fingerprint for the team members contained in the team.\n" +
            "Its content should be an MD5 Hash function which is calculated by taking into account all team’s members" +
            " (which are represented by OPP IDs in case the team member is a membered and by Team Registry's IDs in case the team member is a team).\n" +
            "If the members of a team change then the function should be recalculated in order to depict the team’s current status.")
    private String collectionOfMembersHash;

    @JsonView({Views.SubstanceTeamsList.class})
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "teamId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonSerialize(as = LinkedHashSet.class)
    @JsonDeserialize(as = LinkedHashSet.class)
    @ApiModelProperty(notes = "Used for defining the members of a team which can be either membereds or teams.\n" +
            "    A member is defined by providing:\n" +
            "    Its ID, which is an OPP ID in case of membereds and a Team Registry's ID in case of a team and\n" +
            "    Its type which can be either Substance or Team")
    private Set<TeamMember> members = new LinkedHashSet<>();

    @ApiModelProperty(notes = "  Used for storing extended information concerning a team in a <key,value> pair. Supported key types are the following key “types” are the following:\n" +
            "    ReferenceTeamCode: The ID of the reference team in the Source System. This information always relates to the content of the “teamReferenceIDs” field.  A team may have more than one referenced teams.\n" +
            "            ReferenceTeamType: The Source system to which the ReferenceTeamCode, refers to. Only one source system may be referred for a single team\n" +
            "    Active: Sets the status of a team as active/inactive")
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "extendedInfoTeamId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView({Views.SubstanceTeamsList.class})
    private Set<ExtendedInfo> extendedInfo = new LinkedHashSet<>();
}

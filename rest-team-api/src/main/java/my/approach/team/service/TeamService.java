package my.approach.team.service;

import my.approach.team.model.domain.entities.Team;
import my.approach.team.model.domain.entities.TeamFilter;
import my.approach.team.model.domain.entities.TeamHistory;
import my.approach.team.model.domain.entities.TeamHistoryFilter;
import my.approach.team.validation.CreateTeamValid;
import my.approach.team.validation.UpdateTeamValid;
import my.approach.team.validation.ValidTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.teams.Default;

@Validated
public interface TeamService extends SecureEntityService<Team> {

    @Validated({Default.class, CreateTeamValid.class})
    Team createTeam(@Valid @ValidTeam(teams = CreateTeamValid.class) Team team);
    @Validated({Default.class, UpdateTeamValid.class})
    Team updateTeam(@Valid @ValidTeam(teams = UpdateTeamValid.class) Team team);
    Team findTeam(String teamId);
    Page<Team> findTeams(TeamFilter filter, Pageable pageable);
    boolean teamExists(String teamId);
    void canUpdateGMTTeam();
    void canUpdateGGTTeam();
    void canUpdateRegulatedTeam();
    void canCreateGMTTeam();
    void canCreateGGTTeam();
    void canCreateRegulatedTeam();
    void createHistoryActionEntry(String teamId, String outcome, String teamType, String action, String givenArguments, String result);
    Page<TeamHistory> findHistoryActionEntries(TeamHistoryFilter teamHistoryFilter, Pageable pageable);
}

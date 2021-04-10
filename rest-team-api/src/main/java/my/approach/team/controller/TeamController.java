package my.approach.team.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.approach.team.auth.annotation.RequiresTeamReadPermission;
import my.approach.team.config.ApiPageable;
import my.approach.team.error.ErrorCodes;
import my.approach.team.teaming.model.domain.team.*;
import my.approach.team.model.domain.entities.*;
import my.approach.team.model.dto.ListApiResponse;
import my.approach.team.model.dto.SingleApiResponse;
import my.approach.team.persistence.repositories.TeamMemberTypeRepository;
import my.approach.team.persistence.repositories.TeamTypeRepository;
import my.approach.team.serialization.Views;
import my.approach.team.service.TeamService;
import my.approach.team.util.Constants;
import my.approach.team.util.Util;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/teams-membereds")
@Api(tags = { "Team Registry Rest Controller" })
public class TeamingController {
    private final TeamService teamService;
    private final TeamTypeRepository teamTypeRepository;
    private final TeamMemberTypeRepository teamMemberTypeRepository;



    @ApiResponses(value = {
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_REQUIRED_FIELDS, message = ErrorCodes.CodeDescription.Error_1001),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_EXTENDED_INFO, message = ErrorCodes.CodeDescription.Error_1003),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_1004),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_FOUND, message = ErrorCodes.CodeDescription.Error_1005),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_ACTIVE, message = ErrorCodes.CodeDescription.Error_1006),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_ID_NOT_VALID, message = ErrorCodes.CodeDescription.Error_1007),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_EXTENDED_INFO_OPP_ID_NOT_VALID, message = ErrorCodes.CodeDescription.Error_1008),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_ID_FORMAT_NOT_VALID_, message = ErrorCodes.CodeDescription.Error_2001),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_, message = ErrorCodes.CodeDescription.Error_2002),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_CODE_UNIQUE_PER_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_2003),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_NAME_UNIQUE_PER_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_2004),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_FORMAT_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2005),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_SNG_REF_GRP_TYPE, message = ErrorCodes.CodeDescription.Error_2006),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_TEAM_ID_NOT_FOUND, message = ErrorCodes.CodeDescription.Error_2007),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_TEAM_TYPE_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2008),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2009),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_MEMBER_TYPE_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2010),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_PAIRS_ONLY, message = ErrorCodes.CodeDescription.Error_2011),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_TYPE_INVALID_FORMAT, message = ErrorCodes.CodeDescription.Error_2012),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT, message = ErrorCodes.CodeDescription.Error_2013),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_ACTIVE_INVALID_VALUE, message = ErrorCodes.CodeDescription.Error_2014),

            @ApiResponse(code = 401, message = "Invalid token"),
            @ApiResponse(code = 403, message = "Unauthorized access to resource, or action is not permitted"),
            @ApiResponse(code = 404, message = "The specified resource was not found"),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @JsonView({Views.SubstanceTeamsList.class})
    @ApiOperation("Creates a new Team based on the JSON object provided (certain validations occur, described for each request)")
    public SingleApiResponse<Team> createSubstanceTeam(@RequestBody Team team) throws JsonProcessingException {
        canCreateSpecifiedTeam(team.getTeamType());
        adjustParametersForValidation(team);
        ObjectMapper mapper = new ObjectMapper();
        Team persistedTeam;
        try {
            persistedTeam = adjustParametersForJSONView(teamService.createTeam(team));
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.OK.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.CREATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(persistedTeam)
            );
        } catch (ConstraintViolationException e) {
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.BAD_REQUEST.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.CREATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(e.getMessage())
            );
            throw e;
        } catch (Exception e) {
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.INTERNAL_SERVER_ERROR.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.CREATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(e.getMessage())
            );
            throw e;
        }
        return Util.toSingleApiResponse(persistedTeam);
    }


    @ApiResponses(value = {
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_REQUIRED_FIELDS, message = ErrorCodes.CodeDescription.Error_1001),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_EXTENDED_INFO, message = ErrorCodes.CodeDescription.Error_1003),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MISSING_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_1004),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_FOUND, message = ErrorCodes.CodeDescription.Error_1005),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_ACTIVE, message = ErrorCodes.CodeDescription.Error_1006),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_MEMBER_ID_NOT_VALID, message = ErrorCodes.CodeDescription.Error_1007),
            @ApiResponse(code = ErrorCodes.Codes.CMP_TEAM_EXTENDED_INFO_OPP_ID_NOT_VALID, message = ErrorCodes.CodeDescription.Error_1008),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_ID_FORMAT_NOT_VALID_, message = ErrorCodes.CodeDescription.Error_2001),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_, message = ErrorCodes.CodeDescription.Error_2002),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_CODE_UNIQUE_PER_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_2003),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_NAME_UNIQUE_PER_TEAM_TYPE, message = ErrorCodes.CodeDescription.Error_2004),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_FORMAT_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2005),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_SNG_REF_GRP_TYPE, message = ErrorCodes.CodeDescription.Error_2006),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_TEAM_ID_NOT_FOUND, message = ErrorCodes.CodeDescription.Error_2007),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_TEAM_TYPE_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2008),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2009),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_MEMBER_TYPE_NOT_VALID, message = ErrorCodes.CodeDescription.Error_2010),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_PAIRS_ONLY, message = ErrorCodes.CodeDescription.Error_2011),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_TYPE_INVALID_FORMAT, message = ErrorCodes.CodeDescription.Error_2012),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT, message = ErrorCodes.CodeDescription.Error_2013),
            @ApiResponse(code = ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_ACTIVE_INVALID_VALUE, message = ErrorCodes.CodeDescription.Error_2014),

            @ApiResponse(code = 401, message = "Invalid token"),
            @ApiResponse(code = 403, message = "Unauthorized access to resource, or action is not permitted"),
            @ApiResponse(code = 404, message = "The specified resource was not found"),
    })
    @JsonView({Views.SubstanceTeamsList.class})
    @PutMapping(path = "{teamId}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Updates a Team based on the JSON object provided (certain validations occur, described for each request) a TeamID is required in the request path")
    public SingleApiResponse<Team> updateSubstanceTeam(@PathVariable("teamId") String teamId, @RequestBody Team team) throws JsonProcessingException {
        if (!teamService.teamExists(teamId)) {
            throw new EntityNotFoundException(String.format(("Specified teamId %s does not exist"), teamId));
        } else {
            canUpdateSpecifiedTeam(teamId);
            adjustParametersForValidation(team);
            team.setTeamId(teamId);
        }
        ObjectMapper mapper = new ObjectMapper();
        Team persistedTeam;
        try {
            persistedTeam = adjustParametersForJSONView(teamService.updateTeam(team));
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.OK.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.UPDATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(persistedTeam)
            );
        } catch (ConstraintViolationException e) {
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.BAD_REQUEST.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.UPDATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(e.getMessage())
            );
            throw e;
        } catch (Exception e) {
            teamService.createHistoryActionEntry(
                    team.getTeamId(), HttpStatus.INTERNAL_SERVER_ERROR.name(), team.getTeamType(),
                    Constants.ResourceActionTypes.UPDATE.name(),
                    mapper.writeValueAsString(team), mapper.writeValueAsString(e.getMessage())
            );
            throw e;
        }
        return Util.toSingleApiResponse(persistedTeam);
    }

    /**
     * The Team is retrieved from the DB and checked for the user
     * @param teamId
     */
    private void canUpdateSpecifiedTeam(String teamId) {

        int teamTypeId =
                Optional.of(teamService.findTeam(teamId).getTeamTypeId())
                        .orElseThrow(() -> new EntityNotFoundException("TeamId specified not found."));
        String teamType = teamTypeRepository.findById(teamTypeId).getBusinessProcCode();

        if (teamType != null) {
            switch (teamType) {
                case Constants.GMT_TEAM_TYPE:
                    teamService.canUpdateGMTTeam();
                    break;
                case Constants.GGT_TEAM_TYPE:
                    teamService.canUpdateGGTTeam();
                    break;
                case Constants.REGULATED_TEAM_TYPE:
                    teamService.canUpdateRegulatedTeam();
                    break;
            }
        }

    }

    /**
     * The teamType here is provided by the user
     * (if it is invalid/or does not exist) it will be rejected on the validation level)
     * @param teamType
     */
    private void canCreateSpecifiedTeam(String teamType) {

        if (teamType != null) {
            switch (teamType) {
                case Constants.GMT_TEAM_TYPE:
                    teamService.canCreateGMTTeam();
                    break;
                case Constants.GGT_TEAM_TYPE:
                    teamService.canCreateGGTTeam();
                    break;
                case Constants.REGULATED_TEAM_TYPE:
                    teamService.canCreateRegulatedTeam();
                    break;
            }
        }

    }

    @GetMapping("/history-teams")
    @ApiOperation("Returns a list of the operations applied on a specific team")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "No acceptable team history search parameters have been specified. " +
                    "Refer to the swagger API documentation for further information."),
    })
    @JsonView({Views.SubstanceTeamsList.class})
    @ApiPageable
    @RequiresTeamReadPermission
    public ListApiResponse<TeamHistory> getSubstanceTeamsHistory
            (TeamHistoryFilter teamHistoryFilter, @ApiIgnore Pageable pageable) {
        if (teamHistoryFilter.isFilterEmpty()) {
            throw new IllegalStateException
                    ("No acceptable team history search parameters have been specified." +
                            " Refer to the swagger API documentation for further information."
            );
        }
        return Util.toListApiResponse(teamService.findHistoryActionEntries(teamHistoryFilter, pageable));
    }

    @GetMapping("/search")
    @ApiOperation("Returns a list of teams in the system based on the search criteria provided, " +
            "JSON (body) request is also supported (API Path: api/teams-membereds). This interface is provided for browser web compatibility.")
    @JsonView({Views.SubstanceTeamsList.class})
    @ApiPageable
    @RequiresTeamReadPermission
    public ListApiResponse<Team> getSubstanceTeams
            (TeamFilter teamFilter, @ApiIgnore Pageable pageable) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        if (teamFilter.isFilterEmpty()) {
            teamService.createHistoryActionEntry(
                    "-1" , HttpStatus.BAD_REQUEST.name(), null,
                    Constants.ResourceActionTypes.SEARCH.name(),
                    mapper.writeValueAsString(teamFilter),
                    mapper.writeValueAsString("No acceptable team search parameters have been specified."));

            throw new IllegalStateException("No acceptable team search parameters have been specified.");
        }

        if (!Util.isAnyNullOrEmpty(teamFilter.getTeamType())) {
            TeamType teamType = teamTypeRepository.findIdByBusinessProcCode(teamFilter.getTeamType());

            if (teamType != null) {
                teamFilter.setTeamType(String.valueOf(teamType.getId()));
            } else {
                //Mark as invalid
                teamFilter.setTeamType("-1");
            }
        }
        Page<Team> teams = teamService.findTeams(teamFilter, pageable);

        for (Team team: teams
        ) {
            adjustParametersForJSONView(team);
        }

        teamService.createHistoryActionEntry(
                "-1", HttpStatus.OK.name(), null,
                Constants.ResourceActionTypes.SEARCH.name(),
                mapper.writeValueAsString(teamFilter), mapper.writeValueAsString(teams));

        return Util.toListApiResponse(teams);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @JsonView({Views.SubstanceTeamsList.class})
    @ApiOperation(value = "Hidden from Browsers, as get with body is not supported", hidden = true)
    @RequiresTeamReadPermission
    public ListApiResponse<Team> getSubstanceTeamsRequestBody(@RequestBody TeamFilter teamFilter, Pageable pageable) throws JsonProcessingException {
        return getSubstanceTeams(teamFilter, pageable);
    }

    private void adjustTeamType(Team team) {
        if (!Util.isAnyNullOrEmpty(team.getTeamType())) {
            TeamType teamType = teamTypeRepository.findIdByBusinessProcCode(team.getTeamType());

            if (teamType != null) {
                team.setTeamTypeId(teamType.getId());
            } else {
                team.setTeamTypeId(-1);
            }
        } else {
            team.setTeamTypeId(0);
        }
    }

    private void adjustReferenceValues(Team team) {
        if (team.getTeamReferenceValues() != null) {
            team.setTeamReferenceValues( String.join(",", team.getTeamReferenceIDs()));
        }
    }

    private void adjustMemberTypeValues(Team team) {
        for (TeamMember member: team.getMembers()
             ) {
            //TODO: Simplify calls (unite to one call = constant)
            TeamMemberType memberType =
                    teamMemberTypeRepository.findByMemberTeamType(member.getMemberType());
            if (memberType != null) {
                member.setMemberTypeId(memberType.getId());
            } else {
                //Mark as invalid
                member.setMemberTypeId(-1);
            }
        }
    }

    private void adjustParametersForValidation(Team team) {
        //Clearing potential teamId as it messes up validation if used in create
        team.setTeamId(null);
        adjustMemberTypeValues(team);
        adjustTeamType(team);
        adjustReferenceValues(team);
    }


    private Team adjustParametersForJSONView(Team persistedTeam) {

        if (persistedTeam.getTeamReferenceValues() != null
            && !persistedTeam.getTeamReferenceValues().isEmpty()) {
            persistedTeam.setTeamReferenceIDs
                    (Arrays.asList(persistedTeam.getTeamReferenceValues().split(",")));
        }
        persistedTeam.setTeamType
                (teamTypeRepository.findById(persistedTeam.getTeamTypeId()).getBusinessProcCode());

        for (TeamMember member: persistedTeam.getMembers()
        ) {
            //TODO: Simplify calls (unite to one call = constant)
            TeamMemberType memberType =
                    teamMemberTypeRepository.findById(member.getMemberTypeId());
            member.setMemberType(memberType.getMemberTeamType());
        }

    return persistedTeam;
    }
}

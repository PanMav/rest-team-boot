package my.approach.team.validation;


import my.approach.team.error.ErrorCodes;
import my.approach.team.model.domain.entities.ExtendedInfo;
import my.approach.team.model.domain.entities.Team;
import my.approach.team.model.domain.entities.TeamMember;
import my.approach.team.persistence.repositories.TeamRepository;
import my.approach.team.persistence.repositories.TeamTypeRepository;
import my.approach.team.service.TeamService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TeamValidationsTest {

    final ConstraintValidatorContext ctx = Mockito.mock(ConstraintValidatorContext.class);
    final ValidTeam validTeam = Mockito.mock(ValidTeam.class);
    final TeamRepository teamRepository = Mockito.mock(TeamRepository.class);
    final TeamService teamService = Mockito.mock(TeamService.class);
    final TeamTypeRepository teamTypeRepository = Mockito.mock(TeamTypeRepository.class);
    final ConstraintValidatorContext.ConstraintViolationBuilder builder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
    TeamValidator validator = new TeamValidator(teamService, teamRepository, teamTypeRepository);

    /*
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MISSING_REQUIRED_FIELDS));
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_));*/


    @Before
    public void setup() {
        Mockito.doReturn(builder).when(ctx).buildConstraintViolationWithTemplate(Mockito.anyString());
        Mockito.doReturn(null).when(builder).addPropertyNode(Mockito.anyString());
        Mockito.doReturn(Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)).when(builder).addPropertyNode(Mockito.anyString());
        Mockito.doReturn(new Class[]{CreateTeamValid.class}).when(validTeam).teams();
        Mockito.doReturn(true).when(teamRepository).checkDBValidTeam(any(), any());

        /**
         * TeamTypes: GGT/GMT/REGULATED are: 1/2/3 respectively
         */

    }

    private Team createAcceptableTeam() {
        final Team team = new Team();
        team.setTeamId("GRID-9000");
        team.setTeamCode("DYNCA_ID_01");
        team.setTeamName("Bisphenol");
        team.setTeamTypeId(1);
        team.setTeamType("GGT");

        return team;
    }

    private TeamMember createBasicTeamMember() {
        final TeamMember teamMember = new TeamMember();
        teamMember.setMemberTypeId(2);
        teamMember.setMemberId("GRID-11111");

        return teamMember;
    }

    @Test
    public void testTeam_Operation_Create_ValidatorIsDefined() {
        for (Method method : TeamService.class.getMethods()) {
            if (method.getName().contains("create")) {
                assertTrue(method.getAnnotation(Validated.class).toString().contains("CreateTeamValid"));
            } else if (method.getName().contains("update")) {
                assertTrue(method.getAnnotation(Validated.class).toString().contains("UpdateTeamValid"));
            }
        }
    }

    @Test
    public void testTeamProvided_OK() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        validator.initialize(validTeam);
        assertTrue(validator.isValid(team, ctx));

        //Validation
        verify(builder, times(0)).addPropertyNode("");
    }

    @Test
    public void testTeamProvided_NotOK_TeamIDMissing() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        //Introduce incompatibility/error
        //ID should not match the given teamCode
        team.setTeamTypeId(3);
        Mockito.doReturn(false).when(teamRepository).checkDBValidTeam(any(), any());

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_));

    }

    @Test
    public void testTeamProvided_NotOK_TeamCodeMissing() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        //Introduce incompatibility/error
        team.setTeamCode(null);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MISSING_REQUIRED_FIELDS));

    }

    @Test
    public void testTeamProvided_NotOK_TeamTypeMissing() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        //Introduce incompatibility/error
        team.setTeamTypeId(-1);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_TEAM_TYPE_NOT_VALID));

    }

    @Test
    //Acceptable
    public void testTeamProvided_OK_TeamReferences_Null() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        //Introduce incompatibility/error
        team.setTeamReferenceValues(null);

        validator.initialize(validTeam);
        assertTrue(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(0)).addPropertyNode(any());

    }

    @Test
    //NotAcceptable
    public void testTeamProvided_NotOK_Members_MemberIdEmpty() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error
        TeamMember member = createBasicTeamMember();
        member.setMemberId("");
        member.setMemberType("Team");

        team.getMembers().add(member);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(2)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MEMBER_ID_NOT_VALID));
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID));
    }

    @Test
    //NotAcceptable
    public void testTeamProvided_NotOK_Members_MemberIdAndTypeEmpty() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error
        TeamMember member = createBasicTeamMember();
        member.setMemberId(null);
        //Member type always has a value (@Controller)
        member.setMemberType("");
        Set<TeamMember> members = new HashSet<>();
        members.add(member);
        team.setMembers(members);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations (No type defined, no need to verify id)
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MEMBER_ID_NOT_VALID));
    }

    @Test
    //NotAcceptable
    public void testTeamProvided_NotOK_ExtendedInfo_Not_Complete() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error

        ExtendedInfo info = new ExtendedInfo();
        info.setKey("InvalidKey");
        info.setValue("true");
        team.getExtendedInfo().add(info);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(2)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_SNG_REF_GRP_TYPE));
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_PAIRS_ONLY));
    }


    @Test
    //NotAcceptable
    public void testTeamProvided_NotOK_ExtendedInfo_Invalid_ReferenceCodeType() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error

        ExtendedInfo info = new ExtendedInfo();
        info.setKey("ReferenceTeamType");
        info.setValue("GMT!");
        team.getExtendedInfo().add(info);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT));
    }

    @Test
    //NotAcceptable
    public void testTeamProvided_NotOK_ExtendedInfo_Invalid_ReferenceCodeValue() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error

        ExtendedInfo info = new ExtendedInfo();
        info.setKey("ReferenceTeamType");
        info.setValue("GMT-WRONG");
        team.getExtendedInfo().add(info);

        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        verify(builder, times(1)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT));

    }

    @Test
    //NotAcceptable
    public void testTeamProvided_OK_ExtendedInfo_Valid() {
        //Recreate for each test
        Team team = createAcceptableTeam();

        Mockito.doReturn(false).when(teamService).teamExists(any());
        //Introduce incompatibility/error

        ExtendedInfo infoType = new ExtendedInfo();
        infoType.setKey("ReferenceTeamType");
        infoType.setValue("GMT");
        team.getExtendedInfo().add(infoType);

        ExtendedInfo infoValue = new ExtendedInfo();
        infoValue.setKey("ReferenceTeamCode");
        infoValue.setValue("GMT-WRONG");
        team.getExtendedInfo().add(infoValue);

        Mockito.doReturn(1L).when(teamTypeRepository).countByBusinessProcCode(any());
        Mockito.doReturn(false).when(teamRepository).checkDBValidTeam(any(), any());


        validator.initialize(validTeam);
        assertFalse(validator.isValid(team, ctx));

        //Validations
        //Duplicate TEAM type format exception
        verify(builder, times(2)).addPropertyNode(any());
        Mockito.verify(builder).addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_));
    }
}

package my.approach.team.validation;

import my.approach.team.error.ErrorCodes;
import my.approach.team.model.domain.entities.ExtendedInfo;
import my.approach.team.model.domain.entities.Team;
import my.approach.team.model.domain.entities.TeamMember;
import my.approach.team.persistence.repositories.TeamRepository;
import my.approach.team.persistence.repositories.TeamTypeRepository;
import my.approach.team.service.TeamService;
import my.approach.team.util.Constants;
import my.approach.team.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class TeamValidator implements ConstraintValidator<ValidTeam, Team> {
private final TeamService teamService;
private final TeamRepository teamRepository;
private final TeamTypeRepository teamTypeRepository;
    private Class<?> validationTeam;

    public void initialize(ValidTeam constraintAnnotation) {
        if (constraintAnnotation.teams().length != 1) {
            throw new IllegalStateException("SourceSubstance validator must have 1 and only 1 validation team");
        }
        validationTeam = constraintAnnotation.teams()[0];
    }

    public boolean isValid(Team team, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (validationTeam.equals(CreateTeamValid.class)) {
            return validateCreateTeam(team, context);
        } else if (validationTeam.equals(UpdateTeamValid.class)) {
            return validateUpdateTeam(team, context);
        }
        return false;
    }


    private boolean validateCreateTeam(Team team, ConstraintValidatorContext context) {
        log.info("Starting validation for given Team with ID: " + team.getTeamId());
        boolean presentTeamCodeNameType = presentTeamCodeNameTypeValues(team, context);
        boolean extendedInfoComplete = extendedInfoCompleteValid(team, context);
        boolean teamTypeGiven = isTeamTypeProvidedAndValid(team.getTeamTypeId(), context);
        boolean oppExistsAndActive = checkOPPExistsAndActive(team.getMembers(), context);//1006//1007
        boolean teamIdMemberExists = checkTeamIdMemberExists(team.getMembers(), context);
        boolean uniqueTeamAttributeCombination = checkUniqueCoreAttributeCombination(team, context);
        boolean teamReferenceCodesValid = checkReferenceIDs(team.getTeamReferenceIDs(), context);
        boolean memberTypesValid = checkMemberTypesValid(team.getMembers(), context);
        boolean teamCodeValid = checkTeamCodeValid(team.getTeamCode(), team.getTeamType(), context);
        log.info("finished validation for given Team with ID: " + team.getTeamId());

        return  presentTeamCodeNameType && teamTypeGiven && memberTypesValid
                && extendedInfoComplete && oppExistsAndActive && teamCodeValid
                && teamIdMemberExists && teamReferenceCodesValid && uniqueTeamAttributeCombination;
    }

    private boolean checkTeamCodeValid(String teamCode, String teamType, ConstraintValidatorContext context) {
        if (!Util.isAnyNullOrEmpty(teamCode) && !Util.isAnyNullOrEmpty(teamType)
                && !teamRepository.checkDBValidTeam(teamCode, teamType)) {
            context.buildConstraintViolationWithTemplate(
                    String.format(ErrorCodes.CodeDescription.Error_2002, teamCode)
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_CODE_FORMAT_NOT_VALID_))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean validateUpdateTeam(Team team, ConstraintValidatorContext context) {
        return validateCreateTeam(team, context);
    }

    private boolean checkMemberTypesValid(Set<TeamMember> members, ConstraintValidatorContext context) {
        boolean memberTypesValid = true;
        for (TeamMember member : members
             ) {
            if (member.getMemberTypeId() == -1) {
                context.buildConstraintViolationWithTemplate(
                        String.format(ErrorCodes.CodeDescription.Error_2010, member.getMemberType())
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_MEMBER_TYPE_NOT_VALID))
                        .addConstraintViolation();
                memberTypesValid = false;

            } else if (member.getMemberType().equals(Constants.MemberTypes.Substance.name())) {
                if (!matchedSubstanceFormat(member.getMemberId())) {
                    context.buildConstraintViolationWithTemplate(
                            String.format(ErrorCodes.CodeDescription.Error_2009, member.getMemberId())
                    )
                            .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID))
                            .addConstraintViolation();
                    memberTypesValid = false;

                } else if (!teamRepository.checkDBValidOPPExists(member.getMemberId())) {
                    context.buildConstraintViolationWithTemplate(
                            String.format(ErrorCodes.CodeDescription.Error_1005, member.getMemberId())
                    )
                            .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_FOUND))
                            .addConstraintViolation();
                    memberTypesValid = false;

                } else if (!teamRepository.checkDBOPPActive(member.getMemberId())) {
                    context.buildConstraintViolationWithTemplate(
                            String.format(ErrorCodes.CodeDescription.Error_1006, member.getMemberId())
                    )
                            .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MEMBER_OPP_ID_NOT_ACTIVE))
                            .addConstraintViolation();
                    memberTypesValid = false;
                }


            } else if (member.getMemberType().equals(Constants.MemberTypes.Team.name())) {
                if (!matchedTeamFormat(member.getMemberId())) {
                    context.buildConstraintViolationWithTemplate(
                            String.format(ErrorCodes.CodeDescription.Error_2009, member.getMemberId())
                    )
                            .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_MEMBERS_IDENTIFIER_NOT_VALID))
                            .addConstraintViolation();
                    memberTypesValid = false;
                }
            }
        }
        return memberTypesValid;
    }

    private boolean checkReferenceIDs(List<String> teamReferenceCodes, ConstraintValidatorContext context) {
        boolean teamReferenceIDsValid = true;
        for (String referenceCode: teamReferenceCodes
             ) {
            if (!matchedTeamFormat(referenceCode)) {
                context.buildConstraintViolationWithTemplate(
                        String.format(ErrorCodes.CodeDescription.Error_2005, referenceCode)
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_FORMAT_NOT_VALID))
                        .addConstraintViolation();
                teamReferenceIDsValid = false;
                //Skip check on DB since format is not valid
                continue;

            }

            if (teamRepository.countByTeamId(referenceCode) == 0) {
                context.buildConstraintViolationWithTemplate(
                        String.format(ErrorCodes.CodeDescription.Error_2007, referenceCode)
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_REFERENCE_ID_TEAM_ID_NOT_FOUND))
                        .addConstraintViolation();
                teamReferenceIDsValid = false;
            }
        }
        return teamReferenceIDsValid;
    }

    private boolean checkUniqueCoreAttributeCombination(Team team, ConstraintValidatorContext context) {
        boolean uniqueAttributeCombination = true;

        if (teamRepository.countByTeamCodeAndTeamTypeIdAndTeamIdNotLike
                (team.getTeamCode(), team.getTeamTypeId(), String.valueOf(team.getTeamId())) > 0) {
            context.buildConstraintViolationWithTemplate(
                    String.format(ErrorCodes.CodeDescription.Error_2003, team.getTeamCode())
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_CODE_UNIQUE_PER_TEAM_TYPE))
                    .addConstraintViolation();

            uniqueAttributeCombination = false;
        }
        if (teamRepository.countByTeamNameAndTeamTypeIdAndTeamIdNotLike
                (team.getTeamName(), team.getTeamTypeId(), String.valueOf(team.getTeamId())) > 0) {
            context.buildConstraintViolationWithTemplate(
                    String.format(ErrorCodes.CodeDescription.Error_2004, team.getTeamName())
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_NAME_UNIQUE_PER_TEAM_TYPE))
                    .addConstraintViolation();

            uniqueAttributeCombination = false;
        }

        return  uniqueAttributeCombination;
    }


    private boolean isTeamTypeProvidedAndValid(int teamType, ConstraintValidatorContext context) {
        boolean teamTypeProvidedAndValid = true;
        if (teamType == 0) {
            context.buildConstraintViolationWithTemplate(
                    String.format(ErrorCodes.CodeDescription.Error_1004)
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MISSING_TEAM_TYPE))
                    .addConstraintViolation();
            teamTypeProvidedAndValid = false;

        } else if (teamType < 0) {
            context.buildConstraintViolationWithTemplate(
                    ErrorCodes.CodeDescription.Error_2008
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_TEAM_TYPE_NOT_VALID))
                    .addConstraintViolation();
            teamTypeProvidedAndValid = false;
        }
        return teamTypeProvidedAndValid;
    }

    private boolean checkTeamIdMemberExists(Set<TeamMember> members, ConstraintValidatorContext context) {
        for (TeamMember member: members
             ) {
           if (member.getMemberTypeId() == 2) {
                if (!teamService.teamExists(member.getMemberId())) {
                    context.buildConstraintViolationWithTemplate(
                            String.format(ErrorCodes.CodeDescription.Error_1007, member.getMemberId())
                    )
                            .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MEMBER_ID_NOT_VALID))
                            .addConstraintViolation();
                    return false;
               }
           }
        }
        return true;
    }

    private boolean checkOPPExistsAndActive(Set<TeamMember> members, ConstraintValidatorContext context) {
        return true;
    }

    private boolean presentTeamCodeNameTypeValues(Team team, ConstraintValidatorContext context) {
        if (Util.isAnyNullOrEmpty(
                team.getTeamCode(), team.getTeamName())
                || team.getTeamTypeId() == 0) {

            context.buildConstraintViolationWithTemplate(
                    ErrorCodes.CodeDescription.Error_1001
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MISSING_REQUIRED_FIELDS))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean teamIdValidFormat(String teamId, ConstraintValidatorContext context) {

        if (!matchedTeamFormat(teamId)) {
            context.buildConstraintViolationWithTemplate(
                    ErrorCodes.CodeDescription.Error_2001
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_ID_FORMAT_NOT_VALID_))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean matchedTeamFormat(String teamId) {
        final String teamIDRegex = "GRID-[0-9]{5}";
        final Pattern pattern = Pattern.compile(teamIDRegex);
        final Matcher matcher = pattern.matcher(teamId);
        return matcher.find() && teamId.length() == 10;
    }

    private boolean matchedSubstanceFormat(String memberedId) {
        final String teamIDRegex = "[0-9]{3}.[0-9]{3}.[0-9]{3}";
        final Pattern pattern = Pattern.compile(teamIDRegex);
        final Matcher matcher = pattern.matcher(memberedId);
        return matcher.find() && memberedId.length() == 11;
    }

    private boolean extendedInfoCompleteValid(Team team, ConstraintValidatorContext context) {
        boolean extendedInfoValid = true;
        String extendedInfoRefTeamType = "";

        if (team.getExtendedInfo() == null
                || team.getExtendedInfo().size() == 0) {
            return true;
        }

        for (ExtendedInfo info : team.getExtendedInfo()
        ) {
            if (Util.isAnyNullOrEmpty(info.getKey())
                    || Util.isAnyNullOrEmpty(info.getValue())) {
                context.buildConstraintViolationWithTemplate(
                        ErrorCodes.CodeDescription.Error_1003
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_MISSING_EXTENDED_INFO))
                        .addConstraintViolation();

                extendedInfoValid = false;
            } else if (info.getKey().equals("ReferenceTeamType")
                    && teamTypeRepository.countByBusinessProcCode(info.getValue()) == 0) {
                //TODO: Bring Proc table for offline use
                context.buildConstraintViolationWithTemplate(
                        ErrorCodes.CodeDescription.Error_2013
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_CODE_INVALID_FORMAT))
                        .addConstraintViolation();

                extendedInfoValid = false;
            } else if (info.getKey().equals("Active")
                    && !info.getValue().equals("true")
                    && !info.getValue().equals("false")) {

                context.buildConstraintViolationWithTemplate(
                        ErrorCodes.CodeDescription.Error_2014
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_ACTIVE_INVALID_VALUE))
                        .addConstraintViolation();

                extendedInfoValid = false;
            } else if (info.getKey().equals("Confidential")
                    && !info.getValue().equals("true")
                    && !info.getValue().equals("false")) {

                context.buildConstraintViolationWithTemplate(
                        ErrorCodes.CodeDescription.Error_2015
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_CONFIDENTIAL_INVALID_VALUE))
                        .addConstraintViolation();

                extendedInfoValid = false;
            } else if (info.getKey().equals("ReferenceTeamCode")
                    && team.getExtendedInfo().stream()
                    .map(ExtendedInfo::getValue)
                    .collect(Collectors.toList())
                    .contains("REGULATED")
                    && (!teamRepository.checkDBValidOPPExists(info.getValue())
                    || !teamRepository.checkDBOPPActive(info.getValue()))
            ) {
                context.buildConstraintViolationWithTemplate(
                        String.format(ErrorCodes.CodeDescription.Error_1008, info.getValue())
                )
                        .addPropertyNode(String.valueOf(ErrorCodes.Codes.CMP_TEAM_EXTENDED_INFO_OPP_ID_NOT_VALID))
                        .addConstraintViolation();
                extendedInfoValid = false;
            }
        }

        if (team.getExtendedInfo().stream()
                .filter(info -> !Util.isAnyNullOrEmpty(info.getKey()))
                .filter(info -> info.getKey().equals("ReferenceTeamType"))
                .count() != 1) {
            context.buildConstraintViolationWithTemplate(
                    ErrorCodes.CodeDescription.Error_2006
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_SNG_REF_GRP_TYPE))
                    .addConstraintViolation();

            extendedInfoValid = false;
        } else {
            extendedInfoRefTeamType = team.getExtendedInfo()
                    .stream()
                    .filter(info -> !Util.isAnyNullOrEmpty(info.getKey()))
                    .filter(extendedInfo -> extendedInfo.getKey().equals("ReferenceTeamType"))
                    .map(ExtendedInfo::getValue)
                    .collect(Collectors.toList()).get(0);

            for (ExtendedInfo info: team.getExtendedInfo()
                 ) {
                if (info.getKey() != null && info.getKey().equals("ReferenceTeamCode")) {
                    if (!teamRepository.checkDBValidTeam(info.getValue(), extendedInfoRefTeamType)) {
                        context.buildConstraintViolationWithTemplate(
                                String.format(ErrorCodes.CodeDescription.Error_2012, info.getValue())
                        )
                                .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_REF_TYPE_INVALID_FORMAT))
                                .addConstraintViolation();

                        extendedInfoValid = false;
                    }
                }
            }

        }


        if (!Constants.allowedExtendedInfoKeys.containsAll(team.getExtendedInfo().stream()
            .map(ExtendedInfo::getKey).collect(Collectors.toList()))) {
            context.buildConstraintViolationWithTemplate(
                    ErrorCodes.CodeDescription.Error_2011
            )
                    .addPropertyNode(String.valueOf(ErrorCodes.Codes.COR_TEAM_EXTENDED_INFO_PAIRS_ONLY))
                    .addConstraintViolation();

            extendedInfoValid = false;

        }


        return extendedInfoValid;
    }

}

package my.approach.team.service;

import my.approach.team.teaming.auth.annotation.*;
import my.approach.team.auth.annotation.*;
import my.approach.team.model.auth.Role;
import my.approach.team.teaming.model.domain.team.*;
import my.approach.team.model.domain.entities.*;
import my.approach.team.persistence.repositories.TeamHistoryRepository;
import my.approach.team.persistence.repositories.TeamRepository;
import my.approach.team.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final TeamHistoryRepository teamHistoryRepository;

    @Override
    @Transactional
    public Team createTeam(Team team) {
        adjustCreateParameters(team);
        return teamRepository.save(team);
    }

    private void adjustCreateParameters(Team team) {
        team.setTeamId(teamRepository.retrieveDBUniqueTeamId());
        //Include master team reference in member object
        for (TeamMember member: team.getMembers()
        ) { member.setTeamId(team);

        }
        //Include master team reference in member object
        for (ExtendedInfo extendedInfo: team.getExtendedInfo()
        ) { extendedInfo.setExtendedInfoTeamId(team);

        }
        team.setTeamReferenceValues
                (String.join(",", team.getTeamReferenceIDs()));

        team.setCreatedDate(LocalDateTime.now());
        team.setUpdateDate(LocalDateTime.now());

        //CalculateHashOfMembers
        team.setCollectionOfMembersHash(Util.hashValues(
                team.getMembers().stream()
                        .map(TeamMember::getMemberId)
                        .collect((Collectors.joining(";")))));
    }

    @Override
    @Transactional
    public Team updateTeam(Team team) {
        LocalDateTime createdTime = findTeam(team.getTeamId()).getCreatedDate();
        adjustUpdateParameters(team);
        Team savedTeam = teamRepository.save(team);
        savedTeam.setCreatedDate(createdTime);
        return savedTeam;
    }

    private void adjustUpdateParameters(Team team) {
        //Users are agnostic of DB IDs, therefore retrieving it
        Optional<Team> id = teamRepository.findFirstByTeamId(team.getTeamId());
        id.ifPresent(value -> team.setId(value.getId()));

        //Convert teamReferenceValues in a comma separated style (as in DB)
        team.setTeamReferenceValues
                (String.join(",", team.getTeamReferenceIDs()));

        //Set team reference to the extendedInfo objects
        for (ExtendedInfo info: team.getExtendedInfo()
        ) {
            info.setExtendedInfoTeamId(team);
        }
        //Set team reference for the associated members
        for (TeamMember member: team.getMembers()
        ) {
            member.setTeamId(team);
        }
        //Update the modified date
        team.setUpdateDate(LocalDateTime.now());

        //CalculateHashOfMembers
        team.setCollectionOfMembersHash (Util.hashValues(
                team.getMembers().stream()
                        .map(TeamMember::getMemberId)
                        .collect((Collectors.joining(";")))));
    }


    @Override
    public boolean teamExists(String teamId) {
        return teamRepository.countByTeamId(teamId) > 0;
    }

    @Override
    @RequiresGMTTeamUpdatePermission
    public void canUpdateGMTTeam() {

    }

    @Override
    @RequiresGGTTeamCreatePermission
    public void canUpdateGGTTeam() {
    }

    @Override
    @RequiresRegulatedTeamUpdatePermission
    public void canUpdateRegulatedTeam() {

    }

    @Override
    @RequiresGMTTeamCreatePermission
    public void canCreateGMTTeam() {

    }

    @Override
    @RequiresGGTTeamCreatePermission
    public void canCreateGGTTeam() {

    }

    @Override
    @RequiresRegulatedTeamCreatePermission
    public void canCreateRegulatedTeam() {

    }

    @Override
    public void createHistoryActionEntry
            (String teamId, String outcome, String teamType, String action, String givenArguments, String result) {

        String userRole = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            List<GrantedAuthority> list = new ArrayList<>(SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getAuthorities());
            Role role;

            if (list.size() > 0) {
                role = (Role) list.get(0);
                userRole = role.getName();
            }
        }

        TeamHistory historyObject = new TeamHistory();

        try {
            historyObject.setRoleName(userRole);
            historyObject.setTeamType(teamType);
            historyObject.setActionType(action);
            historyObject.setActionParams(givenArguments);
            historyObject.setTeamId(teamId);

            if (result.length() < 2000) {
                historyObject.setActionResult(result);
            } else {
                historyObject.setActionResult(result.substring(0, 2000));
            }

            historyObject.setActionResponse(outcome);


            teamHistoryRepository.save(historyObject);
        } catch (Exception e) {
            //Just log the error (Team save progress should not be overwritten by this error)
            //Non-transaction handle it here
            log.error(e.getMessage() + " , Failed saving history object for Arguments: " + givenArguments);
        }
    }

    @Override
    public Page<TeamHistory> findHistoryActionEntries(TeamHistoryFilter teamHistoryFilter, Pageable pageable) {

        Specification<TeamHistory> spec = (Specification<TeamHistory>) (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!Util.isAnyNullOrEmpty(teamHistoryFilter.getTeamId())) {
                predicates.add(cb.equal(root.get(TeamHistory_.TEAM_ID), teamHistoryFilter.getTeamId()));
            }
            if (!Util.isAnyNullOrEmpty(teamHistoryFilter.getAction())) {
                predicates.add(cb.equal(root.get(TeamHistory_.ACTION_TYPE), teamHistoryFilter.getAction()));
            }
            if (!Util.isAnyNullOrEmpty(teamHistoryFilter.getOrigin())) {
                predicates.add(cb.equal(root.get(TeamHistory_.TEAM_TYPE), teamHistoryFilter.getOrigin()));
            }
            if (!Util.isAnyNullOrEmpty(teamHistoryFilter.getUser())) {
                predicates.add(cb.equal(root.get(TeamHistory_.USER_NAME), teamHistoryFilter.getUser()));
            }
            if (teamHistoryFilter.getDate() != null) {
                predicates.add(cb.between(root.get(TeamHistory_.CREATED_DATE),
                        teamHistoryFilter.calculateDateFrom(), teamHistoryFilter.calculateDateTo()));
            }
            query.orderBy(cb.desc(root.get(TeamHistory_.createdDate)));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return teamHistoryRepository.findAll(spec, pageable);
    }

    @Override
    public Team findTeam(String teamId) {
        Team team = teamRepository.findFirstByTeamId(teamId).get();
        List<String> teams = team.getTeamReferenceIDs();
        return team;
    }

    @Override
    public Page<Team> findTeams(TeamFilter filter, Pageable pageable) {
        return retrieveFilteredTeams(filter, pageable);
    }

    public Page<Team> retrieveFilteredTeams(TeamFilter teamFilter, Pageable pageable) {


        Specification<Team> spec = (Specification<Team>) (root, query, cb) -> {

            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            List<String> teamReferences = new ArrayList<>();
            Set<String> memberIds = new LinkedHashSet<>();

            if (!Util.isAnyNullOrEmpty(teamFilter.getTeamReferenceIDs())) {
                teamReferences = Arrays.asList(teamFilter.getTeamReferenceIDs().split(","));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getMemberId())) {
                memberIds = Stream.of(teamFilter.getMemberId().split(",")).collect(Collectors.toSet());
            }


            //Criteria builder
            Subquery<Team> memberQuery = query.subquery(Team.class);
            Root<TeamMember> teamMembers = memberQuery.from(TeamMember.class);

            for (String memberId : memberIds
            ) {
                memberQuery.where(cb.equal(teamMembers.get(TeamMember_.memberId), memberId));
                memberQuery.select(teamMembers.get(TeamMember_.teamId));
                predicates.add(root.get(Team_.teamId).in(memberQuery));
            }

            for (String reference: teamReferences
            ) {
                predicates.add(cb.like(root.get(Team_.teamReferenceValues), "%"+reference+"%"));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getTeamId())) {
                predicates.add(cb.equal(root.get(Team_.teamId), teamFilter.getTeamId()));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getTeamCode())) {
                predicates.add(cb.equal(root.get(Team_.teamCode), teamFilter.getTeamCode()));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getTeamName())) {
                predicates.add(cb.equal(root.get(Team_.teamName), teamFilter.getTeamName()));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getTeamType())) {
                predicates.add(cb.equal(root.get(Team_.teamTypeId), teamFilter.getTeamType()));
            }
            if (!Util.isAnyNullOrEmpty(teamFilter.getCollectionOfMembersHash())) {
                predicates.add(cb.equal(root.get(Team_.collectionOfMembersHash), teamFilter.getCollectionOfMembersHash()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };


        return teamRepository.findAll(spec, pageable);

    }


    @Override
    public Page<Team> fetchEntitiesPage(Pageable pageable, Specification<Team> specification) {
        return null;
    }

}

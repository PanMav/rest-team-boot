package my.approach.team.persistence.query;


import my.approach.team.model.domain.entities.Team;
import my.approach.team.model.domain.entities.TeamMember;
import my.approach.team.model.domain.entities.TeamMember_;
import my.approach.team.model.domain.entities.Team_;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TeamSpecifications {
    public static Specification<Team> teamIdIn(String teamId) {
        return (root, query, cb) -> cb.equal(root.get(Team_.teamId), teamId);
    }

    public static Specification<Team> teamCodeIn(String teamCode) {
        return (root, query, cb) -> cb.equal(root.get(Team_.teamCode), teamCode);
    }

    public static Specification<Team> teamNameIn(String teamName) {
        return (root, query, cb) -> cb.equal(root.get(Team_.teamName), teamName);
    }

    public static Specification<Team> teamReferenceLikeIn(String teamReference) {
        return (root, query, cb) -> cb.like(root.get(Team_.teamReferenceValues), teamReference);
    }

    public static Specification<Team> teamMembersHashIn(String collectionOfMembersHash) {
        return (root, query, cb) -> cb.like(root.get(Team_.collectionOfMembersHash), collectionOfMembersHash);
    }

    public static Specification<Team> memberIn(final String member) {

        return (root, query, cb) -> {
            Join<Team, TeamMember> members = root.join(Team_.MEMBERS);
            return cb.equal(members.get(TeamMember_.memberId), member);
        };
    }

}


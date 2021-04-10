package my.approach.team.persistence.repositories;

import my.approach.team.model.domain.entities.TeamMemberType;

import javax.validation.constraints.NotNull;

public interface TeamMemberTypeRepository extends BaseRepository<TeamMemberType, Long> {
    TeamMemberType findByMemberTeamType(@NotNull String memberType);
    TeamMemberType findById(int id);
    Long countById(@NotNull Long id);
}

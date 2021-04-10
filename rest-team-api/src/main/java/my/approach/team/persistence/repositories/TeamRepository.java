package my.approach.team.persistence.repositories;

import my.approach.team.model.domain.entities.Team;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface TeamRepository extends BaseRepository<Team, Long> {
    Optional<Team> findFirstByTeamId(@NonNull String teamId);
    long countByTeamId(@NotNull String teamId);
    long countByTeamNameAndTeamTypeIdAndTeamIdNotLike(@NotNull String teamName, int teamTypeId, String teamId);
    long countByTeamCodeAndTeamTypeIdAndTeamIdNotLike(@NotNull String teamCode, int teamTypeId, String teamId);

}

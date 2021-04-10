package my.approach.team.persistence.repositories;

import my.approach.team.model.domain.entities.TeamType;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;

public interface TeamTypeRepository extends BaseRepository<TeamType, Long> {
    long countByBusinessProcCode(@NonNull String procCode);
    TeamType findIdByBusinessProcCode(@NotNull String businessProcCode);
    TeamType findById(int id);
    Long countById(@NotNull Long id);
}

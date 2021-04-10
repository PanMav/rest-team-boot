package my.approach.team.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
   String retrieveDBUniqueTeamId();
   boolean checkDBValidOPPExists(String oppId);
   boolean checkDBOPPActive(String oppId);
   boolean checkDBValidTeam(String teamId, String teamType);


}

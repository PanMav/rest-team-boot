package my.approach.team.persistence.repositories;

import my.approach.team.model.auth.Role;
import my.approach.team.model.auth.Role_;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface RoleRepository extends BaseRepository<Role, Long> {
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {Role_.PERMISSIONS})
    List<Role> findAllByNameIn(List<String> names);
}

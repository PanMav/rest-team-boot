package my.approach.team.persistence.repositories;

import my.approach.team.model.auth.User;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

import static my.approach.team.util.Constants.Graphs;

public interface UserRepository extends BaseRepository<User, Long> {
    @EntityGraph(Graphs.User.DEFAULT_SINGLE)
    Optional<User> findByUsername(String username);
}

package my.approach.team.service;

import my.approach.team.error.IdmApiClientException;
import my.approach.team.error.NoMoftMembershipException;
import my.approach.team.model.auth.Role;
import my.approach.team.model.auth.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    Optional<User> fetchUnregisteredUserFromIdm(String username) throws IdmApiClientException, NoMoftMembershipException;
    List<Role> fetchRolesByNames(List<String> names);
}

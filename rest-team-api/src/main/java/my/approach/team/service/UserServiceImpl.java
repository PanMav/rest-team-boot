package my.approach.team.service;

import my.approach.team.error.IdmApiClientException;
import my.approach.team.error.NoMoftMembershipException;
import my.approach.team.error.UnauthorizedApiAccessException;
import my.approach.team.model.auth.Role;
import my.approach.team.model.auth.User;
import my.approach.team.model.dto.s2s.IdmUser;
import my.approach.team.persistence.repositories.RoleRepository;
import my.approach.team.persistence.repositories.UserRepository;
import my.approach.team.service.external.IdmWsService;
import my.approach.team.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Primary
public class UserServiceImpl implements UserService, AuditorAware<String> {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final IdmWsService idmService;
    private final boolean authEnabled;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           ObjectProvider<IdmWsService> idmService,
                           @Value("${idm.saml.enabled}") boolean authEnabled) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.idmService = idmService.getIfAvailable();
        this.authEnabled = authEnabled;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable(Constants.Cache.USERS)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseGet(() -> {
            Optional<User> user;
            try {
                user = fetchUnregisteredUserFromIdm(username);
            } catch (IdmApiClientException | NoMoftMembershipException exc) {
                log.error(exc.getLocalizedMessage(), exc);
                user = Optional.empty();
            }

            return user.orElseThrow(() -> {
                String message = String.format("User with username: \"%s\" not found", username);
                return new UsernameNotFoundException(message);
            });
        });
    }
    /**
     * Fetches a new (not registered in SIGMA) user from IDM.
     * If user roles apply to SIGMA then we replicate the user in our DB.
     *
     * @param username The username
     * @return The user - if applicable for SIGMA - null otherwise
     * @throws IdmApiClientException     If an error occurs with IDM API interaction, the {@link IdmApiClientException} from {@link IdmWsService} is propagated
     * @throws NoMoftMembershipException If received user has no MOFT membership
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<User> fetchUnregisteredUserFromIdm(String username) throws IdmApiClientException, NoMoftMembershipException {
        User user = null;
        IdmUser idmUser = idmService.fetchUser(username);

        List<Role> roles = fetchRolesByNames(idmUser.getMembership().getRoles());
        if (!roles.isEmpty()) {
            user = new User();
            user.setUsername(idmUser.getUsername());
            user.setEmail(idmUser.getEmail());
            user.setRoles(new HashSet<>(roles));
            user = userRepository.save(user);
        }

        return Optional.ofNullable(user);
    }

    @Override
    public List<Role> fetchRolesByNames(List<String> names) {
        return roleRepository.findAllByNameIn(names);
    }

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        if (authEnabled) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                return Optional.of(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            } else {
                // Should not happen
                // (SecurityContextHolder should be filled when user is authenticated against an IDM)
                throw new UnauthorizedApiAccessException("Unauthorized user attempted to access resources");
            }
        } else {
            return null;
        }
    }
}

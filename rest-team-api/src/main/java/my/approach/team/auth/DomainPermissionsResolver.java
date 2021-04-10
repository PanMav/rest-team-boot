package my.approach.team.auth;

import my.approach.team.model.auth.Permission;
import my.approach.team.model.auth.Role;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
@Primary
public class DomainPermissionsResolver implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if ((auth == null) || !(permission instanceof String)) {
            return false;
        }
        final Permission domainPermission = Permission.fromRepresentation(((String) permission));
        final List<Role> userRoles = ((List<Role>) auth.getAuthorities());
        final List<Permission> userPermissions = userRoles.stream().map(Role::getPermissions).flatMap(Collection::stream).collect(Collectors.toList());

        return userPermissions.stream().anyMatch(up -> up.equals(domainPermission));
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        throw new UnsupportedOperationException("Method `hasPermission(Authentication, Serializable, String, Object)` is not implemented");
    }
}

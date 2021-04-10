package my.approach.team.model.auth;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.error.InvalidPermissionRepresentation;
import my.approach.team.serialization.Views;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "THH_GRP_AUTH_PERMISSIONS")
@SequenceGenerator(name = "SEQ_THH_GRP_AUTH_PERMISSIONS", sequenceName = "SEQ_THH_GRP_AUTH_PERMISSIONS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_THH_GRP_AUTH_PERMISSIONS")
    @Column(name = "PERMISSION_ID")
    private Long id;

    @Column(name = "PERMISSION_RESOURCE")
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    @JsonView(Views.UserDefault.class)
    private DomainResource resource;

    @Column(name = "PERMISSION_OPERATION")
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    @JsonView(Views.UserDefault.class)
    private DomainOperation operation;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();

    public String toRepresentation() {
        return resource + ":" + operation;
    }

    public static Permission fromRepresentation(String representation) {
        return fromRepresentation(representation, Collections.emptyList());
    }

    public static Permission fromRepresentation(String representation, List<Role> permissionRoles) {
        if (!representation.matches("^\\w+:\\w+$")) {
            throw new InvalidPermissionRepresentation(String.format("Invalid permission representation: %s", representation));
        }
        Permission permission = new Permission();
        try {
            permission.setResource(DomainResource.valueOf(representation.split(":")[0]));
            permission.setOperation(DomainOperation.valueOf(representation.split(":")[1]));
        } catch (IllegalArgumentException | NullPointerException exc) {
            throw new InvalidPermissionRepresentation(String.format("Invalid resource or operation for permission: %s", representation));
        }
        permission.setRoles(new HashSet<>(permissionRoles));

        return permission;
    }

    @Override
    public String toString() {
        return "Permission: " + toRepresentation();
    }
}

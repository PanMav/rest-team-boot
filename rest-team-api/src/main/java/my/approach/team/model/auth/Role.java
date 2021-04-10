package my.approach.team.model.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "THH_GRP_AUTH_ROLES")
@SequenceGenerator(name = "SEQ_THH_GRP_AUTH_ROLES", sequenceName = "SEQ_THH_GRP_AUTH_ROLES", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_THH_GRP_AUTH_ROLES")
    @Column(name = "ROLE_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "ROLE_NAME")
    @EqualsAndHashCode.Include
    @JsonView(Views.UserDefault.class)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "THH_GRP_AUTH_ROLES_PERMISSIONS",
            joinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERMISSION_ID", referencedColumnName = "PERMISSION_ID")
    )
    @JsonView(Views.UserDefault.class)
    private Set<Permission> permissions = new HashSet<>();

    @Override
    @JsonIgnore
    public String getAuthority() {
        return getName();
    }
}

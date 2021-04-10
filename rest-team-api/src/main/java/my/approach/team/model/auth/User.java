package my.approach.team.model.auth;

import com.fasterxml.jackson.annotation.JsonView;
import my.approach.team.serialization.Views;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static my.approach.team.util.Constants.Graphs;

@Entity
@Table(name = "THH_GRP_AUTH_USERS")
@SequenceGenerator(name = "SEQ_THH_GRP_AUTH_USERS", sequenceName = "SEQ_THH_GRP_AUTH_USERS", allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(
        name = Graphs.User.DEFAULT_SINGLE,
        attributeNodes = @NamedAttributeNode(value = User_.ROLES, subgraph = Graphs.User.Subgraph.ROLES),
        subgraphs = @NamedSubgraph(
                name = Graphs.User.Subgraph.ROLES,
                attributeNodes = @NamedAttributeNode(Role_.PERMISSIONS)
        )
)
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_THH_GRP_AUTH_USERS")
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false)
    @JsonView(Views.UserDefault.class)
    private String username;

    @Column(name = "USER_FIRST_NAME")
    @JsonView(Views.UserDefault.class)
    private String firstName;

    @Column(name = "USER_LAST_NAME")
    @JsonView(Views.UserDefault.class)
    private String lastName;

    @Column(name = "USER_EMAIL")
    @JsonView(Views.UserDefault.class)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "THH_GRP_AUTH_USERS_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID")
    )
    @JsonView(Views.UserDefault.class)
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // @TODO :: May need to elaborate on this
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return username;
    }
}

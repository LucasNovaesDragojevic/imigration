package imigration.api.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@NoArgsConstructor
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Integer id;

    private String username;

    private String password;

    @ManyToMany
    private final Set<Authority> authorities = new HashSet<>();

    public User(final String username, final String password, final Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities.addAll(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}

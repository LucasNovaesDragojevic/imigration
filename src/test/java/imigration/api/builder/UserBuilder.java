package imigration.api.builder;

import java.util.Set;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Authority;
import imigration.api.model.entity.User;

@Component
public class UserBuilder {

    private User user;

    public UserBuilder user() {
        this.user = new User();
        return this;
    }

    public UserBuilder withUsername(final String username) {
        this.user.setUsername(username);
        return this;
    }

    public UserBuilder withPassword(final String password) {
        this.user.setPassword(password);
        return this;
    }

    public UserBuilder withAccountNonExpired(final Boolean accountNonExpired) {
        this.user.setAccountNonExpired(accountNonExpired);
        return this;
    }

    public UserBuilder withAccountNonLocked(final Boolean accountNonLocked) {
        this.user.setAccountNonLocked(accountNonLocked);
        return this;
    }

    public UserBuilder withCredentialsNonExpired(final Boolean credentialsNonExpired) {
        this.user.setCredentialsNonExpired(credentialsNonExpired);
        return this;
    }

    public UserBuilder withEnabled(final Boolean enabled) {
        this.user.setEnabled(enabled);
        return this;
    }

    public UserBuilder withAuthorities(final Set<Authority> authorities) {
        this.user.addAllAuthorities(authorities);
        return this;
    }

    public User build() {
        return this.user;
    }

    public User buildRootUser(final String password, final Set<Authority> authorities) {
        return this.user()
                    .withUsername("root@email.com")
                    .withPassword(password)
                    .withEnabled(true)
                    .withAuthorities(authorities)
                    .build();
    }

    public User buildRandomUser(final String password, final Set<Authority> authorities) {
        return this.user()
                    .withUsername("user" + System.currentTimeMillis() + "@email.com")
                    .withPassword(password)
                    .withEnabled(true)
                    .withAuthorities(authorities)
                    .build();
    }
}

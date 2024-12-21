package imigration.api.builder;

import org.springframework.stereotype.Component;

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

    public User build() {
        return this.user;
    }
}

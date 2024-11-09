package imigration.api.model;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AuthorityName name;

    @Override
    public String getAuthority() {
        return this.name.name();
    }

    public Authority(final AuthorityName name) {
        this.name = name;
    }
}

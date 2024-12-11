package imigration.api.model.entity;

import org.springframework.security.core.GrantedAuthority;

import imigration.api.model.enums.AuthorityName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode.Include;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Authority implements GrantedAuthority {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @Include
    private AuthorityName name;

    @Override
    public String getAuthority() {
        return this.name.name();
    }

    public Authority(final AuthorityName name) {
        this.name = name;
    }
}

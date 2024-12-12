package imigration.api.model.entity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue
    private Integer id;

    @Getter
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @Getter
    @OneToOne(optional = false)
    private User owner;
    
    @Getter
    @Column(nullable = false)
    private String token = UUID.randomUUID().toString();

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean validated = false;

    public PasswordResetToken(final User owner) {
        this.owner = owner;
    }
}

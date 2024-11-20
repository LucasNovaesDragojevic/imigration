package imigration.api.model.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Attachment {

    @Getter
    @Id
    @GeneratedValue
    private Integer id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @Getter
    @Setter
    @Column(nullable = false)
    private String content;

    @Getter
    @Setter
    @ManyToOne(optional = false)
    private Comment comment;

    public Attachment(final String content, final Comment comment) {
        this.content = content;
        this.comment = comment;
    }
}

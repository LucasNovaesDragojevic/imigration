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
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    
    @Getter
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
    @Setter
    @ManyToOne(optional = false)
    private User owner;

    @Getter
    @Setter
    @Column(nullable = false)
    private String content;

    @Getter
    @Setter
    @ManyToOne(optional = false)
    private Process process;
}

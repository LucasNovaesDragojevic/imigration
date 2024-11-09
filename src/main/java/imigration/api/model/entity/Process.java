package imigration.api.model.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import imigration.api.model.enums.Country;
import imigration.api.model.enums.Step;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Process {

    @Getter
    @Id
    @GeneratedValue
    private Integer id;

    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Step step;

    @Getter
    @Setter
    @Column(nullable = false)
    private Country nationality;

    @Getter
    @Setter
    @Column(nullable = false)
    private LocalDate dateBirth;

    @Getter
    @Setter
    @Column(nullable = false)
    private String passport;

    @Getter
    @Setter
    private String govId;

    @Getter
    @Setter
    private String driverLicense;
    
    @Getter
    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    public void addComment(final Comment comment) {
        this.comments.add(comment);
    }
}

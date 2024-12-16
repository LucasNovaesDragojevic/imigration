package imigration.api.model.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import imigration.api.model.entity.User;

public record UserResponse(
    Integer id,
    String username, 
    LocalDateTime createdDate,
    Boolean isEnabled,
    List<String> authorities
) {
    public UserResponse(final User user) {
        this(
            user.getId(),
            user.getUsername(),
            LocalDateTime.ofInstant(user.getCreatedDate(), ZoneId.systemDefault()), 
            user.isEnabled(),
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}

package imigration.api.model.response;

import imigration.api.model.entity.User;

public record UserMinimalResponse(Integer id, String username) {

    public UserMinimalResponse(final User user) {
        this(user.getId(), user.getUsername());
    }
}

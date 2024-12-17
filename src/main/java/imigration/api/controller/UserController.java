package imigration.api.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.model.entity.Authority;
import imigration.api.model.response.UserMinimalResponse;
import imigration.api.model.response.UserResponse;
import imigration.api.model.update.UserUpdate;
import imigration.api.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(
        final UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping(Url.USERS)
    Page<UserMinimalResponse> realAll(
        @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = DESC) final Pageable pageable
    ) {
        return userService.findAll(pageable).map(UserMinimalResponse::new);
    }

    @GetMapping(Url.USER)
    UserResponse read(
        @PathVariable(Url.ID) final Integer id
    ) {
        return userService.findById(id).map(UserResponse::new).get();
    }

    @PatchMapping(Url.USER)
    UserResponse update(
        @PathVariable(Url.ID) final Integer id,
        @RequestBody final UserUpdate userUpdate
    ) {
        return userService
                .findById(id)
                .map(u -> {
                    final var authoritiesToUpdate = userUpdate.authorities();
                    final var permitedAuthoritiesToUpdate = 
                        SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .filter(a -> authoritiesToUpdate.contains(a.getAuthority()))
                        .map(Authority.class::cast)
                        .collect(Collectors.toSet());
                    return new UserResponse(userService.update(userUpdate, permitedAuthoritiesToUpdate, u));                
                })
                .get();
    }
}

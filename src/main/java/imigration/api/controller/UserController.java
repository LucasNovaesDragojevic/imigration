package imigration.api.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.model.enums.AuthorityName;
import imigration.api.model.response.UserMinimalResponse;
import imigration.api.model.response.UserResponse;
import imigration.api.model.update.UserUpdate;
import imigration.api.service.AuthorityService;
import imigration.api.service.UserService;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthorityService authorityService;

    public UserController(
        final UserService userService,
        final AuthorityService authorityService
    ) {
        this.userService = userService;
        this.authorityService = authorityService;
    }

    @GetMapping(Url.USERS)
    Page<UserMinimalResponse> realAll(
        @PageableDefault(page = 0, size = 10, sort = "createdDate", direction = DESC) final Pageable pageable
    ) {
        return userService.findAll(pageable).map(UserMinimalResponse::new);
    }

    @GetMapping(Url.USER)
    UserResponse read(@PathVariable(Url.ID) final Integer id) {
        return userService.findById(id).map(UserResponse::new).get();
    }

    @PatchMapping(Url.USER)
    UserResponse update(
        @PathVariable(Url.ID) final Integer id,
        @RequestBody final UserUpdate userUpdate
    ) {
        //TODO User cannot add roles that do not have
        return userService
                .findById(id)
                .map(user -> new UserResponse(
                    userService.update(
                        userUpdate, 
                        authorityService.findByNameIn(AuthorityName.findByNameIn(userUpdate.authorities())), 
                        user)
                ))
                .get();
    }
}

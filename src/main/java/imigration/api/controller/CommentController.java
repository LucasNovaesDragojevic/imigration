package imigration.api.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.Comment;
import imigration.api.model.enums.AuthorityName;
import imigration.api.model.request.CommentRequest;
import imigration.api.model.response.CommentResponse;
import imigration.api.service.AttachmentService;
import imigration.api.service.CommentService;
import imigration.api.service.ProcessService;
import imigration.api.service.UserService;
import jakarta.validation.Valid;

@RestController
public class CommentController {

    private final ProcessService processService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    public CommentController(final ProcessService processService,
                             final CommentService commentService,
                             final AttachmentService attachmentService,
                             final UserService userService
    ) {
        this.processService = processService;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.userService = userService;
    }

    @PostMapping(Url.COMMENTS_BY_PROCESS)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
        @PathVariable(Url.ID) final Integer processId,
        @RequestParam(name = "owner", required = false) final Integer ownerId,
        @RequestBody @Valid final CommentRequest commentRequest
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        Comment comment;
        if (ownerId != null && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
            comment = commentService.create(processService.findByIdAndOwnerId(processId, ownerId), user, commentRequest);
        else
            comment = commentService.create(processService.findByIdAndOwnerId(processId, user.getId()), user, commentRequest);
        if (Objects.isNull(commentRequest.attachments()))
            return new CommentResponse(comment);
        final var attachments = attachmentService.create(user, comment, commentRequest.attachments());
        return new CommentResponse(comment, attachments);
    }
    
    @GetMapping(Url.COMMENTS_BY_PROCESS)
    public Page<CommentResponse> findAllByProcessIdAndOwnerId(
        @PathVariable(Url.ID) final Integer processId,
        @RequestParam(name = "owner", required = false) final Integer ownerId,
        @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = DESC) final Pageable pageable
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (ownerId != null && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
            return processService.findAllByProcessIdAndProcessOwnerId(processId, ownerId, pageable);
        return processService.findAllByProcessIdAndProcessOwnerId(processId, user.getId(), pageable);
    }
}
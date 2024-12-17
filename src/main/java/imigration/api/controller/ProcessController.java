package imigration.api.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import imigration.api.constant.Url;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.Comment;
import imigration.api.model.enums.AuthorityName;
import imigration.api.model.request.CommentRequest;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.CommentResponse;
import imigration.api.model.response.ProcessMinimalResponse;
import imigration.api.model.response.ProcessResponse;
import imigration.api.model.update.ProcessUpdate;
import imigration.api.service.AttachmentService;
import imigration.api.service.CommentService;
import imigration.api.service.ProcessService;
import imigration.api.service.UserService;
import jakarta.validation.Valid;

@RestController
public class ProcessController {

    private final ProcessService processService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    public ProcessController(final ProcessService processService,
                             final CommentService commentService,
                             final AttachmentService attachmentService,
                             final UserService userService
    ) {
        this.processService = processService;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.userService = userService;
    }

    @PostMapping(Url.PROCESSES)
    public ProcessResponse create(
        @RequestBody @Valid final ProcessRequest processRequest
    ) {
        final var owner = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        final var process = processService.create(processRequest, owner);
        final var comment = commentService.create(process, owner, processRequest.comment());
        attachmentService.create(owner, comment, processRequest.comment().attachments());
        return new ProcessResponse(process, comment);
    }

    @GetMapping(Url.PROCESSES)
    public Page<ProcessMinimalResponse> findAllByOwner(
            @RequestParam(name = "owner", required = false) final Integer ownerId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = DESC) final Pageable pageable
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (ownerId != null 
            && !user.getId().equals(ownerId)
            && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
        {
            return processService.findAllByOwner(pageable, userService.findById(ownerId).get());
        }
        return processService.findAllByOwner(pageable, user);
    }

    @GetMapping(Url.PROCESS)
    public ProcessResponse findByIdAndOwner(
        @PathVariable(Url.ID) final Integer id,
        @RequestParam(name = "owner", required = false) final Integer ownerId
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        if (ownerId != null 
            && !user.getId().equals(ownerId)
            && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
        {
            return processService.findByIdAndOwner(id, userService.findById(ownerId).get());
        }
        return processService.findByIdAndOwner(id, user);
    }

    @PatchMapping(Url.PROCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateProcess(
        @PathVariable(Url.ID) final Integer processId,
        @RequestBody @Valid final ProcessUpdate processUpdate
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        if (user.getId().equals(processUpdate.owner()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot review yourself process.");

        if (user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
            processService.updateProcess(processId, processUpdate);
    }

    @PostMapping(Url.COMMENTS_BY_PROCESS)
    public CommentResponse createComment(
        @PathVariable(Url.ID) final Integer processId,
        @RequestParam(name = "owner", required = false) final Integer ownerId,
        @RequestBody @Valid final CommentRequest commentRequest
    ) {
        final var user = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        Comment comment;
        if (ownerId != null 
            && !user.getId().equals(ownerId)
            && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
        {
            comment = commentService.create(processService.findByIdAndOwnerId(processId, ownerId).get(), user, commentRequest);
        }
        else 
        {
            comment = commentService.create(processService.findByIdAndOwnerId(processId, user.getId()).get(), user, commentRequest);
        }
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
        if (ownerId != null 
            && !user.getId().equals(ownerId)
            && user.getAuthorities().contains(new Authority(AuthorityName.PROCESS_REVIEWER)))
        {
            return processService.findAllByProcessIdAndProcessOwnerId(processId, ownerId, pageable);
        }
        return processService.findAllByProcessIdAndProcessOwnerId(processId, user.getId(), pageable);
    }
}

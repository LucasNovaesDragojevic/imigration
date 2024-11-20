package imigration.api.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.model.request.CommentRequest;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.CommentResponse;
import imigration.api.model.response.ProcessMinimalResponse;
import imigration.api.model.response.ProcessResponse;
import imigration.api.service.AttachmentService;
import imigration.api.service.CommentService;
import imigration.api.service.ProcessService;
import imigration.api.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("processes")
public class ProcessController {

    private final ProcessService processService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;
    private final UserService userService;

    public ProcessController(final ProcessService processService,
                             final CommentService commentService,
                             final AttachmentService attachmentService,
                             final UserService userService) {
        this.processService = processService;
        this.commentService = commentService;
        this.attachmentService = attachmentService;
        this.userService = userService;
    }

    @PostMapping
    public ProcessResponse create(@RequestBody @Valid final ProcessRequest processRequest) {
        final var owner = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        final var process = processService.create(processRequest, owner);
        final var comment = commentService.create(process, owner, processRequest.comment());
        attachmentService.create(owner, comment, processRequest.comment().attachments());
        return new ProcessResponse(process, comment);
    }

    @GetMapping
    public Page<ProcessMinimalResponse> findAllByOwner(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = DESC) final Pageable pageable) {
        return processService.findAllByOwner(pageable, userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @GetMapping("{id}")
    public ProcessResponse findByIdAndOwner(@PathVariable("id") final Integer id) {
        return processService.findByIdAndOwner(id, userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @PostMapping("{id}/comments")
    public CommentResponse createComment(@PathVariable("id") final Integer id, @RequestBody @Valid final CommentRequest commentRequest) {
        final var owner = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        final var comment = commentService.create(processService.findById(id), owner, commentRequest);
        final var attachments = attachmentService.create(owner, comment, commentRequest.attachments());
        return new CommentResponse(comment, attachments);
    }
    
    @GetMapping("{id}/comments")
    public Page<CommentResponse> findAllProcessId(@PathVariable("id") final Integer id, @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = DESC) final Pageable pageable) {
        return processService.findAllByProcessId(id, pageable);
    }
}

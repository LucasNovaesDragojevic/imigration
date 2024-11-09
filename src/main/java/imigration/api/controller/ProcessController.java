package imigration.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.ProcessResponse;
import imigration.api.service.ProcessService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("processes")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(final ProcessService processService) {
        this.processService = processService;        
    }

    @PostMapping
    public ProcessResponse post(@RequestBody @Valid final ProcessRequest processRequest) {
        return processService.post(processRequest);
    }
    
}

package org.hesperides.presentation.controllers;

import org.hesperides.application.workshopproperties.WorkshopPropertyUseCases;
import org.hesperides.domain.security.User;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.hesperides.presentation.io.WorkshopPropertyInput;
import org.hesperides.presentation.io.WorkshopPropertyOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/workshop/properties")
@RestController
public class WorkshopPropertiesController extends AbstractController {

    private final WorkshopPropertyUseCases workshopPropertyUseCases;

    @Autowired
    public WorkshopPropertiesController(WorkshopPropertyUseCases workshopPropertyUseCases) {
        this.workshopPropertyUseCases = workshopPropertyUseCases;
    }

    @PostMapping
    public ResponseEntity<WorkshopPropertyOutput> createWorkshopProperty(Authentication authentication,
                                                                         @Valid @RequestBody final WorkshopPropertyInput workshopPropertyInput) {

        // input to domain object
        WorkshopProperty workshopProperty = workshopPropertyInput.toDomainInstance();

        // run the command through use case and return the key
        String key = workshopPropertyUseCases.createWorkshopProperty(workshopProperty, User.fromAuthentication(authentication));

        // then create the view with the given key
        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopPropertyView(key);

        // create the output result
        WorkshopPropertyOutput result = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{key}")
    public ResponseEntity<WorkshopPropertyOutput> getWorkshopProperty(@PathVariable("key") final String workshopPropertyKey) {
        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopPropertyView(workshopPropertyKey);
        WorkshopPropertyOutput result = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);

        return ResponseEntity.ok(result);
    }

    @PutMapping
    public ResponseEntity<WorkshopPropertyOutput> updateWorkshopProperty(Authentication authentication,
                                                                         @Valid @RequestBody final WorkshopPropertyInput workshopPropertyInput) {
        WorkshopProperty workshopProperty = workshopPropertyInput.toDomainInstance();

        workshopPropertyUseCases.updateWorkshopProperty(workshopProperty, User.fromAuthentication(authentication));

        WorkshopPropertyView workshopPropertyView = workshopPropertyUseCases.getWorkshopPropertyView(workshopProperty.getKey());

        // create the output result
        WorkshopPropertyOutput result = WorkshopPropertyOutput.fromWorkshopPropertyView(workshopPropertyView);

        return ResponseEntity.ok(result);
    }
}

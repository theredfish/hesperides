package org.hesperides.application.workshopproperties;

import org.hesperides.domain.security.User;
import org.hesperides.domain.workshopproperties.commands.WorkshopPropertyCommands;
import org.hesperides.domain.workshopproperties.entities.WorkshopProperty;
import org.hesperides.domain.workshopproperties.exceptions.DuplicateWorkshopPropertyException;
import org.hesperides.domain.workshopproperties.exceptions.WorkshopPropertyNotFoundException;
import org.hesperides.domain.workshopproperties.queries.WorkshopPropertyQueries;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkshopPropertyUseCases {

    private final WorkshopPropertyCommands commands;
    private final WorkshopPropertyQueries queries;

    @Autowired
    public WorkshopPropertyUseCases(WorkshopPropertyCommands commands, WorkshopPropertyQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    /**
     * Create new workshop property command if doesn't exists
     *
     * @param workshopProperty the workshop property
     * @param user the user???
     *
     * @return a dispastched command with a workshop property entity (and a user entity??)
     */
    public String createWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        String workshopKey = workshopProperty.getKey();

        if(queries.workshopPropertyExists(workshopKey)) {
            throw new DuplicateWorkshopPropertyException(workshopKey);
        }

        return commands.createWorkshopProperty(workshopProperty, user);
    }

    /**
     * Get the projection view for the given workshop propertie's key
     *
     * @param workshopPropertyKey the very good key
     * @return a view
     */
    public WorkshopPropertyView getWorkshopPropertyView(String workshopPropertyKey) {
        Optional<WorkshopPropertyView> workshopPropertyView = queries.getOptionalWorkshopProperty(workshopPropertyKey);

        if (!workshopPropertyView.isPresent()) {
            throw new WorkshopPropertyNotFoundException(workshopPropertyKey);
        }

        return workshopPropertyView.get();
    }

    public void updateWorkshopProperty(WorkshopProperty workshopProperty, User user) {
        String workshopKey = workshopProperty.getKey();

        if(!queries.workshopPropertyExists(workshopKey)) {
            throw new WorkshopPropertyNotFoundException(workshopKey);
        }

        commands.updateWorkshopProperty(workshopProperty, user);
    }
}

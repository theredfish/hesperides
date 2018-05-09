package org.hesperides.domain.technos.commands;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.hesperides.domain.technos.TemplateCreatedEvent;
import org.hesperides.domain.technos.TemplateDeletedEvent;
import org.hesperides.domain.technos.TemplateUpdatedEvent;

public interface TechnoTemplateCommandsRepository {

    @EventSourcingHandler
    void on(TemplateCreatedEvent event);

    @EventSourcingHandler
    void on(TemplateUpdatedEvent event);

    @EventSourcingHandler
    void on(TemplateDeletedEvent event);

}

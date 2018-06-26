package org.hesperides.domain.workshopproperties;

import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;

import java.util.Optional;

public interface WorkshopPropertyProjectionRepository {

    /*** EVENT HANDLERS ***/
    @EventSourcingHandler
    void onCreatedEvent(WorkshopPropertyCreatedEvent event);

    @EventSourcingHandler
    void onUpdatedEvent(WorkshopPropertyUpdatedEvent event);

    /*** QUERY HANDLERS ***/
    @QueryHandler
    Optional<WorkshopPropertyView> query(GetWorkshopPropertyByKeyQuery query);
}

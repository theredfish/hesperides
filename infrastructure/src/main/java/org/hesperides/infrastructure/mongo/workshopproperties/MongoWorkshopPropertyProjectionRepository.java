package org.hesperides.infrastructure.mongo.workshopproperties;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.workshopproperties.GetWorkshopPropertyByKeyQuery;
import org.hesperides.domain.workshopproperties.WorkshopPropertyCreatedEvent;
import org.hesperides.domain.workshopproperties.WorkshopPropertyProjectionRepository;
import org.hesperides.domain.workshopproperties.WorkshopPropertyUpdatedEvent;
import org.hesperides.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoWorkshopPropertyProjectionRepository implements WorkshopPropertyProjectionRepository {

    private final MongoWorkshopPropertyRepository workshopPropertyRepository;

    @Autowired
    public MongoWorkshopPropertyProjectionRepository(MongoWorkshopPropertyRepository workshopPropertyRepository) {
        this.workshopPropertyRepository = workshopPropertyRepository;
    }

    /*** EVENT HANDLERS ***/
    @Override
    @CommandHandler
    public void onCreatedEvent(WorkshopPropertyCreatedEvent event) {
        WorkshopPropertyDocument workshopPropertyDocument = WorkshopPropertyDocument.fromDomainInstance(event
                .getWorkshopProperty());

        workshopPropertyRepository.save(workshopPropertyDocument);
    }

    @Override
    @CommandHandler
    public void onUpdatedEvent(WorkshopPropertyUpdatedEvent event) {
        WorkshopPropertyDocument workshopPropertyDocument = WorkshopPropertyDocument.fromDomainInstance(event
                .getWorkshopProperty());

        workshopPropertyRepository.save(workshopPropertyDocument);
    }

    /**
     * QUERY HANDLERS
     **/
    @Override
    @QueryHandler
    public Optional<WorkshopPropertyView> query(GetWorkshopPropertyByKeyQuery query) {
        Optional<WorkshopPropertyDocument> workshopPropertyDocument = workshopPropertyRepository
                .findOptionalByKey(query.getPlatformKey());

        return workshopPropertyDocument.map(WorkshopPropertyDocument::toWorkshopPropertyView);
    }
}

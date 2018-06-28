package org.hesperides.infrastructure.mongo.platforms;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.GetApplicationByName;
import org.hesperides.domain.platforms.GetPlatformByKeyQuery;
import org.hesperides.domain.platforms.PlatformCreatedEvent;
import org.hesperides.domain.platforms.PlatformDeletedEvent;
import org.hesperides.domain.platforms.PlatformProjectionRepository;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void on(PlatformCreatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatform());
        platformRepository.save(platformDocument);
    }

    @EventHandler
    @Override
    public void on(PlatformDeletedEvent event) {
        platformRepository.deleteByKey(new PlatformKeyDocument(event.getPlatformKey()));
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query) {
        Optional<PlatformView> optionalPlatformView = Optional.empty();
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        Optional<PlatformDocument> optionalPlatformDocument = platformRepository.findOptionalByKey(platformKeyDocument);
        if (optionalPlatformDocument.isPresent()) {
            optionalPlatformView = Optional.of(optionalPlatformDocument.get().toPlatformView());
        }
        return optionalPlatformView;
    }

    @QueryHandler
    @Override
    public Optional<ApplicationView> onGetPlatformByApplicationNameQuery(GetApplicationByName query) {
        Optional<ApplicationView> optionalApplicationView = Optional.empty();

        List<PlatformDocument> platformDocuments = platformRepository.findAllByKeyApplicationName(query
                .getApplicationName());

        if (!CollectionUtils.isEmpty(platformDocuments)) {
            ApplicationView applicationView = new ApplicationView(query.getApplicationName(),
                    platformDocuments.stream()
                            .map(PlatformDocument::toPlatformView)
                            .collect(Collectors.toList()));
            optionalApplicationView = Optional.of(applicationView);
        }


        return optionalApplicationView;
    }
}

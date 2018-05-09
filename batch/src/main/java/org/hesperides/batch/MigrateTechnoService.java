package org.hesperides.batch;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.batch.redis.legacy.events.technos.LegacyTechnoDeletedEvent;
import org.hesperides.batch.redis.legacy.events.technos.LegacyTechnoTemplateCreatedEvent;
import org.hesperides.batch.redis.legacy.events.technos.LegacyTechnoTemplateDeletedEvent;
import org.hesperides.batch.redis.legacy.events.technos.LegacyTechnoTemplateUpdatedEvent;
import org.hesperides.domain.security.User;
import org.hesperides.domain.technos.TechnoCreatedEvent;
import org.hesperides.domain.technos.entities.Techno;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class MigrateTechnoService extends MigrateAbstractService {

    static {
        AGGREGATE_TYPE = "TechnoAggregate";
        PATTERN = "template";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent",LegacyTechnoTemplateCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent",LegacyTechnoTemplateUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent",LegacyTechnoDeletedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent",LegacyTechnoTemplateDeletedEvent.class);


    }

    @Override
    protected List<GenericDomainEventMessage<Object>> convertToDomainEvent(List<LegacyEvent> events) {
        List<GenericDomainEventMessage<Object>> domainEventMessages = new ArrayList<>();
        //Ce booléen force la création d'un nouvel évenement TechnoCreatedEvent, si l'évennement précédent celui traité est de type TemplatePackageDeletedEvent
        AtomicBoolean shouldCreateTechno = new AtomicBoolean(true);
        events.forEach(event -> {
            try {
                LegacyInterface legacyInterface = convertToLegacyEvent(event);
                String aggregateId = legacyInterface.getKeyString();
                Long timestamp = event.getTimestamp();
                Supplier<Instant> supplier = () -> Instant.ofEpochMilli(timestamp);
                User user = new User(event.getUser());
                if (shouldCreateTechno.get()) {
                    domainEventMessages.add(new GenericDomainEventMessage<>(
                            AGGREGATE_TYPE,
                            aggregateId,
                            domainEventMessages.size(),
                            new GenericEventMessage<>(new TechnoCreatedEvent(new Techno(legacyInterface.getKey(), null), user)),
                            supplier));
                    shouldCreateTechno.set(false);
                }

                GenericEventMessage<Object> eventMessage = new GenericEventMessage<>(legacyInterface.toDomainEvent(user), MetaData.emptyInstance());
                if ("com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent".equals(event.getEventType())){
                    shouldCreateTechno.set(true);
                }
                domainEventMessages.add(new GenericDomainEventMessage<>(AGGREGATE_TYPE, aggregateId, domainEventMessages.size(), eventMessage, supplier));
            }
        catch (Exception ignored){}
        });

        return domainEventMessages;
    }
}

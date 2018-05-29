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
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TechnoIO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class TechnoMigrationService extends AbstractMigrationService {

    static {
        AGGREGATE_TYPE = "TechnoAggregate";
        PATTERN = "template";
        CONVERTED_SET = "a-"+PATTERN+"-set";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent",LegacyTechnoTemplateCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent",LegacyTechnoTemplateUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent",LegacyTechnoDeletedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent",LegacyTechnoTemplateDeletedEvent.class);


    }

    public TechnoMigrationService(RestTemplate restTemplate) {
        this.legacyRestTemplate = restTemplate;
        this.refonteRestTemplate = restTemplate;
        refonteRestTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("tech", "password"));
    }

    @Override
    protected Map<TemplateContainer.Key,List<GenericDomainEventMessage<Object>>> convertToDomainEvent(List<LegacyEvent> events) {
        Map map = new HashMap();
        List<GenericDomainEventMessage<Object>> domainEventMessages = new ArrayList<>();
        //Ce booléen force la création d'un nouvel évenement TechnoCreatedEvent, si l'évennement précédent celui traité est de type TemplatePackageDeletedEvent
        AtomicBoolean shouldCreateTechno = new AtomicBoolean(true);
        TemplateContainer.Key key = convertToLegacyEvent(events.get(0)).getKey();
        events.forEach(event -> {
            try {
                LegacyInterface legacyInterface = convertToLegacyEvent(event);
                String aggregateId = legacyInterface.getKey().toString();
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
        map.put(key,domainEventMessages);
        return map;
    }

    @Override
    protected void verify(TemplateContainer.Key key){
        //getURI met lui même le prefix au pluriel, et rajoute un / à la fin
        final String legacyUri = LEGACY_URI + key.getURI("templates/package");
        final String refonteUri = REFONTE_URI + key.getURI("templates/package");

        checkTemplatesList(legacyUri, refonteUri);
//
//        ResponseEntity<TechnoIO> leg = legacyRestTemplate.getForEntity(legacyUri, TechnoIO.class);
//        ResponseEntity<TechnoIO> ref = refonteRestTemplate.getForEntity(refonteUri, TechnoIO.class);
//
//        if (ref.getBody().equals(leg.getBody())) {
//            checkTemplatesList(legacyUri, refonteUri);
//        }

    }
}

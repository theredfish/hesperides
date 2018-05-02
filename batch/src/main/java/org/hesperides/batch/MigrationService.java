package org.hesperides.batch;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.*;
import org.hesperides.batch.redis.legacy.events.modules.*;
import org.hesperides.domain.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.Math.toIntExact;

@Log
@Service
public class MigrationService {
    @Autowired
    EmbeddedEventStore eventBus;

    void migrate(String key, ListOperations<String, LegacyEvent> redisOperations) {
        IntStream.range(0, toIntExact(redisOperations.size(key))).forEach(
                number -> {
                    ConvertModule(redisOperations.index(key, number), Long.valueOf(number));
                }
        );
    }

    private void ConvertModule(LegacyEvent event, Long index) {
        LegacyInterface legacyInterface = ConvertEvent(event);
        if (legacyInterface != null) {

            Long timestamp = event.getTimestamp();
            Supplier<Instant> supplier = () -> Instant.ofEpochMilli(timestamp);
            User user = new User(event.getUser());
            GenericEventMessage eventMessage = new GenericEventMessage(legacyInterface.toDomainEvent(user), MetaData.emptyInstance());
            String aggregateId = legacyInterface.getKey().toString();

            if (eventMessage != null) {
                GenericDomainEventMessage domainEventMessage = new GenericDomainEventMessage<>("ModuleAggregate", aggregateId, index, eventMessage, supplier);

                try {
                    eventBus.publish(domainEventMessage);
                    log.info(domainEventMessage.getAggregateIdentifier() + event.getEventType());

                } catch (Exception e) {
                    log.info("Aie");
                }
            }
        }
    }

    public LegacyInterface ConvertEvent(LegacyEvent event) {
        Gson gson = new Gson();
        Map<String, Type> dictionary = new HashMap<>();
        //Module
        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent", LegacyModuleCreatedEvent.class);
//        TODO: Implémenter technos
//        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent",LegacyModuleUpdatedEvent.class);
        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent", LegacyModuleDeletedEvent.class);
        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent", LegacyModuleTemplateCreatedEvent.class);
        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent", LegacyModuleTemplateUpdatedEvent.class);
        dictionary.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent", LegacyModuleTemplateDeletedEvent.class);

//        Technos
//        La création et mise à jour d'une techno en temps qu'entité n'existe pas sur le Legacy
//        TODO: WIP création et update template dans une techno

//        dictionary.put("com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent",LegacyTechnoTemplateCreatedEvent.class);
//        dictionary.put("com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent",LegacyTechnoTemplateUpdatedEvent.class);
//        dictionary.put("com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent",LegacyTechnoTemplateDeletedEvent.class);

        //Platforms
        String eventType = event.getEventType();
        LegacyInterface result;
        if (dictionary.containsKey(event.getEventType())) {
            result = gson.fromJson(event.getData(), dictionary.get(eventType));
        } else
            result = null;
        return result;


    }
}

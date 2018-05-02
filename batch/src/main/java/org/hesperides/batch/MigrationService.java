package org.hesperides.batch;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.*;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.Math.toIntExact;
@Log
@Service
public class MigrationService {
    @Autowired
    EmbeddedEventStore eventBus;

    void migrate(String key, ListOperations<String,LegacyEvent> redisOperations){
        IntStream.range(0, toIntExact(redisOperations.size(key))).forEach(
                number -> {
                    ConvertModule(redisOperations.index(key, number),Long.valueOf(number));
                }
        );
    }
private void ConvertModule(LegacyEvent event,Long index){
        Long timestamp = event.getTimestamp();
        Supplier<Instant> supplier = ()-> Instant.ofEpochMilli(timestamp);
        Gson gson = new Gson();
        GenericEventMessage eventMessage = null;
        GenericDomainEventMessage domainEventMessage = null;
        User user = new User(event.getUser());

        String aggregateId = "";

        LegacyInterface legacyEvent;
        switch (event.getEventType()){
            case LegacyModuleCreatedEvent.EVENT_TYPE :
                legacyEvent = gson.fromJson(event.getData(),LegacyModuleCreatedEvent.class);
                eventMessage = new GenericEventMessage(legacyEvent.toDomainEvent(user),MetaData.emptyInstance());
                aggregateId = legacyEvent.getKey().toString();
                break;
            case LegacyModuleUpdatedEvent.EVENT_TYPE :
                //TODO Implémentation Update Module, en attente techno
                break;
            case LegacyModuleDeletedEvent.EVENT_TYPE:
                legacyEvent = gson.fromJson(event.getData(),LegacyModuleDeletedEvent.class);
                eventMessage = new GenericEventMessage(legacyEvent.toDomainEvent(user),MetaData.emptyInstance());
                aggregateId = legacyEvent.getKey().toString();

                break;

            case LegacyTemplateCreatedEvent.EVENT_TYPE:
                legacyEvent = gson.fromJson(event.getData(),LegacyTemplateCreatedEvent.class);
                eventMessage = new GenericEventMessage(legacyEvent.toDomainEvent(user),MetaData.emptyInstance());
                aggregateId = legacyEvent.getKey().toString();
                break;
            case LegacyTemplateUpdatedEvent.EVENT_TYPE:
                legacyEvent = gson.fromJson(event.getData(),LegacyTemplateUpdatedEvent.class);
                eventMessage = new GenericEventMessage(legacyEvent.toDomainEvent(user),MetaData.emptyInstance());
                aggregateId = legacyEvent.getKey().toString();

                break;
            case LegacyTemplateDeletedEvent.EVENT_TYPE:
                legacyEvent = gson.fromJson(event.getData(),LegacyTemplateDeletedEvent.class);
                eventMessage = new GenericEventMessage(legacyEvent.toDomainEvent(user),MetaData.emptyInstance());
                aggregateId = legacyEvent.getKey().toString();
                break;
            default:
                throw new UnsupportedOperationException("Deserialization for class " + event.getEventType() + " is not implemented");

        }
        if (eventMessage != null) {
            domainEventMessage = new GenericDomainEventMessage("ModuleAggregate",aggregateId,index,eventMessage,supplier);

            try {
                eventBus.publish(domainEventMessage);
                log.info(domainEventMessage.getAggregateIdentifier() + event.getEventType());

            } catch (Exception e) {
                log.info("Aie");
            }
        }
    }
}

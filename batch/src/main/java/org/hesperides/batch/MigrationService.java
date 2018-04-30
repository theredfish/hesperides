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


        switch (event.getEventType()){
            case LegacyModuleCreatedEvent.EVENT_TYPE :
                LegacyModuleCreatedEvent lmce = gson.fromJson(event.getData(),LegacyModuleCreatedEvent.class);
                Module moduleCr = lmce.toDomainModule();
                ModuleCreatedEvent moduleCreatedEvent = new ModuleCreatedEvent(moduleCr,user);
                eventMessage = new GenericEventMessage(moduleCreatedEvent,MetaData.emptyInstance());
                aggregateId = moduleCr.getKey().toString();
                break;
            case LegacyModuleUpdatedEvent.EVENT_TYPE :
                //TODO Implémentation Update Module, en attente techno
                break;
            case LegacyModuleDeletedEvent.EVENT_TYPE:
                LegacyModuleDeletedEvent lmde = gson.fromJson(event.getData(),LegacyModuleDeletedEvent.class);

                ModuleDeletedEvent moduleDeletedEvent = new ModuleDeletedEvent(lmde.key(),user);
                eventMessage = new GenericEventMessage(moduleDeletedEvent,MetaData.emptyInstance());
                aggregateId = lmde.key().toString();

                break;

            case LegacyTemplateCreatedEvent.EVENT_TYPE:
                LegacyTemplateCreatedEvent ltce = gson.fromJson(event.getData(),LegacyTemplateCreatedEvent.class);
                Template templateCr = ltce.toDomainTemplate();
                TemplateCreatedEvent templateCreatedEvent = new TemplateCreatedEvent(templateCr.getTemplateContainerKey(),templateCr,user);
                eventMessage = new GenericEventMessage(templateCreatedEvent,MetaData.emptyInstance());
                aggregateId = templateCr.getTemplateContainerKey().toString();
                break;
            case LegacyTemplateUpdatedEvent.EVENT_TYPE:
                LegacyTemplateUpdatedEvent ltue = gson.fromJson(event.getData(),LegacyTemplateUpdatedEvent.class);
                Template templateUp = ltue.toDomainTemplate();
                TemplateUpdatedEvent templateUpdatedEvent = new TemplateUpdatedEvent(templateUp.getTemplateContainerKey(),templateUp,user);
                eventMessage = new GenericEventMessage(templateUpdatedEvent,MetaData.emptyInstance());
                aggregateId = templateUp.getTemplateContainerKey().toString();

                break;
            case LegacyTemplateDeletedEvent.EVENT_TYPE:
                LegacyTemplateDeletedEvent ltde = gson.fromJson(event.getData(),LegacyTemplateDeletedEvent.class);

                TemplateDeletedEvent templateDeletedEvent = new TemplateDeletedEvent(ltde.key(),ltde.getTemplateName(),user);
                eventMessage = new GenericEventMessage(templateDeletedEvent,MetaData.emptyInstance());
                aggregateId = ltde.key().toString();
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

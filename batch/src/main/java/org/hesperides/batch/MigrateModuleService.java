package org.hesperides.batch;

import lombok.extern.java.Log;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.modules.*;
import org.springframework.data.redis.core.ListOperations;

import java.util.HashMap;
import java.util.List;

@Log
public class MigrateModuleService extends MigrateAbstractService {

    static {
        PATTERN = "module";
        AGGREGATE_TYPE = "ModuleAggregate";
        CONVERTED_SET = "a-"+PATTERN;
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent", LegacyModuleCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent",LegacyModuleUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent", LegacyModuleDeletedEvent.class);

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent", LegacyModuleTemplateCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent", LegacyModuleTemplateUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent", LegacyModuleTemplateDeletedEvent.class);

    }

    @Override
    protected void processOps(String key, ListOperations<String, LegacyEvent> redisOperations){
        List<GenericDomainEventMessage<Object>> list;
        list = convertToDomainEvent(redisOperations.range(key,0,redisOperations.size(key)));
        checkModuleCreatedFirst(key,redisOperations.index(key,0));
        try{
            log.info("Processing: " + key + " (" + list.size() + (list.size() > 1 ? " events)" : " event)"));
            eventBus.publish(list);
            pushIntoSet(key);
        }
        catch (Exception e){
            log.severe(e.getMessage());
        }
    }

    void checkModuleCreatedFirst(String key,LegacyEvent event){
        if(!"com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent".equals(event.getEventType())){
            log.severe(key);
        }
    }

}

package org.hesperides.batch;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.LegacyInterface;
import org.hesperides.domain.security.User;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Log
abstract class MigrateAbstractService {
    EmbeddedEventStore eventBus;

    static String AGGREGATE_TYPE;
    static String PATTERN;
    static Map<String, Type> LEGACY_EVENTS_DICTIONARY;
    static String CONVERTED_SET;


    RedisTemplate stringTemplate;
    RestTemplate legacyRestTemplate;
    RestTemplate rebornRestTemplate;


    void migrate(RedisTemplate<String, LegacyEvent> redisTemplate, EmbeddedEventStore eventBus, RedisTemplate<String,String> stringTemplate) {
        this.stringTemplate = stringTemplate;
        this.eventBus = eventBus;
        log.info("migrate "  + PATTERN);
        Set<String> keys = redisTemplate.keys(PATTERN + "*");

        keys.forEach(key -> {
            log.info(key);
            if(!isAlreadyConverted(key))
                processOps(key, redisTemplate.opsForList());

        });

    }

    protected void processOps(String key, ListOperations<String, LegacyEvent> redisOperations){
        List<GenericDomainEventMessage<Object>> list;
        list = convertToDomainEvent(redisOperations.range(key,0,redisOperations.size(key)));

        try{
            log.info("Processing: " + key + " (" + list.size() + (list.size() > 1 ? " events)" : " event)"));
            eventBus.publish(list);
            verify(key);
        }
        catch (Exception e){
            log.severe(e.getMessage());
        }
    }

    protected List<GenericDomainEventMessage<Object>> convertToDomainEvent(List<LegacyEvent> events) {
        List<GenericDomainEventMessage<Object>> domainEventMessage = new ArrayList<>();
        events.forEach(event -> {
            try {
                LegacyInterface legacyInterface = convertToLegacyEvent(event);
                Long timestamp = event.getTimestamp();
                Supplier<Instant> supplier = () -> Instant.ofEpochMilli(timestamp);
                User user = new User(event.getUser());
                GenericEventMessage<Object> eventMessage = new GenericEventMessage<>(legacyInterface.toDomainEvent(user), MetaData.emptyInstance());
                String aggregateId = legacyInterface.getKeyString();
                domainEventMessage.add(new GenericDomainEventMessage<>(AGGREGATE_TYPE, aggregateId, domainEventMessage.size(), eventMessage, supplier));
            }
            catch (Exception e){
                log.severe(e.getLocalizedMessage());
            }
        });

        return domainEventMessage;
    }

    protected LegacyInterface convertToLegacyEvent(LegacyEvent event) {
        Gson gson = new Gson();
        String eventType = event.getEventType();
        LegacyInterface result = null;
        if (LEGACY_EVENTS_DICTIONARY.containsKey(event.getEventType())) {
            result = gson.fromJson(event.getData(), LEGACY_EVENTS_DICTIONARY.get(eventType));
        } else {
            log.info("Event conversion " + event.toString() + " not yet implemented");
            throw new NotImplementedException();
        }
        return result;
    }

    protected Boolean isAlreadyConverted(String key){
//        RedisTemplate<String,String> rt = new RedisTemplate<>();
        Boolean bob =  stringTemplate.opsForSet().isMember(CONVERTED_SET,key);
        return bob;
    }

    protected void verify(String key){

        stringTemplate.opsForSet().add(CONVERTED_SET,key);
    }

}

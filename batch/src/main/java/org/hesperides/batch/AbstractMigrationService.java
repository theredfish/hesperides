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
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TemplateIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

@Log
abstract class AbstractMigrationService {
    EmbeddedEventStore eventBus;

    static String AGGREGATE_TYPE;
    static String PATTERN;
    static Map<String, Type> LEGACY_EVENTS_DICTIONARY;
    static String CONVERTED_SET;
    @Value("${legacy.uri}")
    static String LEGACY_URI = "http://localhost:8080";
    @Value("${refonte.uri}")
    static String REFONTE_URI = "http://localhost:8082";

    RedisTemplate stringTemplate;
    RestTemplate legacyRestTemplate;
    RestTemplate refonteRestTemplate;

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

    protected void processOps(String redisKey, ListOperations<String, LegacyEvent> redisOperations){
        Map<TemplateContainer.Key,List<GenericDomainEventMessage<Object>>> list;
        list = convertToDomainEvent(redisOperations.range(redisKey,0,redisOperations.size(redisKey)));
        try{
            log.info("Processing: " + redisKey + " (" + list.size() + (list.size() > 1 ? " events)" : " event)"));
            list.forEach( (key,value) -> {
                eventBus.publish(value);
                verify(key);
            });
        }
        catch (Exception e){
            log.severe(e.getMessage());
        }
    }

    protected Map<TemplateContainer.Key,List<GenericDomainEventMessage<Object>>> convertToDomainEvent(List<LegacyEvent> events) {
        Map map = new HashMap();
        List<GenericDomainEventMessage<Object>> domainEventMessages = new ArrayList<>();
        TemplateContainer.Key key = convertToLegacyEvent(events.get(0)).getKey();
        events.forEach(event -> {
            try {
                LegacyInterface legacyInterface = convertToLegacyEvent(event);
                Long timestamp = event.getTimestamp();
                Supplier<Instant> supplier = () -> Instant.ofEpochMilli(timestamp);
                User user = new User(event.getUser());
                GenericEventMessage<Object> eventMessage = new GenericEventMessage<>(legacyInterface.toDomainEvent(user), MetaData.emptyInstance());
                String aggregateId = legacyInterface.getKey().toString();
                domainEventMessages.add(new GenericDomainEventMessage<>(AGGREGATE_TYPE, aggregateId, domainEventMessages.size(), eventMessage, supplier));
            }
            catch (Exception e){
                log.severe(e.getLocalizedMessage());
            }
        });
        map.put(key,domainEventMessages);

        return map;
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

    protected void verify(TemplateContainer.Key key){

        stringTemplate.opsForSet().add(CONVERTED_SET,key.toString());
    }

    void checkTemplatesList(String legacyUri, String refonteUri) {
        String tempLegacyUri = legacyUri + "/templates/";
        String tempRefonteUri = refonteUri + "/templates/";

        ResponseEntity<PartialTemplateIO[]> leg = legacyRestTemplate.getForEntity(tempLegacyUri, PartialTemplateIO[].class);
        ResponseEntity<PartialTemplateIO[]> ref = legacyRestTemplate.getForEntity(tempRefonteUri, PartialTemplateIO[].class);

        log.info(leg.getBody().hashCode() + " " +ref.getBody().hashCode());
        if ( Arrays.equals(leg.getBody(),ref.getBody())) {
            Arrays.stream(leg.getBody()).forEach(template -> checkTemplate(template.getName(), legacyUri, refonteUri));
        } else {
            log.severe("Liste des templates différente : " + tempLegacyUri);
        }
    }

    void checkTemplate(String templateName, String legacyUri, String refonteUri) {
        String tempLegacyUri = legacyUri + "/templates/" + templateName;
        String tempRefonteUri = refonteUri + "/templates/" + templateName;

        ResponseEntity<TemplateIO> leg = legacyRestTemplate.getForEntity(tempLegacyUri, TemplateIO.class);
        ResponseEntity<TemplateIO> ref = refonteRestTemplate.getForEntity(tempRefonteUri, TemplateIO.class);
        if (!ref.getBody().equals(leg.getBody())) {
            log.severe("Template " + templateName + " différent : " + tempLegacyUri);
        }
    }

}

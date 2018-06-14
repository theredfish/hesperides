package org.hesperides.batch.service;

import com.google.gson.Gson;
import lombok.extern.java.Log;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.messaging.MetaData;
import org.hesperides.batch.token.MongoTokenRepository;
import org.hesperides.batch.token.Token;
import org.hesperides.batch.legacy.entities.LegacyEvent;
import org.hesperides.batch.legacy.events.LegacyInterface;
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
public abstract class AbstractMigrationService {
    static String AGGREGATE_TYPE;
    static String PATTERN;
    static Map<String, Type> LEGACY_EVENTS_DICTIONARY;
    static String CONVERTED_SET;
    @Value("${legacy.uri}")
    static String LEGACY_URI = "http://localhost:8080";
    @Value("${refonte.uri}")
    static String REFONTE_URI = "http://localhost:8082";

    ListOperations<String, LegacyEvent> listOperations;

    EmbeddedEventStore eventBus;
    RestTemplate legacyRestTemplate;
    RestTemplate refonteRestTemplate;
    MongoTokenRepository mongoTokenRepository;
    Token token;

    AbstractMigrationService(EmbeddedEventStore eventBus, RestTemplate restTemplate,
                             ListOperations<String, LegacyEvent> listOperations, MongoTokenRepository mongoTokenRepository) {
        this.eventBus = eventBus;
        this.legacyRestTemplate = restTemplate;
        this.refonteRestTemplate = restTemplate;
        this.listOperations = listOperations;
        this.mongoTokenRepository = mongoTokenRepository;
        refonteRestTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("tech", "password"));
    }

    public void migrate(List<Token> tokenList) {
        tokenList.forEach(token -> {
            this.token = token;
            processOps();

        });
    }

    protected void processOps() {
        List<GenericDomainEventMessage<Object>> eventsList = convertToDomainEvent(listOperations.range(token.getKey(), 0, -1));
        try {
            log.info("Processing: " + token.getKey() + " (" + eventsList.size() + (eventsList.size() > 1 ? " events)" : " event)"));
            token.setLegacyEventCount(eventsList.size());
            eventBus.publish(eventsList);
            verify(token.getRefonteKey());

        } catch (ConcurrencyException e) {
            log.info("pouet");
            verify(token.getRefonteKey());
        } catch (Exception e) {
            log.severe(e.getMessage() + " c'est pour voir quand ça pète");
            token.setStatus(Token.DELETED);
        } finally {
            mongoTokenRepository.save(token);
        }
    }

    protected List<GenericDomainEventMessage<Object>> convertToDomainEvent(List<LegacyEvent> events) {
        List<GenericDomainEventMessage<Object>> domainEventMessages = new ArrayList<>();
        this.token.setRefonteKey(convertToLegacyEvent(events.get(0)).getKey());
        events.forEach(event -> {
            try {
                LegacyInterface legacyInterface = convertToLegacyEvent(event);
                Long timestamp = event.getTimestamp();
                Supplier<Instant> supplier = () -> Instant.ofEpochMilli(timestamp);
                User user = new User(event.getUser(), true, true);
                GenericEventMessage<Object> eventMessage = new GenericEventMessage<>(legacyInterface.toDomainEvent(user), MetaData.emptyInstance());
                String aggregateId = legacyInterface.getKey().toString();
                domainEventMessages.add(new GenericDomainEventMessage<>(AGGREGATE_TYPE, aggregateId, domainEventMessages.size(), eventMessage, supplier));
            } catch (Exception e) {
                log.severe(e.getLocalizedMessage());
            }
        });
        token.setRefonteEventCount(domainEventMessages.size());
        return domainEventMessages;
    }

    LegacyInterface convertToLegacyEvent(LegacyEvent event) {
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

    abstract void verify(TemplateContainer.Key key);

    void checkTemplatesList(String legacyUri, String refonteUri) {
        String tempLegacyUri = legacyUri + "/templates/";
        String tempRefonteUri = refonteUri + "/templates/";

        try {
            ResponseEntity<PartialTemplateIO[]> legacyResponse = legacyRestTemplate.getForEntity(tempLegacyUri, PartialTemplateIO[].class);
            ResponseEntity<PartialTemplateIO[]> refonteResponse = legacyRestTemplate.getForEntity(tempRefonteUri, PartialTemplateIO[].class);
            PartialTemplateIO[] legacyArray = legacyResponse.getBody();
            PartialTemplateIO[] refonteArray = refonteResponse.getBody();

            Arrays.sort(legacyArray);
            Arrays.sort(refonteArray);


            if (Arrays.equals(legacyArray, refonteArray)) {
                Arrays.stream(legacyArray).forEach(template -> checkTemplate(template.getName(), legacyUri, refonteUri));
            } else {
                log.severe("Liste des templates différente : " + tempLegacyUri);
                this.token.setStatus(Token.KO);

            }
        }catch (Exception e){
            log.info(e.getMessage());
        }


    }

    private void checkTemplate(String templateName, String legacyUri, String refonteUri) {
        if (token.getStatus() == Token.KO)
            return;

        String tempLegacyUri = legacyUri + "/templates/" + templateName;
        String tempRefonteUri = refonteUri + "/templates/" + templateName;

        ResponseEntity<TemplateIO> leg = legacyRestTemplate.getForEntity(tempLegacyUri, TemplateIO.class);
        ResponseEntity<TemplateIO> ref = refonteRestTemplate.getForEntity(tempRefonteUri, TemplateIO.class);

        TemplateIO legacyTemplate = leg.getBody();
        TemplateIO refonteTemplate = ref.getBody();

        if (legacyTemplate.getRights() == null) {
            TemplateIO.RightsIO rights = new TemplateIO.RightsIO(new TemplateIO.FileRightsIO(null, null, null)
                    , new TemplateIO.FileRightsIO(null, null, null)
                    , new TemplateIO.FileRightsIO(null, null, null));

            legacyTemplate = new TemplateIO(legacyTemplate.getName(),
                    legacyTemplate.getNamespace(),
                    legacyTemplate.getFilename(),
                    legacyTemplate.getLocation(),
                    legacyTemplate.getContent(),
                    rights,
                    legacyTemplate.getVersionId());
        }

        if (!refonteTemplate.equals(legacyTemplate)) {
            log.severe("Template " + templateName + " différent : " + tempLegacyUri);
            this.token.setStatus(Token.KO);
        } else {
            this.token.setStatus(Token.OK);
        }
    }

}

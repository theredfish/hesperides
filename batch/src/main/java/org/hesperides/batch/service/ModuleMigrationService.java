package org.hesperides.batch.service;

import lombok.extern.java.Log;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.batch.token.MongoTokenRepository;
import org.hesperides.batch.token.Token;
import org.hesperides.batch.legacy.entities.LegacyEvent;
import org.hesperides.batch.legacy.events.modules.*;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;

@Log
public class ModuleMigrationService extends AbstractMigrationService {

    static {
        PATTERN = "module";
        AGGREGATE_TYPE = "ModuleAggregate";
        CONVERTED_SET = "a-" + PATTERN + "set";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();

//        LEGACY_URI = LEGACY_URI + "/modules";
//        REFONTE_URI = REFONTE_URI + "/modules";

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent", LegacyModuleCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent", LegacyModuleUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent", LegacyModuleDeletedEvent.class);

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent", LegacyModuleTemplateCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent", LegacyModuleTemplateUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent", LegacyModuleTemplateDeletedEvent.class);

    }

    public ModuleMigrationService(EmbeddedEventStore eventBus, RestTemplate restTemplate, ListOperations<String, LegacyEvent> listOperations, MongoTokenRepository mongoTokenRepository) {
        super(eventBus, restTemplate, listOperations, mongoTokenRepository);
    }

    @Override
    protected void processOps() {

        if (checkModuleCreatedFirst(token.getKey(), listOperations.index(token.getKey(), 0))) {
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
    }

    //    @Before("execution(* org.hesperides.batch.service.ModuleMigrationService.processOps(..))")
    public Boolean checkModuleCreatedFirst(String key, LegacyEvent event) {
        Boolean ret = true;
        if (!"com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent".equals(event.getEventType())) {
            log.severe(key);
            ret = false;
            token.setStatus(Token.MODULE_ERRORED);
        }
        return ret;
    }

    @Override
    protected void verify(TemplateContainer.Key key) {
        final String legacyUri = LEGACY_URI + key.getURI("module");
        final String refonteUri = REFONTE_URI + key.getURI("module");

        try {
            ResponseEntity<ModuleIO> leg = legacyRestTemplate.getForEntity(legacyUri, ModuleIO.class);
            ResponseEntity<ModuleIO> ref = refonteRestTemplate.getForEntity(refonteUri, ModuleIO.class);
            if (ref.getBody().equals(leg.getBody())) {
                checkTemplatesList(legacyUri, refonteUri);
            } else {
                token.setStatus(Token.KO);
            }
        }
        catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 404) {
                token.setStatus(Token.DELETED);
            }
        }
        catch (Exception e){
            log.info(e.getMessage());
        }
    }

//    void checkTemplatesList(String legacyUri, String refonteUri) {
//
//        String tempLegacyUri = legacyUri + "/templates/";
//        String tempRefonteUri = refonteUri + "/templates/";
//
//        ResponseEntity<PartialTemplateIO[]> leg = legacyRestTemplate.getForEntity(tempLegacyUri, PartialTemplateIO[].class);
//        ResponseEntity<PartialTemplateIO[]> ref = legacyRestTemplate.getForEntity(tempRefonteUri, PartialTemplateIO[].class);
//
//        if ( Arrays.equals(leg.getBody(),ref.getBody())) {
//            Arrays.stream(leg.getBody()).forEach(template -> checkTemplate(template.getName(), legacyUri, refonteUri));
//        } else {
//            log.severe("Liste des templates différente : " + tempLegacyUri);
//        }
//    }
//
//    void checkTemplate(String templateName, String legacyUri, String refonteUri) {
//        String tempLegacyUri = legacyUri + "/templates/" + templateName;
//        String tempRefonteUri = refonteUri + "/templates/" + templateName;
//
//        ResponseEntity<TemplateIO> leg = legacyRestTemplate.getForEntity(tempLegacyUri, TemplateIO.class);
//        ResponseEntity<TemplateIO> ref = refonteRestTemplate.getForEntity(tempRefonteUri, TemplateIO.class);
//        if (!ref.getBody().equals(leg.getBody())) {
//            log.severe("Template " + templateName + " différent : " + tempLegacyUri);
//        }
//    }
}

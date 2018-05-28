package org.hesperides.batch;

import lombok.extern.java.Log;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.hesperides.batch.redis.legacy.events.modules.*;
import org.hesperides.presentation.io.ModuleIO;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TemplateIO;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Log
public class MigrateModuleService extends MigrateAbstractService {

    static {
        PATTERN = "module-module-ptor-1.0-releas";
        AGGREGATE_TYPE = "ModuleAggregate";
        CONVERTED_SET = "a-" + PATTERN + "set";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent", LegacyModuleCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent", LegacyModuleUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent", LegacyModuleDeletedEvent.class);

        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent", LegacyModuleTemplateCreatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent", LegacyModuleTemplateUpdatedEvent.class);
        LEGACY_EVENTS_DICTIONARY.put("com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent", LegacyModuleTemplateDeletedEvent.class);

    }

    public MigrateModuleService(RestTemplate restTemplate) {
        this.legacyRestTemplate = restTemplate;
        this.rebornRestTemplate = restTemplate;
        rebornRestTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("tech", "password"));
    }


//    private final RestTemplate legacyRestTemplate;
//    private final RestTemplate rebornRestTemplate;
//    public MigrateModuleService(RestTemplate restTemplate) {
//        this.legacyRestTemplate = restTemplate;
//        this.rebornRestTemplate = restTemplate;
//        rebornRestTemplate.getInterceptors().add(new BasicAuthorizationInterceptor("tech","password"));
//    }


    @Override
    protected void processOps(String key, ListOperations<String, LegacyEvent> redisOperations) {
        List<GenericDomainEventMessage<Object>> list;
        list = convertToDomainEvent(redisOperations.range(key, 0, -1));
        checkModuleCreatedFirst(key, redisOperations.index(key, 0));
        try {
            log.info("Processing: " + key + " (" + list.size() + (list.size() > 1 ? " events)" : " event)"));
            eventBus.publish(list);
            verify(key);
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    void checkModuleCreatedFirst(String key, LegacyEvent event) {
        if (!"com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent".equals(event.getEventType())) {
            log.severe(key);
        }
    }

    @Override
    protected void verify(String key) {
        //TODO : mettre au bon endroit
        final String legacyUri = "http://localhost:8080/rest/modules/module-ptor/1.0/release";
//        RestTemplate legacyRestTemplate = new RestTemplate();
        final String rebornUri = "http://localhost:8081/rest/modules/module-ptor/1.0/release";
//        RestTemplate rebornRestTemplate = new RestTemplate();


        ResponseEntity<ModuleIO> pouet = legacyRestTemplate.getForEntity(legacyUri, ModuleIO.class);
        log.info("pouet : " + pouet.getBody().toString());
        ResponseEntity<ModuleIO> pouetReborn = rebornRestTemplate.getForEntity(rebornUri, ModuleIO.class);
        log.info("pouetReborn : " + pouetReborn.getBody().toString());

        ResponseEntity<String> respLeg = legacyRestTemplate.getForEntity(legacyUri, String.class);
        ResponseEntity<String> respReb = rebornRestTemplate.getForEntity(rebornUri, String.class);
        if (respReb.getBody().equals(respLeg.getBody())) {
            String tempLegacyUri = legacyUri + "/templates/";
            String tempRebornUri = rebornUri + "/templates/";
            respLeg = rebornRestTemplate.getForEntity(tempLegacyUri, String.class);
            ResponseEntity<PartialTemplateIO[]> partialLegacy = legacyRestTemplate.getForEntity(tempLegacyUri, PartialTemplateIO[].class);
            respReb = rebornRestTemplate.getForEntity(tempRebornUri, String.class);

            if (respReb.getBody().equals(respLeg.getBody())) {
                Arrays.stream(partialLegacy.getBody()).forEach(template -> {
                    String templateLegacyUri = legacyUri + "/templates/"+ template.getName();
                    String templateRebornUri = rebornUri + "/templates/"+ template.getName();
                    ResponseEntity<TemplateIO> leg = legacyRestTemplate.getForEntity(templateLegacyUri,TemplateIO.class);
                    ResponseEntity<TemplateIO> reb = rebornRestTemplate.getForEntity(templateRebornUri,TemplateIO.class);
                    if(reb.getBody().equals(leg.getBody())){
                        log.info("Youpi, on passe à la suite");
                    }

                });
            }
        }


    }

}

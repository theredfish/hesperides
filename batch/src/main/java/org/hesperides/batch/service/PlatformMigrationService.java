package org.hesperides.batch.service;

import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.batch.token.MongoTokenRepository;
import org.hesperides.batch.legacy.entities.LegacyEvent;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

public class PlatformMigrationService extends AbstractMigrationService {
    static {
        //TODO : être sur de cet aggregat
        AGGREGATE_TYPE = "PlatformAggregate";
        PATTERN = "platform";
        LEGACY_EVENTS_DICTIONARY = new HashMap<>();


    }


    public PlatformMigrationService(EmbeddedEventStore eventBus, RestTemplate restTemplate, RedisTemplate<String, LegacyEvent> redisTemplate, MongoTokenRepository mongoTokenRepository) {
        super(eventBus, restTemplate, redisTemplate, mongoTokenRepository);
    }

    @Override
    void verify(TemplateContainer.Key key) {

    }
}

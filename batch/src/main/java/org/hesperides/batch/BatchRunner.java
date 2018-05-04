package org.hesperides.batch;


import lombok.extern.java.Log;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

import java.util.Set;

@Log
@Component
@Profile("batch")
public class BatchRunner {

    private static final String SEARCHPATTERN = "module";
    @Autowired
    EmbeddedEventStore eventBus;

    private ApplicationRunner titledRunner(String title, ApplicationRunner rr){
        return args -> {
            log.info(title.toUpperCase() + " : ");
            rr.run(args);
        };
    }


    @Bean
    ApplicationRunner moduleImport(RedisTemplate<String,LegacyEvent> rt,MigrationService migrationService){
        return titledRunner("moduleImport",args ->{

            Set<String> keys = rt.keys(SEARCHPATTERN +"*");
            keys.forEach(key -> migrationService.migrate(key,rt.opsForList()));
        });
    }


    @Bean
    RedisTemplate<String,LegacyEvent> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String,LegacyEvent> template = new RedisTemplate<>();

        RedisSerializer<LegacyEvent> values = new Jackson2JsonRedisSerializer<>(LegacyEvent.class);
        RedisSerializer keys = new StringRedisSerializer();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keys);
        template.setHashKeySerializer(keys);
        template.setValueSerializer(values);
        template.setHashValueSerializer(values);

        return template;
    }
}

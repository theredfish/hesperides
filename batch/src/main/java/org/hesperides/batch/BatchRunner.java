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

@Log
@Component
@Profile("batch")
public class BatchRunner {

    @Autowired
    EmbeddedEventStore eventBus;

    private ApplicationRunner titledRunner(ApplicationRunner rr){
        return args -> {
            log.info("Let's Gooo : ");
            rr.run(args);
        };
    }

    @Bean
    ApplicationRunner moduleImport(RedisTemplate<String,LegacyEvent> redisTemplate){
        return titledRunner(args ->{
            MigrateAbstractService migrateTechno = new MigrateTechnoService();
            migrateTechno.migrate(redisTemplate,eventBus);
            MigrateAbstractService migrateModule = new MigrateModuleService();
            migrateModule.migrate(redisTemplate,eventBus);
//            MigrateAbstractService migratePlatform = new MigratePlatformService();
//            migratePlatform.migrate(redisTemplate,eventBus);

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

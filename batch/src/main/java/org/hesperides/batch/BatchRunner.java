package org.hesperides.batch;


import lombok.extern.java.Log;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Log
@Component
@Profile("batch")
public class BatchRunner {

    @Autowired
    EmbeddedEventStore eventBus;

    private ApplicationRunner titledRunner(String title,ApplicationRunner rr){
        return args -> {
            log.info(title+" : ");
            rr.run(args);
        };
    }

//    @Bean
//    ApplicationRunner hashCreation(RedisTemplate<String,String> legacyTemplate){
//        return titledRunner("phase 1",args ->{
//            legacyTemplate.keys("modu*").forEach(key -> legacyTemplate.opsForSet().add("Set",key+0));
//        });
//    }

    @Bean
    ApplicationRunner moduleImport(RedisTemplate<String,LegacyEvent> legacyTemplate,RedisTemplate<String,String> stringTemplate,RestTemplate restTemplate){
        return titledRunner("phase 2",args ->{
//            MigrateAbstractService migrateTechno = new MigrateTechnoService();
//            migrateTechno.migrate(legacyTemplate,eventBus,stringTemplate);
            MigrateAbstractService migrateModule = new MigrateModuleService(restTemplate);
            migrateModule.migrate(legacyTemplate,eventBus,stringTemplate);
//            MigrateAbstractService migratePlatform = new MigratePlatformService();
//            migratePlatform.migrate(legacyTemplate,eventBus);

        });
    }


//    @Bean
//    RedisTemplate<String,LegacyEvent> legacyTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String,LegacyEvent> template = new RedisTemplate<>();
//
//        RedisSerializer<LegacyEvent> values = new Jackson2JsonRedisSerializer<>(LegacyEvent.class);
//        RedisSerializer keys = new StringRedisSerializer();
//
//        template.setConnectionFactory(redisConnectionFactory);
//        template.setKeySerializer(keys);
//        template.setHashKeySerializer(keys);
//        template.setValueSerializer(values);
//        template.setHashValueSerializer(values);
//
//        return template;
//    }

//    @Bean
//    SetOperations<String,String> stringTemplate(RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<String,String> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//
//        RedisSerializer wtf = new StringRedisSerializer();
//        template.setKeySerializer(wtf);
//        template.setHashKeySerializer(wtf);
//        template.setValueSerializer(wtf);
//        template.setHashValueSerializer(wtf);
//        return template.opsForSet();
//    }
}

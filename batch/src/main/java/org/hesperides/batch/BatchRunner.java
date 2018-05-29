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
            AbstractMigrationService migrateTechno = new TechnoMigrationService(restTemplate);
            migrateTechno.migrate(legacyTemplate,eventBus,stringTemplate);
            AbstractMigrationService migrateModule = new ModuleMigrationService(restTemplate);
            migrateModule.migrate(legacyTemplate,eventBus,stringTemplate);
//            AbstractMigrationService migratePlatform = new PlatformMigrationService();
//            migratePlatform.migrate(legacyTemplate,eventBus);

        });
    }
}

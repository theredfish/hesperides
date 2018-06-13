package org.hesperides.batch;


import lombok.extern.java.Log;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log
@Component
@Profile("batch")
public class BatchRunner {


    private final EmbeddedEventStore eventBus;
    private final MongoTokenRepository mongoTokenRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public BatchRunner(MongoTokenRepository mongoTokenRepository, EmbeddedEventStore eventBus, MongoTemplate mongoTemplate) {
        this.mongoTokenRepository = mongoTokenRepository;
        this.eventBus = eventBus;
        this.mongoTemplate = mongoTemplate;
    }

    private ApplicationRunner titledRunner(String title, ApplicationRunner rr) {
        return args -> {
            log.info(title + " : ");
            rr.run(args);
        };
    }

    @Bean
    ApplicationRunner moduleImport(RedisTemplate<String, LegacyEvent> legacyTemplate, RestTemplate restTemplate) {
        return titledRunner("Convertion events Legacy", args -> {
            List<Token> templateList = new ArrayList<>();
            List<Token> moduleList = new ArrayList<>();
            if (mongoTemplate.collectionExists("token")) {
                log.info("Récupération de le la liste de Tokens");
                templateList = mongoTokenRepository.findAllByTypeAndStatus("techno",Token.WIP);
                moduleList = mongoTokenRepository.findAllByTypeAndStatus("module",Token.WIP);

            } else {
                log.info("Création de le la liste de Tokens");
                Set<String> legacySet = legacyTemplate.keys("template*");
                List<Token> finalTemplateList = templateList;
                legacySet.forEach(item -> finalTemplateList.add(new Token(item, "techno")));
                mongoTokenRepository.save(templateList);

                legacySet = legacyTemplate.keys("module*");
                List<Token> finalModuleList = moduleList;
                legacySet.forEach(item -> finalModuleList.add(new Token(item, "module")));
                mongoTokenRepository.save(moduleList);
            }

            AbstractMigrationService migrateTechno = new TechnoMigrationService(eventBus, restTemplate, legacyTemplate, mongoTokenRepository);
            migrateTechno.migrate(templateList);
            AbstractMigrationService migrateModule = new ModuleMigrationService(eventBus, restTemplate, legacyTemplate, mongoTokenRepository);
            migrateModule.migrate(moduleList);
//            AbstractMigrationService migratePlatform = new PlatformMigrationService();
//            migratePlatform.migrate(legacyTemplate,eventBus);

        });
    }
}

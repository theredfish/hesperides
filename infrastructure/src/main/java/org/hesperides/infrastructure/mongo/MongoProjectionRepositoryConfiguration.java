package org.hesperides.infrastructure.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;

import static org.hesperides.domain.Profiles.MONGO;

@Configuration
@Profile(MONGO)
public class MongoProjectionRepositoryConfiguration {

    @Value("${hesperides.projection_repository.database}")
    private String database;

    @Value("${hesperides.projection_repository.host}")
    private String host;

    @Value("${hesperides.projection_repository.port}")
    private int port;

    @Value("${hesperides.projection_repository.username}")
    private String username;

    @Value("${hesperides.projection_repository.password}")
    private String password;

    @Bean
    public Mongo mongo() {
        return new MongoClient(new ServerAddress(host, port), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongo(), database);
    }

}

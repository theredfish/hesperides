package org.hesperides.batch;

import lombok.extern.java.Log;
import org.hesperides.batch.legacy.entities.LegacyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Log
@Configuration
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = "org.hesperides.batch")
@Profile("batch")
public class BatchConfiguration {
    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.host}")
    private String redisHost;


    @Bean
    RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory con = new JedisConnectionFactory();
        con.setHostName(redisHost);
        con.setPort(Integer.parseInt(redisPort));
        return con;
    }

    @Bean
    RedisTemplate<String, LegacyEvent> legacyTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, LegacyEvent> template = new RedisTemplate<>();

        RedisSerializer<LegacyEvent> values = new Jackson2JsonRedisSerializer<>(LegacyEvent.class);
        RedisSerializer keys = new StringRedisSerializer();

        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(keys);
        template.setHashKeySerializer(keys);
        template.setValueSerializer(values);
        template.setHashValueSerializer(values);

        return template;
    }

    @Bean
    RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());

        converters.add(new GsonHttpMessageConverter());
        restTemplate.setMessageConverters(converters);
        return restTemplate;
    }
}

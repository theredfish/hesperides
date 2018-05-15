package org.hesperides.batch;

import org.hesperides.batch.redis.legacy.entities.LegacyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class Config {
    @Value("${REDIS_PORT}")
    private String redisPort;
    @Value("${REDIS_HOST}")
    private String redisHost;


    @Bean
    RedisConnectionFactory redisConnectionFactory(){
        JedisConnectionFactory con = new JedisConnectionFactory();
        con.setHostName(redisHost);
        con.setPort(Integer.parseInt(redisPort));
        return con;
    }

    @Bean
    RedisTemplate<String,LegacyEvent> legacyTemplate(RedisConnectionFactory redisConnectionFactory) {
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
    @Bean
    RedisTemplate<String,String> stringTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisSerializer wtf = new StringRedisSerializer();
        template.setKeySerializer(wtf);
        template.setHashKeySerializer(wtf);
        template.setValueSerializer(wtf);
        template.setHashValueSerializer(wtf);
        return template;
    }
}

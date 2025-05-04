package ru.hpclab.hl.module1.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.hpclab.hl.module1.dto.CourierDTO;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, CourierDTO> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, CourierDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        Jackson2JsonRedisSerializer<CourierDTO> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, CourierDTO.class);

        template.setValueSerializer(serializer);
        return template;
    }
}
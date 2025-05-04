package ru.hpclab.hl.module1.configuration;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hpclab.hl.module1.service.CourierStatisticsService;
import ru.hpclab.hl.module1.service.cache.CourierRedisCache;

@Configuration
public class ServicesConfig {

    @Bean
    @ConditionalOnProperty(prefix = "statistics", name = "service", havingValue = "true")
    CourierStatisticsService statisticsService(CourierRedisCache courierStatisticsService) {
        return new CourierStatisticsService(courierStatisticsService);
    }
}

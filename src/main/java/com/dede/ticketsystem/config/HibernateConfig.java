package com.dede.ticketsystem.config;

import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Bean
    public PhysicalNamingStrategyStandardImpl physicalNamingStrategy() {
        return new PhysicalNamingStrategyStandardImpl();
    }
}

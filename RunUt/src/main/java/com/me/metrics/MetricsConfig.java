package com.me.metrics;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public StatsDClient statsDClient(
            @Value("${metrics.statsd.host:localhost}") String hostname,
            @Value("${metrics.statsd.port:8125}") int port,
            @Value("metrics.prefix:csye6225.webapp") String prefix
    ){
        return new NonBlockingStatsDClient(prefix,hostname,port);
    }
}

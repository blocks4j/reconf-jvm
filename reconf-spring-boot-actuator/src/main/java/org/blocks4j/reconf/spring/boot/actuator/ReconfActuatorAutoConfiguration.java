package org.blocks4j.reconf.spring.boot.actuator;

import org.blocks4j.reconf.client.setup.Environment;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReconfActuatorAutoConfiguration {

    private static final String SPRING_BOOT_APPLICATION_PROPERTIES = "application.properties";

    public static final String BLOCKS4J_RECONF_PREFIX = "blocks4j.reconf.";

    static {
        String propertyFile = System.getProperty(Environment.SYSTEM_PROPERTY, SPRING_BOOT_APPLICATION_PROPERTIES);
        System.setProperty(Environment.SYSTEM_PROPERTY, propertyFile);

        System.setProperty(Environment.SYSTEM_PROPERTY_PREFIX, BLOCKS4J_RECONF_PREFIX);
    }

    @Bean
    public ReconfSyncEndpoint reconfSyncEndpoint() {
        return new ReconfSyncEndpoint();
    }

    @Bean
    @ConditionalOnEnabledEndpoint(ReconfSyncEndpoint.RECONF_SYNC_ENDPOINT_ID)
    public ReconfSyncMvcEndpoint reconfMvcEndpoint() {
        return new ReconfSyncMvcEndpoint(this.reconfSyncEndpoint());
    }

}

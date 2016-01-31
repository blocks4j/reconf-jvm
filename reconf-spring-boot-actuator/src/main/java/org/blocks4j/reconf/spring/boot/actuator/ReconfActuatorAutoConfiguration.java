package org.blocks4j.reconf.spring.boot.actuator;

import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReconfActuatorAutoConfiguration {

    @Bean
    public ReconfSyncEndpoint reconfListEndpoint() {
        return new ReconfSyncEndpoint();
    }

    @Bean
    @ConditionalOnEnabledEndpoint(ReconfSyncEndpoint.RECONF_SYNC_ENDPOINT_ID)
    public ReconfSyncMvcEndpoint reconfMvcEndpoint() {
        return new ReconfSyncMvcEndpoint(this.reconfListEndpoint());
    }

}

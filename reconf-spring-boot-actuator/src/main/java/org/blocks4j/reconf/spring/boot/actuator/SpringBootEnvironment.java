package org.blocks4j.reconf.spring.boot.actuator;

import org.blocks4j.reconf.client.config.MapDBConfigurationRepository;
import org.blocks4j.reconf.client.setup.AbstractEnvironment;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;

public class SpringBootEnvironment extends AbstractEnvironment {

    public SpringBootEnvironment(ReconfConfiguration reconfConfiguration) {
        super(reconfConfiguration, getMapDBConfigurationRepository(reconfConfiguration));
    }

    private static MapDBConfigurationRepository getMapDBConfigurationRepository(ReconfConfiguration reconfConfiguration) {
        return new MapDBConfigurationRepository(reconfConfiguration.getLocalCacheSettings());
    }
}
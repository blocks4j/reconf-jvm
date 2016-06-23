package org.blocks4j.reconf.spring.boot.actuator;

import org.blocks4j.reconf.client.factory.ConfigurationRepositoryFactory;
import org.blocks4j.reconf.client.setup.Environment;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.client.setup.config.parser.PropertiesParser;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Configuration
public class ReconfActuatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ReconfConfiguration.class)
    public ReconfConfiguration reconfConfiguration(AbstractEnvironment environment) {
        return PropertiesParser.withProperties(this.loadPropertiesFromEnvironment(environment))
                               .withPrefix("blocks4j.reconf.")
                               .parse();
    }

    private Properties loadPropertiesFromEnvironment(AbstractEnvironment environment) {
        return this.extractIntoProperties(environment.getPropertySources().iterator());
    }

    private Properties extractIntoProperties(Iterator<PropertySource<?>> propertySourceIterator) {
        return this.accumulatePropertiesFromSources(propertySourceIterator)
                   .reduce(new Properties(), (properties, currentProperties) -> {
                       properties.putAll(currentProperties);
                       return properties;
                   });
    }

    private Stream<Properties> accumulatePropertiesFromSources(Iterator<PropertySource<?>> propertySourceIterator) {
        return this.getMapSources(propertySourceIterator)
                   .map(mapPropertySource -> {
                       Properties properties = new Properties();
                       for (String propName : mapPropertySource.getPropertyNames()) {
                           properties.put(propName, mapPropertySource.getProperty(propName));
                       }
                       return properties;
                   });
    }

    private Stream<MapPropertySource> getMapSources(Iterator<PropertySource<?>> propertySourceIterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(propertySourceIterator, Spliterator.ORDERED), false)
                            .filter(propertySource -> propertySource instanceof MapPropertySource)
                            .map(propertySource -> (MapPropertySource) propertySource);
    }

    @Bean
    @ConditionalOnMissingBean(Environment.class)
    public Environment reconfActuatorAutoConfigurationEnvironment(ReconfConfiguration reconfConfiguration) {
        return new SpringBootEnvironment(reconfConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean(ConfigurationRepositoryFactory.class)
    public ConfigurationRepositoryFactory configurationRepositoryFactory(Environment environment) {
        return new ConfigurationRepositoryFactory(environment);
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

package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.config.MapDBConfigurationRepository;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.client.setup.config.parser.PropertiesParser;
import org.blocks4j.reconf.infra.io.ClasspathInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultEnvironment extends AbstractEnvironment {

    private static final String RECONF_DEFAULT_FILE = "reconf.properties";
    private static final String SYSTEM_PROPERTY = "reconf.client.file.location";
    private static final String SYSTEM_PROPERTY_PREFIX = "reconf.client.file.properties.prefix";

    public DefaultEnvironment(ReconfConfiguration reconfConfiguration) {
        super(reconfConfiguration, getMapDBConfigurationRepository(reconfConfiguration));
    }

    public DefaultEnvironment() {
        this(getDefaultReconfConfiguration());
    }

    private static MapDBConfigurationRepository getMapDBConfigurationRepository(ReconfConfiguration reconfConfiguration) {
        return new MapDBConfigurationRepository(reconfConfiguration.getLocalCacheSettings());
    }

    private static ReconfConfiguration getDefaultReconfConfiguration() {
        InputStream configurationInputStream = getDefaultConfigurationInputStream();
        String prefix = System.getProperty(SYSTEM_PROPERTY_PREFIX);

        return PropertiesParser.forInputStream(configurationInputStream)
                               .withPrefix(prefix)
                               .parse();
    }

    private static InputStream getDefaultConfigurationInputStream() {
        String fileLocation = System.getProperty(SYSTEM_PROPERTY, RECONF_DEFAULT_FILE);

        InputStream inputStream = ClasspathInputStream.from(fileLocation);
        if (inputStream == null) {
            try {
                inputStream = new FileInputStream(fileLocation);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        return inputStream;
    }
}
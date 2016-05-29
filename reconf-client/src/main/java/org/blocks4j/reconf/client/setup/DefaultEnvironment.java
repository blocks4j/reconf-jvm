package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.client.setup.config.parser.PropertiesFileParser;
import org.blocks4j.reconf.infra.io.ClasspathInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultEnvironment extends Environment {

    private static final String RECONF_DEFAULT_FILE = "reconf.properties";
    private static final String SYSTEM_PROPERTY = "reconf.client.file.location";
    private static final String SYSTEM_PROPERTY_PREFIX = "reconf.client.file.properties.prefix";


    public DefaultEnvironment() {
        super();
    }

    @Override
    public ReconfConfiguration getDefaultConfiguration() {
        InputStream configurationInputStream = this.getDefaultConfigurationInputStream();
        String prefix = System.getProperty(SYSTEM_PROPERTY_PREFIX);

        return PropertiesFileParser.forInputStream(configurationInputStream)
                .withPrefix(prefix)
                .parse();
    }

    private InputStream getDefaultConfigurationInputStream() {
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
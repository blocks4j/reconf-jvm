package org.blocks4j.reconf.client.setup.config.parser;

import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PropertiesFileParser {

    private InputStream inputStream;
    private String prefix;

    private PropertiesFileParser(InputStream inputStream) {
        this.inputStream = inputStream;
        this.prefix = StringUtils.EMPTY;
    }

    public static PropertiesFileParser forInputStream(InputStream inputStream) {
        return new PropertiesFileParser(inputStream);
    }

    public PropertiesFileParser withPrefix(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            this.prefix = prefix;
        }
        return this;
    }

    public ReconfConfiguration parse() {
        Properties content = new Properties();
        try {
            content.load(this.inputStream);

            Map<String, String> properties = new LinkedHashMap<>();

            for (Map.Entry<Object, Object> entry : content.entrySet()) {
                Object entryValue = entry.getValue();
                String value = entryValue == null ? "" : entryValue.toString();
                properties.put(entry.getKey().toString().toLowerCase(Locale.ENGLISH), value);
            }

            ReconfConfiguration reconfConfiguration = buildSingleProperties(properties);
            reconfConfiguration.setLocalCacheSettings(buildLocalCacheSettings(properties));
            reconfConfiguration.setConnectionSettings(buildConnectionSettings(properties));

            return reconfConfiguration;
        } catch (Exception e) {
            throw new ReConfInitializationError("error parsing the configuration file with content" + LineSeparator.value() + content, e);
        }
    }

    private ConnectionSettings buildConnectionSettings(Map<String, String> properties) {
        ConnectionSettings connectionSettings = new ConnectionSettings();

        if (properties.containsKey(this.prefix + "server.url")) {
            connectionSettings.setUrl(properties.get(this.prefix + "server.url"));
        }
        if (properties.containsKey(this.prefix + "server.timeout")) {
            connectionSettings.setTimeout(asInteger(properties.get(this.prefix + "server.timeout")));
        }
        if (properties.containsKey(this.prefix + "server.time-unit")) {
            connectionSettings.setTimeUnit(asTimeUnit(properties.get(this.prefix + "server.time-unit")));
        }
        if (properties.containsKey(this.prefix + "server.max-retry")) {
            connectionSettings.setMaxRetry(asInteger(properties.get(this.prefix + "server.max-retry")));
        }

        return connectionSettings;
    }

    private LocalCacheSettings buildLocalCacheSettings(Map<String, String> properties) {
        LocalCacheSettings localCacheSettings = new LocalCacheSettings();

        if (properties.containsKey(this.prefix + "local.cache.location")) {
            File file = null;
            try {
                file = new File(properties.get(this.prefix + "local.cache.location"));
            } catch (Exception ignored) {
            }
            localCacheSettings.setBackupLocation(file);
        }
        if (properties.containsKey(this.prefix + "local.cache.max-log-file-size-mb")) {
            localCacheSettings.setMaxLogFileSize(asInteger(properties.get(this.prefix + "local.cache.max-log-file-size-mb")));
        }
        if (properties.containsKey(this.prefix + "local.cache.compressed")) {
            localCacheSettings.setCompressed(asBoolean(properties.get(this.prefix + "local.cache.compressed"), true));
        }

        return localCacheSettings;
    }


    private ReconfConfiguration buildSingleProperties(Map<String, String> properties) {
        ReconfConfiguration reconfConfiguration = new ReconfConfiguration();

        if (properties.containsKey(this.prefix + "locale")) {
            reconfConfiguration.setLocale(properties.get(this.prefix + "locale"));
        }
        if (properties.containsKey(this.prefix + "debug")) {
            reconfConfiguration.setDebug(asBoolean(properties.get(this.prefix + "debug"), false));
        }
        if (properties.containsKey(this.prefix + "experimental")) {
            reconfConfiguration.setExperimentalFeatures(asBoolean(properties.get(this.prefix + "experimental"), false));
        }

        return reconfConfiguration;
    }

    private Boolean asBoolean(String arg, boolean returnIfNull) {
        try {
            return Boolean.valueOf(arg);
        } catch (Exception ignored) {
        }
        return returnIfNull;
    }

    private Integer asInteger(String arg) {
        Integer integer = null;
        try {
            integer = Integer.valueOf(arg);
        } catch (Exception ignored) {
        }
        return integer;
    }

    private TimeUnit asTimeUnit(String arg) {
        TimeUnit timeUnit = null;
        try {
            timeUnit = TimeUnit.valueOf(arg);
        } catch (Exception ignored) {
        }
        return timeUnit;
    }

}

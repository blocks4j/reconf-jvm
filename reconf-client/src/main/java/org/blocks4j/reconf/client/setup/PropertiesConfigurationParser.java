/*
 *   Copyright 2013-2015 Blocks4J Team (www.blocks4j.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.blocks4j.reconf.client.setup;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;


public class PropertiesConfigurationParser {

    private Map<String, String> properties = new LinkedHashMap<String, String>();
    private String locale;
    private boolean experimentalFeatures;
    private boolean debugEnabled;
    private LocalCacheSettings localCacheSettings = new LocalCacheSettings();
    private ConnectionSettings connectionSettings = new ConnectionSettings();

    public PropertiesConfigurationParser(Properties content) {
        try {
            for (Entry<Object, Object> entry : content.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                String value = entry.getValue() == null ? "" : entry.getValue().toString();
                properties.put(entry.getKey().toString().toLowerCase(Locale.ENGLISH), value);
            }
            init();
            buildLocalCacheSettings();
            buildConnectionSettings();

        } catch (Exception e) {
            throw new ReConfInitializationError("error parsing the configuration file with content" + LineSeparator.value() + content, e);
        }
    }

    private void init() {
        if (properties.containsKey("locale")) {
            locale = properties.get("locale");
        }
        if (properties.containsKey("debug")) {
            debugEnabled = asBoolean(properties.get("debug"), false);
        }
        if (properties.containsKey("experimental")) {
            experimentalFeatures = asBoolean(properties.get("experimental"), false);
        }
    }

    private void buildLocalCacheSettings() {
        if (properties.containsKey("local.cache.location")) {
            File file = null;
            try {
                file = new File(properties.get("local.cache.location"));
            } catch (Exception ignored) { }
            localCacheSettings.setBackupLocation(file);
        }
        if (properties.containsKey("local.cache.max-log-file-size-mb")) {
            localCacheSettings.setMaxLogFileSize(asInteger(properties.get("local.cache.max-log-file-size-mb")));
        }
        if (properties.containsKey("local.cache.compressed")) {
            localCacheSettings.setCompressed(asBoolean(properties.get("local.cache.compressed"), true));
        }
    }

    private void buildConnectionSettings() {
        if (properties.containsKey("server.url")) {
            connectionSettings.setUrl(properties.get("server.url"));
        }
        if (properties.containsKey("server.timeout")) {
            connectionSettings.setTimeout(asInteger(properties.get("server.timeout")));
        }
        if (properties.containsKey("server.time-unit")) {
            connectionSettings.setTimeUnit(asTimeUnit(properties.get("server.time-unit")));
        }
        if (properties.containsKey("server.max-retry")) {
            connectionSettings.setMaxRetry(asInteger(properties.get("server.max-retry")));
        }
    }

    private Integer asInteger(String arg) {
        Integer integer = null;
        try {
            integer = Integer.valueOf(arg);
        } catch (Exception ignored) {}
        return integer;
    }

    private TimeUnit asTimeUnit(String arg) {
        TimeUnit timeUnit = null;
        try {
            timeUnit = TimeUnit.valueOf(arg);
        } catch (Exception ignored) {}
        return timeUnit;
    }

    private Boolean asBoolean(String arg, boolean returnIfNull) {
        try {
            return Boolean.valueOf(arg);
        } catch (Exception ignored) { }
        return returnIfNull;
    }

    public LocalCacheSettings getLocalCacheSettings() {
        return localCacheSettings;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isExperimentalFeatures() {
        return experimentalFeatures;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}

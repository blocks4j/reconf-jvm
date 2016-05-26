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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.check.ObservableThread;
import org.blocks4j.reconf.client.check.ObserverThread;
import org.blocks4j.reconf.client.config.update.ConfigurationRepositoryUpdater;
import org.blocks4j.reconf.client.factory.ConfigurationRepositoryElementFactory;
import org.blocks4j.reconf.client.validation.PropertiesConfigurationValidator;
import org.blocks4j.reconf.infra.i18n.LocaleHolder;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.io.ClasspathInputStream;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.system.LocalHostname;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class Environment {

    private static final String RECONF_DEFAULT_FILE = "reconf.properties";
    public static final String SYSTEM_PROPERTY = "reconf.client.file.location";
    public static final String SYSTEM_PROPERTY_PREFIX = "reconf.client.file.properties.prefix";

    private static final PropertiesConfiguration config;
    private static final ConfigurationRepositoryElementFactory factory;
    private static DatabaseManager mgr;
    private static MessagesBundle msg;
    private static ObserverThread checker;

    static {
        try {
            Properties raw = new Properties();

            String prop = System.getProperty(SYSTEM_PROPERTY);
            if (StringUtils.isNotBlank(prop)) {
                loadUserDefinedProperties(raw, prop);
            }

            if (raw.isEmpty()) {
                System.clearProperty(SYSTEM_PROPERTY_PREFIX);
                LoggerHolder.getLog().info(String.format("trying to read file [%s] from classpath", RECONF_DEFAULT_FILE));
                InputStream defaultFileIS = ClasspathInputStream.from(RECONF_DEFAULT_FILE);

                if (defaultFileIS != null) {
                    raw.load(defaultFileIS);
                }
            }
            if (raw.isEmpty()) {
                throw new ReConfInitializationError("configuration file is either empty or could not be found");
            }

            String prefix = System.getProperty(SYSTEM_PROPERTY_PREFIX);
            PropertiesConfigurationParser parser = new PropertiesConfigurationParser(raw, prefix);
            LocaleHolder.set(parser.getLocale());

            config = new PropertiesConfiguration();
            config.setConnectionSettings(parser.getConnectionSettings());
            config.setLocalCacheSettings(parser.getLocalCacheSettings());
            config.setDebug(parser.isDebugEnabled());
            config.setExperimentalFeatures(parser.isExperimentalFeatures());

            msg = MessagesBundle.getBundle(Environment.class);
            LoggerHolder.getLog().info(msg.get("file.load"));

            validate(config);
            LoggerHolder.getLog().info(msg.format("configured", LineSeparator.value(), config.toString()));

            factory = new ConfigurationRepositoryElementFactory(config);
            LoggerHolder.getLog().info(msg.get("db.setup"));
            mgr = new DatabaseManager(config.getLocalCacheSettings());

            LoggerHolder.getLog().info(msg.format("instance.name", LocalHostname.getName()));
            checker = new ObserverThread();
            checker.start();

            if (config.isExperimentalFeatures()) {
                LoggerHolder.getLog().info(msg.format("experimental.features.enabled", LocalHostname.getName()));
                LoggerHolder.getLog().info(msg.format("experimental.features.none"));
            }

        } catch (ReConfInitializationError e) {
            if (mgr != null) {
                mgr.shutdown();
            }
            throw e;

        } catch (Throwable t) {
            if (mgr != null) {
                mgr.shutdown();
            }
            throw new ReConfInitializationError(t);
        }
    }

    private static void loadUserDefinedProperties(Properties raw, String userDefinedPropertyFileLocation) throws IOException {
        LoggerHolder.getLog().info(String.format("system property [%s] found. trying to read file [%s]", SYSTEM_PROPERTY, userDefinedPropertyFileLocation));
        InputStream userDefinedProperties = ClasspathInputStream.from(userDefinedPropertyFileLocation);
        if (userDefinedProperties == null) {
            File file = new File(userDefinedPropertyFileLocation);
            if (file.exists()) {
                userDefinedProperties = new FileInputStream(file);
            } else {
                LoggerHolder.getLog().info(String.format("file [%s] not found.", userDefinedPropertyFileLocation));
            }
        }

        if (userDefinedProperties != null) {
            raw.load(userDefinedProperties);
        }
    }

    private static void validate(PropertiesConfiguration configuration) {
        if (configuration == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }
        Set<String> violations = PropertiesConfigurationValidator.validate(configuration);
        if (CollectionUtils.isEmpty(violations)) {
            return;
        }
        List<String> errors = new ArrayList<>();
        int i = 1;
        for (String violation : violations) {
            errors.add(i++ + " - " + violation);
        }

        if (configuration.isDebug()) {
            LoggerHolder.getLog().error(msg.format("error.config", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())));
        } else {
            throw new ReConfInitializationError(msg.format("error.config", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())));
        }

    }

    public static void setUp() {
        LoggerHolder.getLog().info(msg.get("start"));
    }

    public static DatabaseManager getManager() {
        return mgr;
    }

    public static ConfigurationRepositoryElementFactory getFactory() {
        return factory;
    }

    public static void addThreadToCheck(ObservableThread thread) {
        if (checker != null) {
            checker.add(thread);
        }
    }

    public static List<SyncResult> syncActiveConfigurationRepositoryUpdaters() {
        List<ObservableThread> toSync = checker.getActiveThreads();
        List<SyncResult> result = new ArrayList<>();

        for (ObservableThread thread : toSync) {
            if (!(thread instanceof ConfigurationRepositoryUpdater)) {
                continue;
            }
            ConfigurationRepositoryUpdater updater = (ConfigurationRepositoryUpdater) thread;

            try {
                updater.syncNow(RuntimeException.class);
                result.add(new SyncResult(updater.getName()));

            } catch (Exception e) {
                result.add(new SyncResult(updater.getName(), e));
            }
        }
        return result;
    }
}

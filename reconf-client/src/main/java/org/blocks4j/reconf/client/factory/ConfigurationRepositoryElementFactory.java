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
package org.blocks4j.reconf.client.factory;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.annotations.ConfigurationRepository;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.client.validation.ConfigurationRepositoryElementValidator;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ConfigurationRepositoryElementFactory {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryElementFactory.class);
    private ReconfConfiguration configuration;

    public ConfigurationRepositoryElementFactory(ReconfConfiguration configuration) {
        this.configuration = configuration;
    }

    public ConfigurationRepositoryElement create(Class<?> arg) {
        ConfigurationRepositoryElement result = createNewRepositoryFor(arg);
        validate(result);
        return result;
    }

    private ConfigurationRepositoryElement createNewRepositoryFor(Class<?> arg) {
        if (!arg.isInterface()) {
            throw new ReConfInitializationError(msg.format("error.is.not.interface", arg.getCanonicalName()));
        }

        if (!arg.isAnnotationPresent(ConfigurationRepository.class)) {
            return null;
        }

        ConfigurationRepository ann = arg.getAnnotation(ConfigurationRepository.class);
        ConfigurationRepositoryElement result = new ConfigurationRepositoryElement();
        result.setProduct(ann.product());
        result.setComponent(ann.component());
        result.setConnectionSettings(configuration.getConnectionSettings());
        result.setInterfaceClass(arg);
        result.setRate(ann.pollingRate());
        result.setTimeUnit(ann.pollingTimeUnit());
        result.setConfigurationItems(ConfigurationItemElement.from(result));
        return result;
    }

    private void validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }

        Map<String, String> violations = ConfigurationRepositoryElementValidator.validate(arg);
        if (MapUtils.isEmpty(violations)) {
            return;
        }

        List<String> errors = new ArrayList<>();
        int i = 1;
        for (Entry<String, String> violation : violations.entrySet()) {
            errors.add(i++ + " - " + violation.getValue() + " @ " + StringUtils.replace(arg.getInterfaceClass().toString(), "interface ", "") + "." + violation.getKey());
        }

        if (configuration.isDebug()) {
            LoggerHolder.getLog().error(msg.format("error.factory", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())) + LineSeparator.value());
        } else {
            throw new ReConfInitializationError(msg.format("error.factory", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())) + LineSeparator.value());
        }
    }
}

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
package org.blocks4j.reconf.client.validation;

import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.validation.ConnectionSettingsValidator;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigurationRepositoryElementValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryElement.class);

    public static Map<String, String> validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            return Collections.emptyMap();
        }

        Map<String, String> errors = new LinkedHashMap<String, String>();

        checkConnectionSettings(arg, errors);
        checkComponent(arg, errors);
        checkProduct(arg, errors);
        checkUpdateFrequency(arg, errors);
        checkInterfaceClass(arg, errors);
        checkConfigurationItemElements(arg, errors);
        return errors;
    }

    private static void checkConnectionSettings(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        for (String error : ConnectionSettingsValidator.validate(arg.getConnectionSettings())) {
            errors.put("connectionSettings", error);
        }
    }

    private static void checkComponent(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        if (StringUtils.isEmpty(arg.getComponent())) {
            errors.put("@ConfigurationRepository", msg.get("error.component"));
        }
    }

    private static void checkProduct(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        if (StringUtils.isEmpty(arg.getProduct())) {
            errors.put("@ConfigurationRepository", msg.get("error.product"));
        }
    }

    private static void checkUpdateFrequency(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        if (arg.getRate() == null || arg.getRate() < 1) {
            errors.put("@ConfigurationRepository", msg.get("rate.error"));
        }

        if (arg.getTimeUnit() == null || !EnumSet.of(TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS).contains(arg.getTimeUnit())) {
            errors.put("@ConfigurationRepository", msg.get("timeUnit.null"));
        }
    }

    private static void checkInterfaceClass(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        if (arg.getInterfaceClass() == null) {
            errors.put("interfaceClass", "is null");
        }
    }

    private static void checkConfigurationItemElements(ConfigurationRepositoryElement arg, Map<String, String> errors) {
        if (arg.getConfigurationItems() == null) {
            return;
        }
        for (int i = 0; i < arg.getConfigurationItems().size(); i++) {
            errors.putAll(ConfigurationItemElementValidator.validate(i, arg.getConfigurationItems().get(i)));
        }
    }
}

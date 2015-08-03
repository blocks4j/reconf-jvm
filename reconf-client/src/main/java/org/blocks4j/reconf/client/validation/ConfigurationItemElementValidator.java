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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

public class ConfigurationItemElementValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemElement.class);

    public static Map<String, String> validate(int pos, ConfigurationItemElement arg) {
        if (arg == null) {
            return Collections.EMPTY_MAP;
        }

        Map<String, String> errors = new LinkedHashMap<String, String>();
        String prefix = getPrefix(pos);
        checkMethodName(prefix, arg, errors);
        checkValue(prefix, arg, errors);
        checkAdapter(prefix, arg, errors);
        checkMethod(prefix, arg, errors);
        return errors;
    }

    private static void checkMethodName(String prefix, ConfigurationItemElement arg, Map<String, String> errors) {
        if (arg.getMethodName() == null) {
            errors.put(prefix + "methodName", "is null");
        }
        if (arg.getMethodName() != null && StringUtils.isEmpty(arg.getMethodName())) {
            errors.put(prefix + "methodName", "is empty");
        }
    }

    private static void checkValue(String prefix, ConfigurationItemElement arg, Map<String, String> errors) {
        if (arg.getValue() == null) {
            errors.put(prefix + "@ConfigurationItem", msg.get("error.value"));
        }
        if (arg.getValue() != null && StringUtils.isEmpty(arg.getValue())) {
            errors.put(prefix + "@ConfigurationItem", msg.get("error.value"));
        }
    }

    private static void checkAdapter(String prefix, ConfigurationItemElement arg, Map<String, String> errors) {
        if (arg.getAdapter() == null) {
            errors.put(prefix + "@ConfigurationItem", msg.get("adapter.null"));
        }
    }

    private static void checkMethod(String prefix, ConfigurationItemElement arg, Map<String, String> errors) {
        if (arg.getMethod() == null) {
            errors.put(prefix + "method", "is null");
        }
    }

    private static String getPrefix(int pos) {
        return "ConfigurationItem[" + pos + "].";
    }
}

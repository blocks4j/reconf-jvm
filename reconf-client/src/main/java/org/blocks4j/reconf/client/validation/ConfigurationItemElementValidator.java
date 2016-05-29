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

import com.google.common.primitives.Primitives;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.adapter.ConfigurationAdapter;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigurationItemElementValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemElement.class);

    public static Map<String, String> validate(int pos, ConfigurationItemElement arg) {
        if (arg == null) {
            return Collections.emptyMap();
        }

        Map<String, String> errors = new LinkedHashMap<>();
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
        Class<? extends ConfigurationAdapter> adapter = arg.getAdapter();
        if (adapter == null) {
            errors.put(prefix + "@ConfigurationItem", msg.get("error.adapter.null"));
        } else {
            if (isInvalidAdapterForReturnType(adapter, arg.getMethod().getReturnType())) {
                errors.put(prefix + "@ConfigurationItem", msg.get("error.adapter.incompatible.type"));
            }
        }
    }

    private static boolean isInvalidAdapterForReturnType(Class<? extends ConfigurationAdapter> adapter, Class<?> returnType) {
        try {
            Method interfaceMethod = getInterfaceAdapterMethod();
            Method adapterMethod = adapter.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());

            Class<?> desiredType = Primitives.wrap(returnType);

            return !(desiredType.isAssignableFrom(adapterMethod.getReturnType()) ||
                    adapterMethod.getReturnType().isAssignableFrom(desiredType));
        } catch (Exception exception) {
            return true;
        }
    }

    private static Method getInterfaceAdapterMethod() {
        return ConfigurationAdapter.class.getMethods()[0];
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

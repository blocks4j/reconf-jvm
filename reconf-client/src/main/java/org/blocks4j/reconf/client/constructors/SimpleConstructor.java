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
package org.blocks4j.reconf.client.constructors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;


public class SimpleConstructor implements ObjectConstructor {

    private static final MessagesBundle msg = MessagesBundle.getBundle(SimpleConstructor.class);
    private static final Map<Class<?>,Class<?>> primitiveBoxing = new HashMap<Class<?>,Class<?>>() {
        private static final long serialVersionUID = 1L;
        {
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(byte.class, Byte.class);
            put(short.class, Short.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(boolean.class, Boolean.class);
        }
    };

    public Object construct(MethodData data) throws Throwable {
        if (data.hasAdapter()) {
            return data.getAdapter().adapt(data.getReturnType(), data.getValue());
        }

        Class<?> returnClass = (Class<?>) data.getReturnType();

        String trimmed = StringUtils.defaultString(StringUtils.trim(data.getValue()));
        if (!trimmed.startsWith("'") || !trimmed.endsWith("'")) {
            throw new RuntimeException(msg.format("error.invalid.string", data.getValue(), data.getMethod()));
        }

        String wholeValue = StringUtils.substring(trimmed, 1, trimmed.length()-1);

        if (String.class.equals(returnClass)) {
            return wholeValue;
        }

        if (null == data.getValue()) {
            return null;
        }

        if (Object.class.equals(returnClass)) {
            returnClass = String.class;
        }

        if (char.class.equals(data.getReturnType()) || Character.class.equals(data.getReturnType())) {
            if (StringUtils.length(wholeValue) == 1) {
                return CharUtils.toChar(wholeValue);
            }
            return CharUtils.toChar(StringUtils.replace(wholeValue, " ", ""));
        }

        if (primitiveBoxing.containsKey(returnClass)) {
            if (StringUtils.isBlank(wholeValue)) {
                return null;
            }
            Method parser = primitiveBoxing.get(returnClass).getMethod("parse" + StringUtils.capitalize(returnClass.getSimpleName()), new Class<?>[] {String.class});
            return parser.invoke(primitiveBoxing.get(returnClass), new Object[] { StringUtils.trim(wholeValue) });
        }

        Method valueOf = null;
        try {
            valueOf = returnClass.getMethod("valueOf", new Class<?>[] {String.class});
        } catch (NoSuchMethodException ignored) {}

        if (valueOf == null) {
            try {

                valueOf = returnClass.getMethod("valueOf", new Class<?>[] {Object.class});
            } catch (NoSuchMethodException ignored) {}
        }

        if (null != valueOf) {
            if (StringUtils.isEmpty(wholeValue)) {
                return null;
            }
            return valueOf.invoke(data.getReturnType(), new Object[] { StringUtils.trim(wholeValue) });
        }

        Constructor<?> constructor = null;

        try {
            constructor = returnClass.getConstructor(String.class);
            constructor.setAccessible(true);

        } catch (NoSuchMethodException ignored) {
            throw new IllegalStateException(msg.format("error.string.constructor", returnClass.getSimpleName(), data.getMethod()));
        }

        try {
            return constructor.newInstance(wholeValue);

        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof NumberFormatException) {
                return constructor.newInstance(StringUtils.trim(wholeValue));
            }
            throw e;
        }
    }
}
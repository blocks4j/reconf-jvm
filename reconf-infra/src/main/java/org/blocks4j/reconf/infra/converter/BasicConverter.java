package org.blocks4j.reconf.infra.converter;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BasicConverter {

    private static final MessagesBundle msg = MessagesBundle.getBundle(BasicConverter.class);
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

    public BasicConverter() {

    }

    public Object convertFromString(Class<?> finalClass, String rawValue) throws Exception {
        if (String.class.equals(finalClass)) {
            return rawValue;
        }

        if (null == rawValue) {
            return null;
        }

        if (Object.class.equals(finalClass)) {
            finalClass = String.class;
        }

        if (char.class.equals(finalClass) || Character.class.equals(finalClass)) {
            if (StringUtils.length(rawValue) == 1) {
                return CharUtils.toChar(rawValue);
            }
            return CharUtils.toChar(StringUtils.replace(rawValue, " ", ""));
        }

        if (primitiveBoxing.containsKey(finalClass)) {
            if (StringUtils.isBlank(rawValue)) {
                return null;
            }
            Method parser = primitiveBoxing.get(finalClass).getMethod("parse" + StringUtils.capitalize(finalClass.getSimpleName()), String.class);
            return parser.invoke(primitiveBoxing.get(finalClass), StringUtils.trim(rawValue));
        }

        Method valueOf = null;
        try {
            valueOf = finalClass.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException ignored) {}

        if (valueOf == null) {
            try {

                valueOf = finalClass.getMethod("valueOf", Object.class);
            } catch (NoSuchMethodException ignored) {}
        }

        if (null != valueOf) {
            if (StringUtils.isEmpty(rawValue)) {
                return null;
            }
            return valueOf.invoke(finalClass, StringUtils.trim(rawValue));
        }

        Constructor<?> constructor;

        try {
            constructor = finalClass.getConstructor(String.class);
            constructor.setAccessible(true);

        } catch (NoSuchMethodException ignored) {
            throw new IllegalStateException(msg.format("error.string.constructor", finalClass.getSimpleName()));
        }

        try {
            return constructor.newInstance(rawValue);

        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof NumberFormatException) {
                return constructor.newInstance(StringUtils.trim(rawValue));
            }
            throw e;
        }
    }

}

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.factory.ObjectConstructorFactory;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

public class MapConstructor implements ObjectConstructor {

    private static final MessagesBundle msg = MessagesBundle.getBundle(MapConstructor.class);

    public Object construct(MethodData data) throws Throwable {

        if (data.hasAdapter()) {
            return data.getAdapter().adapt(data.getReturnType(), data.getValue());
        }

        Class<?> returnClass = null;
        Type keyType = null;
        Type valueType = null;

        if (data.getReturnType() instanceof ParameterizedType){
            ParameterizedType parameterized = (ParameterizedType) data.getReturnType();
            returnClass = (Class<?>) parameterized.getRawType();

            if (parameterized.getActualTypeArguments().length == 1) {
                Type first = parameterized.getActualTypeArguments()[0];
                if (returnClass.getGenericSuperclass() != null && returnClass.getGenericSuperclass() instanceof ParameterizedType) {
                    parameterized = (ParameterizedType) returnClass.getGenericSuperclass();
                    if (parameterized.getActualTypeArguments().length != 2) {
                        throw new IllegalArgumentException(msg.format("error.cant.build.type", data.getReturnType(), data.getMethod()));
                    }
                    if (parameterized.getActualTypeArguments()[0] instanceof TypeVariable) {
                        keyType = first;
                        valueType = parameterized.getActualTypeArguments()[1];

                    } else if (parameterized.getActualTypeArguments()[1] instanceof TypeVariable) {
                        valueType = first;
                        keyType = parameterized.getActualTypeArguments()[0];

                    } else {
                        throw new IllegalArgumentException(msg.format("error.cant.build.type", data.getReturnType(), data.getMethod()));
                    }
                }

            } else {
                keyType = parameterized.getActualTypeArguments()[0];
                valueType = parameterized.getActualTypeArguments()[1];
            }

        } else if (data.getReturnType() instanceof Class) {
            returnClass = (Class<?>) data.getReturnType();

            if (returnClass.getGenericSuperclass() != null && returnClass.getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) returnClass.getGenericSuperclass();
                if (parameterized.getActualTypeArguments().length != 2) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", data.getReturnType(), data.getMethod()));
                }
                keyType = parameterized.getActualTypeArguments()[0];
                valueType = parameterized.getActualTypeArguments()[1];

            } else {
                keyType = Object.class;
                valueType = Object.class;
            }

        } else {
            throw new IllegalArgumentException(msg.format("error.return", data.getMethod()));
        }

        if (returnClass.isInterface()) {
            returnClass = getDefaultImplementation(data, returnClass);
        }

        Constructor<?> constructor = returnClass.getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
        Map<Object, Object> mapInstance = (Map<Object,Object>) constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);

        if (null == data.getValue() || StringUtils.isEmpty(data.getValue())) {
            return mapInstance;
        }

        if ((!(keyType instanceof Class)) || (!StringUtils.startsWith(data.getValue(), "[") || !StringUtils.endsWith(data.getValue(), "]"))) {
            throw new IllegalArgumentException(msg.format("error.build", data.getValue(), data.getMethod()));
        }

        StringParser parser = new StringParser(data);
        for (Entry<String, String> each : parser.getTokensAsMap().entrySet()) {
            Object value = ObjectConstructorFactory.get(valueType).construct(new MethodData(data.getMethod(), valueType, each.getValue()));
            mapInstance.put(ObjectConstructorFactory.get(keyType).construct(new MethodData(data.getMethod(), keyType, each.getKey())), value);
        }

        return mapInstance;
    }

    private Class<?> getDefaultImplementation(MethodData data, Class<?> returnClass) {
        if (Map.class.equals(returnClass)) {
            return HashMap.class;
        }
        if (ConcurrentMap.class.equals(returnClass)) {
            return ConcurrentHashMap.class;
        }
        if (ConcurrentNavigableMap.class.equals(returnClass)) {
            return ConcurrentSkipListMap.class;
        }
        if (NavigableMap.class.equals(returnClass) || SortedMap.class.equals(returnClass)) {
            return TreeMap.class;
        }
        throw new UnsupportedOperationException(msg.format("error.implementation", returnClass, data.getMethod()));
    }
}

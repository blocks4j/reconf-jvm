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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.lang3.ArrayUtils;
import org.blocks4j.reconf.client.factory.ObjectConstructorFactory;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;


public class CollectionConstructor implements ObjectConstructor {

    private static final MessagesBundle msg = MessagesBundle.getBundle(CollectionConstructor.class);

    public Object construct(MethodData data) throws Throwable {

        if (data.hasAdapter()) {
            return data.getAdapter().adapt(data.getReturnType(), data.getValue());
        }

        Class<?> returnClass = null;
        Type innerClass = null;

        if (data.getReturnType() instanceof ParameterizedType){
            ParameterizedType parameterized = (ParameterizedType) data.getReturnType();
            returnClass = (Class<?>) parameterized.getRawType();

            if (parameterized.getActualTypeArguments()[0] instanceof ParameterizedType) {
                innerClass = parameterized.getActualTypeArguments()[0];

            } else if (parameterized.getActualTypeArguments()[0] instanceof Class<?>) {
                innerClass = parameterized.getActualTypeArguments()[0];
            }
        } else if (data.getReturnType() instanceof Class) {
            returnClass = (Class<?>) data.getReturnType();

            if (returnClass.getGenericSuperclass() != null && returnClass.getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) returnClass.getGenericSuperclass();
                if (parameterized.getActualTypeArguments().length != 1) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", data.getReturnType(), data.getMethod()));
                }
                if (parameterized.getActualTypeArguments()[0] instanceof TypeVariable) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", data.getReturnType(), data.getMethod()));
                } else {
                    innerClass = parameterized.getActualTypeArguments()[0];
                }

            } else {
                innerClass = Object.class;
            }

        } else {
            throw new IllegalArgumentException(msg.format("error.return", data.getMethod()));
        }

        if (returnClass.isInterface()) {
            returnClass = getDefaultImplementation(data, returnClass);
        }

        Constructor<?> constructor = returnClass.getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
        Collection<Object> collectionInstance = (Collection<Object>) constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);

        if (null == data.getValue()) {
            return collectionInstance;
        }

        for (String s : new StringParser(data).getTokens()) {
            Object o = ObjectConstructorFactory.get(innerClass).construct(new MethodData(data.getMethod(), innerClass, s));
            if (o != null) {
                collectionInstance.add(o);
            }
        }

        return collectionInstance;
    }

    private Class<?> getDefaultImplementation(MethodData data, Class<?> returnClass) {
        if (Collection.class.equals(returnClass)) {
            return ArrayList.class;
        }
        if (List.class.equals(returnClass)) {
            return ArrayList.class;
        }
        if (Set.class.equals(returnClass)) {
            return HashSet.class;
        }
        if (SortedSet.class.equals(returnClass) || NavigableSet.class.equals(returnClass)) {
            return TreeSet.class;
        }
        if (Queue.class.equals(returnClass)) {
            return LinkedList.class;
        }
        if (BlockingQueue.class.equals(returnClass)) {
            return ArrayBlockingQueue.class;
        }
        if (BlockingDeque.class.equals(returnClass)) {
            return LinkedBlockingDeque.class;
        }
        throw new UnsupportedOperationException(msg.format("error.implementation", returnClass, data.getMethod()));
    }
}

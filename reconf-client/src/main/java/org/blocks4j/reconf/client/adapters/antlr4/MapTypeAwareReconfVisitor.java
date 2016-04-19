package org.blocks4j.reconf.client.adapters.antlr4;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;


public class MapTypeAwareReconfVisitor extends ReconfBaseVisitor<Map<Object, Object>> {

    private static final MessagesBundle msg = MessagesBundle.getBundle(MapTypeAwareReconfVisitor.class);

    private Type keyType;
    private Type valueType;

    private Class<?> mapClass;

    private Map<Object, Object> aggregationCollection;

    public MapTypeAwareReconfVisitor(Type baseType) {
        this.loadCollectionInformation(baseType);
    }

    @Override
    public Map<Object, Object> visitMapEntry(ReconfParser.MapEntryContext ctx) {
        Map<Object, Object> entry = new HashMap<Object, Object>();

        Object key = new TypeAwareReconfVisitor(keyType).visitPrimitive(ctx.primitive());
        Object value = new TypeAwareReconfVisitor(valueType).visitValue(ctx.value());

        entry.put(key, value);

        return entry;
    }

    @Override
    public Map<Object, Object> visitMap(ReconfParser.MapContext ctx) {
        this.aggregationCollection = this.createMap();
        Map<Object, Object> map = super.visitMap(ctx);
        if (map == null) {
            map = this.aggregationCollection;
        }
        return map;
    }

    @Override
    protected Map<Object, Object> aggregateResult(Map<Object, Object> aggregate, Map<Object, Object> nextResult) {
        if (aggregate == null) {
            aggregate = this.aggregationCollection;
        }

        if (nextResult != null) {
            aggregate.putAll(nextResult);
        }

        return aggregate;
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> createMap() {
        try {
            Constructor<?> constructor = this.mapClass.getConstructor();
            return (Map<Object, Object>) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<Object, Object> visitValue(ReconfParser.ValueContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Map<Object, Object> visitStructure(ReconfParser.StructureContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Map<Object, Object> visitCollection(ReconfParser.CollectionContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Map<Object, Object> visitPrimitive(ReconfParser.PrimitiveContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Map<Object, Object> visitErrorNode(ErrorNode node) {
        throw new IllegalArgumentException();
    }

    private void loadCollectionInformation(Type baseType) {
        if (baseType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) baseType;
            mapClass = (Class<?>) parameterizedType.getRawType();

            if (parameterizedType.getActualTypeArguments().length == 1) {
                Type first = parameterizedType.getActualTypeArguments()[0];
                if (mapClass.getGenericSuperclass() != null && mapClass.getGenericSuperclass() instanceof ParameterizedType) {
                    parameterizedType = (ParameterizedType) mapClass.getGenericSuperclass();
                    if (parameterizedType.getActualTypeArguments().length != 2) {
                        throw new IllegalArgumentException(msg.format("error.cant.build.type", baseType));
                    }
                    if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable) {
                        keyType = first;
                        valueType = parameterizedType.getActualTypeArguments()[1];

                    } else if (parameterizedType.getActualTypeArguments()[1] instanceof TypeVariable) {
                        valueType = first;
                        keyType = parameterizedType.getActualTypeArguments()[0];

                    } else {
                        throw new IllegalArgumentException(msg.format("error.cant.build.type", baseType));
                    }
                }

            } else {
                keyType = parameterizedType.getActualTypeArguments()[0];
                valueType = parameterizedType.getActualTypeArguments()[1];
            }

        } else if (baseType instanceof Class) {
            mapClass = (Class<?>) baseType;

            if (mapClass.getGenericSuperclass() != null && mapClass.getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) mapClass.getGenericSuperclass();
                if (parameterizedType.getActualTypeArguments().length != 2) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", baseType));
                }
                keyType = parameterizedType.getActualTypeArguments()[0];
                valueType = parameterizedType.getActualTypeArguments()[1];

            } else {
                keyType = Object.class;
                valueType = Object.class;
            }

        } else {
            throw new IllegalArgumentException(msg.format("error.return", baseType));
        }

        if (mapClass.isInterface()) {
            mapClass = getDefaultImplementation(mapClass);
        }
    }

    private Class<?> getDefaultImplementation(Class<?> clazz) {
        if (Map.class.equals(clazz)) {
            return HashMap.class;
        }
        if (ConcurrentMap.class.equals(clazz)) {
            return ConcurrentHashMap.class;
        }
        if (ConcurrentNavigableMap.class.equals(clazz)) {
            return ConcurrentSkipListMap.class;
        }
        if (NavigableMap.class.equals(clazz) || SortedMap.class.equals(clazz)) {
            return TreeMap.class;
        }
        throw new UnsupportedOperationException(msg.format("error.implementation", clazz));
    }
}

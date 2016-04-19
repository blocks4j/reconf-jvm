package org.blocks4j.reconf.client.adapters.antlr4;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class CollectionTypeAwareReconfVisitor extends ReconfBaseVisitor<Collection<Object>> {

    private static final MessagesBundle msg = MessagesBundle.getBundle(CollectionTypeAwareReconfVisitor.class);

    private Type itemType;
    private Class<?> collectionClass;

    private Collection<Object> aggregationCollection;

    public CollectionTypeAwareReconfVisitor(Type baseType) {
        this.loadCollectionInformation(baseType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> visitValue(ReconfParser.ValueContext ctx) {
        return Collections.singleton(new TypeAwareReconfVisitor(this.itemType).visitValue(ctx));
    }

    @Override
    public Collection<Object> visitCollection(ReconfParser.CollectionContext ctx) {
        this.aggregationCollection = this.createCollection();

        Collection<Object> objects = super.visitCollection(ctx);
        if (objects == null) {
            objects = this.aggregationCollection;
        }
        return objects;
    }

    @Override
    protected Collection<Object> aggregateResult(Collection<Object> aggregate, Collection<Object> nextResult) {
        if (aggregate == null) {
            aggregate = this.aggregationCollection;
        }

        if (nextResult != null) {
            aggregate.addAll(nextResult);
        }

        return aggregate;
    }

    @Override
    public Collection<Object> visitStructure(ReconfParser.StructureContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Collection<Object> visitMap(ReconfParser.MapContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Collection<Object> visitMapEntry(ReconfParser.MapEntryContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Collection<Object> visitPrimitive(ReconfParser.PrimitiveContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Collection<Object> visitErrorNode(ErrorNode node) {
        throw new IllegalArgumentException();
    }


    @SuppressWarnings("unchecked")
    private Collection<Object> createCollection() {
        try {
            Constructor<?> constructor = this.collectionClass.getConstructor();
            return (Collection<Object>) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadCollectionInformation(Type baseType) {

        if (baseType instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) baseType;
            this.collectionClass = (Class<?>) parameterized.getRawType();

            if (parameterized.getActualTypeArguments()[0] instanceof ParameterizedType) {
                this.itemType = parameterized.getActualTypeArguments()[0];

            } else if (parameterized.getActualTypeArguments()[0] instanceof Class<?>) {
                this.itemType = parameterized.getActualTypeArguments()[0];
            }
        } else if (baseType instanceof Class) {
            this.collectionClass = (Class<?>) baseType;

            if (this.collectionClass.getGenericSuperclass() != null && this.collectionClass.getGenericSuperclass() instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) this.collectionClass.getGenericSuperclass();
                if (parameterized.getActualTypeArguments().length != 1) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", baseType));
                }
                if (parameterized.getActualTypeArguments()[0] instanceof TypeVariable) {
                    throw new IllegalArgumentException(msg.format("error.cant.build.type", baseType));
                } else {
                    this.itemType = parameterized.getActualTypeArguments()[0];
                }

            } else {
                this.itemType = Object.class;
            }

        } else {
            throw new IllegalArgumentException(msg.format("error.return"));
        }

        if (collectionClass.isInterface()) {
            collectionClass = getDefaultImplementation(collectionClass);
        }
    }

    private Class<?> getDefaultImplementation(Class<?> clazz) {
        if (Collection.class.equals(clazz)) {
            return ArrayList.class;
        }
        if (List.class.equals(clazz)) {
            return ArrayList.class;
        }
        if (Set.class.equals(clazz)) {
            return HashSet.class;
        }
        if (SortedSet.class.equals(clazz) || NavigableSet.class.equals(clazz)) {
            return TreeSet.class;
        }
        if (Queue.class.equals(clazz)) {
            return LinkedList.class;
        }
        if (BlockingQueue.class.equals(clazz)) {
            return ArrayBlockingQueue.class;
        }
        if (BlockingDeque.class.equals(clazz)) {
            return LinkedBlockingDeque.class;
        }
        throw new UnsupportedOperationException(msg.format("error.implementation", clazz));
    }
}

package org.blocks4j.reconf.client.adapter.antlr4;

import org.antlr.v4.runtime.tree.ErrorNode;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;


public class ArrayTypeAwareReconfVisitor extends ReconfBaseVisitor<Object> {

    private Type itemType;
    private Object aggregationArray;
    private int index;

    public ArrayTypeAwareReconfVisitor(Type baseType) {
        this.index = 0;
        this.itemType = this.getItemType(baseType);
    }

    @Override
    public Object visitValue(ReconfParser.ValueContext ctx) {
        return new TypeAwareReconfVisitor(this.itemType).visitValue(ctx);
    }

    @Override
    public Object visitCollection(ReconfParser.CollectionContext ctx) {
        this.aggregationArray = Array.newInstance((Class<?>) this.itemType, ctx.value().size());
        Object array = super.visitCollection(ctx);
        if (array == null) {
            array = this.aggregationArray;
        }
        return array;
    }

    @Override
    public Object visitStructure(ReconfParser.StructureContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Object visitMap(ReconfParser.MapContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Object visitMapEntry(ReconfParser.MapEntryContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Object visitPrimitive(ReconfParser.PrimitiveContext ctx) {
        throw new IllegalStateException();
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
        throw new IllegalArgumentException();
    }

    @Override
    protected Object aggregateResult(Object aggregate, Object nextResult) {
        if (aggregate == null) {
            aggregate = this.aggregationArray;
        }

        if (nextResult != null) {
            Array.set(aggregate, index++, nextResult);
        }

        return aggregate;
    }

    private Type getItemType(Type baseType) {
        Type itemType;
        if (baseType instanceof Class<?>) {
            Class<?> cast = (Class<?>) baseType;
            itemType = null == cast.getComponentType() ? cast : cast.getComponentType();

        } else if (baseType instanceof GenericArrayType) {
            itemType = ((GenericArrayType) baseType).getGenericComponentType();
        } else {
            throw new UnsupportedOperationException();
        }

        return itemType;
    }
}

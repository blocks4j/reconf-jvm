package org.blocks4j.reconf.client.adapters.antlr4;

import org.antlr.v4.runtime.tree.ErrorNode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class StructureTypeAwareReconfVisitor extends ReconfBaseVisitor<Object> {

    private final Type baseType;

    public StructureTypeAwareReconfVisitor(Type baseType) {
        this.baseType = baseType;
    }

    @Override
    public Object visitStructure(ReconfParser.StructureContext ctx) {
        if (ctx.OPEN_BRACKET() != null) {
            if (baseType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) baseType;

                Type rawType = parameterizedType.getRawType();
                if (Map.class.isAssignableFrom((Class<?>) rawType)) {
                    ctx.addChild(new ReconfParser.MapContext(ctx, ctx.invokingState));
                } else {
                    ctx.addChild(new ReconfParser.CollectionContext(ctx, ctx.invokingState));
                }
            } else {
                ctx.addChild(new ReconfParser.CollectionContext(ctx, ctx.invokingState));
            }
        }

        return super.visitStructure(ctx);
    }

    @Override
    public Object visitCollection(ReconfParser.CollectionContext ctx) {
        Class<?> collectionType = Object.class;

        if (this.baseType instanceof ParameterizedType) {
            collectionType = (Class<?>) ((ParameterizedType) this.baseType).getRawType();
        } else if (this.baseType instanceof Class<?>) {
            collectionType = (Class<?>) this.baseType;
        }

        if (Collection.class.isAssignableFrom(collectionType)) {
            return new CollectionTypeAwareReconfVisitor(this.baseType).visitCollection(ctx);
        }
        return new ArrayTypeAwareReconfVisitor(this.baseType).visitCollection(ctx);
    }

    @Override
    public Object visitMap(ReconfParser.MapContext ctx) {
        return new MapTypeAwareReconfVisitor(this.baseType).visitMap(ctx);
    }

    @Override
    public Object visitValue(ReconfParser.ValueContext ctx) {
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
}

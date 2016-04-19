package org.blocks4j.reconf.client.adapters.antlr4;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.converter.BasicConverter;

import java.lang.reflect.Type;

public class TypeAwareReconfVisitor extends ReconfBaseVisitor<Object> {

    private static final BasicConverter basicConverter = new BasicConverter();

    private Type baseType;

    public TypeAwareReconfVisitor(Type baseType) {
        this.baseType = baseType;
    }

    @Override
    public Object visitStructure(ReconfParser.StructureContext ctx) {
        return new StructureTypeAwareReconfVisitor(this.baseType).visitStructure(ctx);
    }

    @Override
    public Object visitCollection(ReconfParser.CollectionContext ctx) {
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
    public Object visitErrorNode(ErrorNode node) {
        throw new IllegalArgumentException();
    }

    @Override
    public Object visitPrimitive(ReconfParser.PrimitiveContext ctx) {
        String rawValue = ctx.LITERAL().getText();

        try {
            return basicConverter.convertFromString((Class<?>) this.baseType, this.processPrimitiveValue(rawValue));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String processPrimitiveValue(String rawValue) {
        String trimmedRawValue = StringUtils.trim(rawValue);
        String strippedRawValue = StringUtils.substring(trimmedRawValue, 1, trimmedRawValue.length() - 1);
        return StringUtils.replace(strippedRawValue, "\\'", "'");
    }
}

package org.blocks4j.reconf.data;

import java.lang.reflect.Type;

public class MethodReturnData {

    private final Type returnType;
    private final String rawValue;

    public MethodReturnData(Type returnType, String rawValue) {
        this.returnType = returnType;
        this.rawValue = rawValue;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getRawValue() {
        return rawValue;
    }

    @Override
    public String toString() {
        return this.rawValue;
    }
}

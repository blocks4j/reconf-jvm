package org.blocks4j.reconf.adapter;

import org.blocks4j.reconf.data.MethodReturnData;

public interface ConfigurationAdapter<T> {

    T adapt(MethodReturnData methodData);

}
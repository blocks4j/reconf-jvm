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
package org.blocks4j.reconf.client.adapter;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.blocks4j.reconf.adapter.ConfigurationAdapter;
import org.blocks4j.reconf.data.MethodReturnData;

import java.io.IOException;

public class Jackson2ConfigurationAdapter implements ConfigurationAdapter<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeFactory typeFactory = TypeFactory.defaultInstance();

    @Override
    public Object adapt(MethodReturnData methodData) {
        try {
            JavaType javaType = this.typeFactory.constructType(methodData.getReturnType());
            return this.objectMapper.readValue(methodData.getRawValue(), javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

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
package org.blocks4j.reconf.client.adapters;


import org.blocks4j.reconf.client.constructors.MethodData;
import org.blocks4j.reconf.client.factory.ObjectConstructorFactory;

public class DefaultConfigurationAdapter implements ConfigurationAdapter<Object> {

    public Object adapt(MethodData methodData) {
        try {
            return ObjectConstructorFactory.get(methodData.getReturnType()).construct(methodData);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

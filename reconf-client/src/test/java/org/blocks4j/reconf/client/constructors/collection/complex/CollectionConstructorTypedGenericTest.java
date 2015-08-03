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
package org.blocks4j.reconf.client.constructors.collection.complex;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.blocks4j.reconf.client.constructors.CollectionConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CollectionConstructorTypedGenericTest {

    private MethodData data;
    private Method method;

    @Before
    public void prepare() throws Exception {
        method = CollectionConstructorTypedGenericTestTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_two_elem_string_list() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['x', ' y']");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(StringList.class));
        Assert.assertTrue(((Collection<?>) o).size() == 2);
        Assert.assertTrue(((List<String>) o).get(0).equals("x"));
        Assert.assertTrue(((List<String>) o).get(1).equals(" y"));
    }

}

interface CollectionConstructorTypedGenericTestTarget {
    StringList get();
}

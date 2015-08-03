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
package org.blocks4j.reconf.client.constructors.map.complex;

import java.lang.reflect.Method;
import java.util.Map;
import org.blocks4j.reconf.client.constructors.MapConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapConstructorTypedGenericTest {

    private MethodData data;
    private Method fixedKeyValue;
    private Method genericKey;
    private Method genericValue;
    private Method inherited;

    @Before
    public void prepare() throws Exception {
        fixedKeyValue = MapConstructorTypedGenericTestTarget.class.getMethod("fixedKeyValue", new Class<?>[]{});
        genericKey = MapConstructorTypedGenericTestTarget.class.getMethod("genericKey", new Class<?>[]{});
        genericValue = MapConstructorTypedGenericTestTarget.class.getMethod("genericValue", new Class<?>[]{});
        inherited = MapConstructorTypedGenericTestTarget.class.getMethod("inherited", new Class<?>[]{});
    }

    @Test
    public void test_fixedKeyValue() throws Throwable {
        data = new MethodData(fixedKeyValue, fixedKeyValue.getGenericReturnType(), "['k':'1']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(StringLongHashMap.class));
        Map<String, Long> cast = (Map<String,Long>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals(1L));
    }

    @Test
    public void test_genericKey() throws Throwable {
        data = new MethodData(genericKey, genericKey.getGenericReturnType(), "['1':'k']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(GenericStringHashMap.class));
        Map<Long, String> cast = (Map<Long, String>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals(1L));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals("k"));
    }

    @Test
    public void test_genericValue() throws Throwable {
        data = new MethodData(genericValue, genericValue.getGenericReturnType(), "['k':'1']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(StringGenericHashMap.class));
        Map<String, Long> cast = (Map<String, Long>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals(1L));
    }

    @Test
    public void test_inherited() throws Throwable {
        data = new MethodData(inherited, inherited.getGenericReturnType(), "['k':'1']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(InheritedMap.class));
        Map<Object, Object> cast = (Map<Object, Object>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals("1"));

    }
}

interface MapConstructorTypedGenericTestTarget {
    StringLongHashMap fixedKeyValue();
    GenericStringHashMap<Long> genericKey();
    StringGenericHashMap<Long> genericValue();
    InheritedMap inherited();
}

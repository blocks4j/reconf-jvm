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
package org.blocks4j.reconf.client.constructors.map;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.blocks4j.reconf.client.constructors.MapConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class MapConstructorMapStringValueTest {

    private MethodData data;
    private Method method;
    private Class<?> targetClass = LinkedHashMap.class;

    @Before
    public void prepare() throws Exception {
        method = MapConstructorMapStringValueTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_complex_map() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['k1':['ik1':'iv1','ik2':'iv2'],'k2':[]]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, Map<String,String>> cast = (Map<String,Map<String,String>>) o;
        Assert.assertTrue("iv1".equals(cast.get("k1").get("ik1")));
        Assert.assertTrue("iv2".equals(cast.get("k1").get("ik2")));
        Assert.assertTrue(cast.containsKey("k2"));
        Assert.assertTrue(cast.get("k2").isEmpty());
    }

    @Test
    public void test_null() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), null);
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Assert.assertTrue(((Map<?,?>) o).isEmpty());
    }

    @Test
    public void test_empty() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Assert.assertTrue(((Map<?,?>) o).isEmpty());
    }

    @Test(expected=Exception.class)
    public void test_blank() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "   ");
        new MapConstructor().construct(data);
    }

    @Test
    public void empty_map() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "[]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String> cast = (Map<String,String>) o;
        Assert.assertTrue(cast.isEmpty());
    }
}

interface MapConstructorMapStringValueTarget {
    LinkedHashMap<String, LinkedHashMap<String,String>> get();
}
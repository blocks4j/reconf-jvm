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
package org.blocks4j.reconf.client.constructors.collection;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import org.blocks4j.reconf.client.constructors.CollectionConstructor;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class CollectionConstructorCharTest {

    private MethodData data;
    private Method methodList;

    @Before
    public void prepare() throws Exception {
        methodList = CollectionConstructorCharTestTarget.class.getMethod("list", new Class<?>[]{});
    }

    @Test
    public void test_null_char_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), null);
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(LinkedList.class));
        Assert.assertTrue(((Collection<?>) o).isEmpty());
    }

    @Test
    public void test_empty_char_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "[]");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(LinkedList.class));
        Assert.assertTrue(((Collection<?>) o).isEmpty());
    }

    @Test
    public void test_one_elem_char_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "['a']");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(LinkedList.class));
        Assert.assertTrue(((Collection<?>) o).size() == 1);
        Assert.assertTrue(((LinkedList<Character>) o).get(0).equals('a'));
    }

    @Test
    public void test_two_elem_char_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "['a','b']");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(LinkedList.class));
        Assert.assertTrue(((Collection<?>) o).size() == 2);
        Assert.assertTrue(((LinkedList<Character>) o).get(0).equals('a'));
        Assert.assertTrue(((LinkedList<Character>) o).get(1).equals('b'));
    }
}

interface CollectionConstructorCharTestTarget {
    LinkedList<Character> list();
}
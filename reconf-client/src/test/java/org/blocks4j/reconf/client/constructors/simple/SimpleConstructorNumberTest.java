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
package org.blocks4j.reconf.client.constructors.simple;

import java.lang.reflect.Method;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.blocks4j.reconf.client.constructors.SimpleConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SimpleConstructorNumberTest {

    private MethodData data;
    private Method intMethod;
    private Method longMethod;
    private Method byteMethod;
    private Method shortMethod;

    @Before
    public void prepare() throws Exception {
        intMethod = SimpleConstructorNumberTestTarget.class.getMethod("getInt", new Class<?>[]{});
        longMethod = SimpleConstructorNumberTestTarget.class.getMethod("getLong", new Class<?>[]{});
        byteMethod = SimpleConstructorNumberTestTarget.class.getMethod("getByte", new Class<?>[]{});
        shortMethod = SimpleConstructorNumberTestTarget.class.getMethod("getShort", new Class<?>[]{});
    }

    @Test
    public void test_int_class() throws Throwable {
        data = new MethodData(intMethod, intMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Integer.class));
    }

    @Test
    public void test_positive_int() throws Throwable {
        data = new MethodData(intMethod, intMethod.getReturnType(), "'1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Integer(1), o);
        Assert.assertEquals(1, o);
    }

    @Test
    public void test_negative_int() throws Throwable {
        data = new MethodData(intMethod, intMethod.getReturnType(), "'-1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Integer(-1), o);
        Assert.assertEquals(-1, o);
    }

    @Test
    public void test_untrimmed_int() throws Throwable {
        data = new MethodData(intMethod, intMethod.getReturnType(), "' 1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Integer(1), o);
        Assert.assertEquals(1, o);
    }

    @Test
    public void test_long_class() throws Throwable {
        data = new MethodData(longMethod, longMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Long.class));
    }

    @Test
    public void test_positive_long() throws Throwable {
        data = new MethodData(longMethod, longMethod.getReturnType(), "'1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Long(1), o);
        Assert.assertEquals(1L, o);
    }

    @Test
    public void test_negative_long() throws Throwable {
        data = new MethodData(longMethod, longMethod.getReturnType(), "'-1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Long(-1), o);
        Assert.assertEquals(-1L, o);
    }

    @Test
    public void test_untrimmed_long() throws Throwable {
        data = new MethodData(longMethod, longMethod.getReturnType(), "'1 '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Long(1), o);
        Assert.assertEquals(1L, o);
    }

    @Test
    public void test_byte_class() throws Throwable {
        data = new MethodData(byteMethod, byteMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Byte.class));
    }

    @Test
    public void test_positive_byte() throws Throwable {
        data = new MethodData(byteMethod, byteMethod.getReturnType(), "'1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Byte((byte) 1), o);
        Assert.assertEquals((byte) 1, o);
    }

    @Test
    public void test_negative_byte() throws Throwable {
        data = new MethodData(byteMethod, byteMethod.getReturnType(), "'-1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Byte((byte) -1), o);
        Assert.assertEquals((byte) -1, o);
    }

    @Test
    public void test_untrimmed_byte() throws Throwable {
        data = new MethodData(byteMethod, byteMethod.getReturnType(), "' 1 '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Byte((byte) 1), o);
        Assert.assertEquals((byte) 1, o);
    }

    @Test
    public void test_short_class() throws Throwable {
        data = new MethodData(shortMethod, shortMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Short.class));
    }

    @Test
    public void test_positive_short() throws Throwable {
        data = new MethodData(shortMethod, shortMethod.getReturnType(), "'1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Short((short) 1), o);
        Assert.assertEquals((short) 1, o);
    }

    @Test
    public void test_negative_short() throws Throwable {
        data = new MethodData(shortMethod, shortMethod.getReturnType(), "'-1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Short((short) -1), o);
        Assert.assertEquals((short) -1, o);
    }

    @Test
    public void test_untrimmed_short() throws Throwable {
        data = new MethodData(shortMethod, shortMethod.getReturnType(), "'    1    '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Short((short) 1), o);
        Assert.assertEquals((short) 1, o);
    }
}

interface SimpleConstructorNumberTestTarget {
    int getInt();
    long getLong();
    byte getByte();
    short getShort();
}
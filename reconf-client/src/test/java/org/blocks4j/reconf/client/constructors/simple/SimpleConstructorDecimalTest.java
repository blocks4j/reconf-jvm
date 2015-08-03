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


public class SimpleConstructorDecimalTest {

    private MethodData data;
    private Method doubleMethod;
    private Method floatMethod;

    @Before
    public void prepare() throws Exception {
        doubleMethod = SimpleConstructorDecimalTestTarget.class.getMethod("getDouble", new Class<?>[]{});
        floatMethod = SimpleConstructorDecimalTestTarget.class.getMethod("getFloat", new Class<?>[]{});
    }

    @Test
    public void test_double_class() throws Throwable {
        data = new MethodData(doubleMethod, doubleMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Double.class));
    }

    @Test
    public void test_positive_double() throws Throwable {
        data = new MethodData(doubleMethod, doubleMethod.getReturnType(), "'1.00000'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Double(1), o);
        Assert.assertEquals(1D, o);
    }

    @Test
    public void test_negative_double() throws Throwable {
        data = new MethodData(doubleMethod, doubleMethod.getReturnType(), "'-1.0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Double(-1), o);
        Assert.assertEquals(-1D, o);
    }

    @Test
    public void test_untrimmed_double() throws Throwable {
        data = new MethodData(doubleMethod, doubleMethod.getReturnType(), "' 1.00000 '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Double(1), o);
        Assert.assertEquals(1D, o);
    }

    @Test
    public void test_float_class() throws Throwable {
        data = new MethodData(floatMethod, floatMethod.getReturnType(), "'0'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Float.class));
    }

    @Test
    public void test_positive_float() throws Throwable {
        data = new MethodData(floatMethod, floatMethod.getReturnType(), "'1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Float(1), o);
        Assert.assertEquals(1F, o);
    }

    @Test
    public void test_negative_float() throws Throwable {
        data = new MethodData(floatMethod, floatMethod.getReturnType(), "'-1'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Float(-1), o);
        Assert.assertEquals(-1F, o);
    }

    @Test
    public void test_untrimmed_float() throws Throwable {
        data = new MethodData(floatMethod, floatMethod.getReturnType(), "' 1.00000 '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Float(1), o);
        Assert.assertEquals(1F, o);
    }
}

interface SimpleConstructorDecimalTestTarget {
    float getFloat();
    double getDouble();
}
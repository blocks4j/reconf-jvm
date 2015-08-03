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
package org.blocks4j.reconf.client.constructors.complex;

import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.blocks4j.reconf.client.constructors.MethodData;
import org.blocks4j.reconf.client.constructors.StringParser;
import org.junit.Assert;
import org.junit.Test;


public class StringParserTest {

    @Test
    public void test_inner_col() {
        StringParser type = new StringParser(methodDataOf("[ ['a'] ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("['a']"));
    }

    @Test
    public void test_escaped_inner_col() {
        StringParser type = new StringParser(methodDataOf("[ ['[a]'] ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("['[a]']"));
    }

    @Test
    public void test_escaped_two_elem_inner_col() {
        StringParser type = new StringParser(methodDataOf("[ ['[a]', 'b'], ['c'] ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 2);
        Assert.assertTrue(values.get(0).equals("['[a]', 'b']"));
        Assert.assertTrue(values.get(1).equals("['c']"));
    }

    @Test
    public void test_escaped_single_quote_elem() {
        StringParser type = new StringParser(methodDataOf("[ ['\\''] ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("[''']"));
    }

    @Test
    public void test_escaped_double_backslash() {
        //essa entrada simula o valor \\ vindo do remote
        StringParser type = new StringParser(methodDataOf("[ ['\\\\'] ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.get(0).equals("['\\']"));
    }

    @Test
    public void test_new_line() {
        StringParser type = new StringParser(methodDataOf("[ '\n' ]"));
        Assert.assertTrue(type.getTokens().size() == 1);
        Assert.assertTrue(type.getTokens().iterator().next().equals("'\n'"));
    }

    @Test
    public void test_two_elem() {
        StringParser type = new StringParser(methodDataOf("[ '\n' , 'x', 'y' ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 3);
        Assert.assertTrue(values.get(0).equals("'\n'"));
        Assert.assertTrue(values.get(1).equals("'x'"));
        Assert.assertTrue(values.get(2).equals("'y'"));
    }

    @Test
    public void test_three_elem() {
        StringParser type = new StringParser(methodDataOf("[ '0', '1' , '2' notAToken ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 3);
        Assert.assertTrue(values.get(0).equals("'0'"));
        Assert.assertTrue(values.get(1).equals("'1'"));
        Assert.assertTrue(values.get(2).equals("'2'"));
    }

    @Test
    public void empty_invalid() {
        StringParser type = new StringParser(methodDataOf("[ notAToken ]"));
        Assert.assertTrue(type.getTokens().isEmpty());
    }

    @Test
    public void valid_with_escape() {
        StringParser type = new StringParser(methodDataOf("[ '\\\\', '\\\\' ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 2);
        Assert.assertTrue(values.get(0).equals("'\\'"));
        Assert.assertTrue(values.get(1).equals("'\\'"));
    }

    @Test
    public void valid_with_separator() {
        StringParser type = new StringParser(methodDataOf("[ '\\'', '\\'' ]"));
        List<String> values = type.getTokens();
        Assert.assertTrue(values.size() == 2);
        Assert.assertTrue(values.get(0).equals("'''"));
        Assert.assertTrue(values.get(1).equals("'''"));
    }

    private MethodData methodDataOf(String arg) {
        try {
            return new MethodData(Object.class.getMethod("toString", ArrayUtils.EMPTY_CLASS_ARRAY), String.class, arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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
package org.blocks4j.reconf.infra.io;

import java.io.InputStream;
import java.net.URI;
import org.blocks4j.reconf.infra.log.LoggerHolder;


public class ClasspathInputStream {

    public static InputStream from(String arg) {
        URI uri = URI.create(arg);
        if (null == uri) {
            return null;
        }

        InputStream is = null;

        try {
            String name = RegExp.withoutSchemeAndParameters(uri);
            is = classLoader(name);
            if (null == is) {
                is = currentThread(name);
                if (null == is) {
                    is = newClass(name);
                }
                if (null == is) {
                    is = classLoader("/" + name);
                }
                if (null == is) {
                    is = currentThread("/" + name);
                }
                if (null == is) {
                    is = newClass("/" + name);
                }
                if (null == is) {
                    return null;
                }
            }

        } catch (Exception e) {
            LoggerHolder.getLog().error("error while reading file from classpath", e);
        }

        return is;
    }

    private static InputStream classLoader(String arg) {
        return ClassLoader.getSystemResourceAsStream(arg);
    }

    private static InputStream currentThread(String arg) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(arg);
    }

    private static InputStream newClass(String arg) {
        return Class.class.getResourceAsStream(arg);
    }
}

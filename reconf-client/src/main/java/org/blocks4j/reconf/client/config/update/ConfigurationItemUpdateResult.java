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
package org.blocks4j.reconf.client.config.update;

import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;

public class ConfigurationItemUpdateResult implements Cloneable {

    public enum Type {update, noChange, error}

    public enum Source {server, localCache}

    private boolean success;
    private Type type;
    private Object object;
    private String product;
    private String component;
    private String item;
    private Method method;
    private Class<?> cast;
    private String rawValue;
    private Throwable error;
    private Source source;

    private ConfigurationItemUpdateResult() {
    }

    public Object getObject() {
        return object;
    }

    public String getProduct() {
        return product;
    }

    public String getComponent() {
        return component;
    }

    public String getItem() {
        return item;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getCast() {
        return cast;
    }

    public String getRawValue() {
        return rawValue;
    }

    public Throwable getError() {
        return error;
    }

    public Type getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !this.isSuccess();
    }

    public Source getSource() {
        return source;
    }

    public static int countSuccess(Collection<ConfigurationItemUpdateResult> arg) {
        if (CollectionUtils.isEmpty(arg)) {
            return 0;
        }
        int result = 0;
        for (ConfigurationItemUpdateResult proc : arg) {
            if (proc.getType() != Type.error) {
                ++result;
            }
        }
        return result;
    }

    public static class Builder {
        private ConfigurationItemUpdateResult inner = new ConfigurationItemUpdateResult();

        public static Builder update(Object object) {
            Builder result = new Builder();
            result.inner.type = Type.update;
            result.inner.object = object;
            result.inner.success = true;
            return result;
        }

        public static Builder noChange() {
            Builder result = new Builder();
            result.inner.type = Type.noChange;
            result.inner.object = null;
            result.inner.success = true;
            return result;
        }

        public static Builder error(Throwable t) {
            Builder result = new Builder();
            result.inner.type = Type.error;
            result.inner.object = null;
            result.inner.success = false;
            result.inner.error = t;
            return result;
        }


        public Builder valueRead(String value) {
            this.inner.rawValue = value;
            return this;
        }

        public Builder product(String product) {
            this.inner.product = product;
            return this;
        }

        public Builder component(String component) {
            this.inner.component = component;
            return this;
        }

        public Builder item(String item) {
            this.inner.item = item;
            return this;
        }

        public Builder method(Method method) {
            this.inner.method = method;
            return this;
        }

        public Builder cast(Class<?> cast) {
            this.inner.cast = cast;
            return this;
        }

        public Builder from(Source source) {
            this.inner.source = source;
            return this;
        }

        public ConfigurationItemUpdateResult build() {
            return this.inner;
        }
    }
}

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
package org.blocks4j.reconf.client.notification;

import java.lang.reflect.Method;
import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;
import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult.Source;

abstract class AbstractNotification {

    private String product;
    private String component;
    private String item;
    private Method method;
    private Class<?> cast;
    private String rawValue;
    private Source source;
    private String qualifier;

    public AbstractNotification(ConfigurationItemUpdateResult result) {
        this.product = result.getProduct();
        this.component = result.getComponent();
        this.item = result.getItem();
        this.method = result.getMethod();
        this.cast = result.getCast();
        this.rawValue = result.getRawValue();
        this.source = result.getSource();
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

    public String getQualifier() {
        return qualifier;
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

    public Source getSource() {
        return source;
    }
}

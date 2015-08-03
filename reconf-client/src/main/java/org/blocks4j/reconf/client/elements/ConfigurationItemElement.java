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
package org.blocks4j.reconf.client.elements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.blocks4j.reconf.client.adapters.ConfigurationAdapter;
import org.blocks4j.reconf.client.annotations.ConfigurationItem;
import org.blocks4j.reconf.client.annotations.UpdateConfigurationRepository;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;


public class ConfigurationItemElement {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemElement.class);
    private String methodName;
    private Method method;
    private String value;
    private String component;
    private String product;
    private Class<? extends ConfigurationAdapter> adapter;

    public static List<ConfigurationItemElement> from(ConfigurationRepositoryElement repository) {
        List<ConfigurationItemElement> result = new ArrayList<ConfigurationItemElement>();
        for (Method method : repository.getInterfaceClass().getMethods()) {

            ConfigurationItem ann = method.getAnnotation(ConfigurationItem.class);
            if (ann == null) {
                if(method.isAnnotationPresent(UpdateConfigurationRepository.class)) {
                    continue;
                }
                throw new ReConfInitializationError(msg.format("error.not.configured.method", method.toString()));
            }

            ConfigurationItemElement resultItem = null;

            for (ConfigurationItemElement item : repository.getConfigurationItems()) {
                if (StringUtils.equals(item.getMethodName(), method.getName())) {
                    resultItem = item;
                }
            }

            if (resultItem == null) {
                resultItem = new ConfigurationItemElement();
                resultItem.setMethod(method.getName());
                resultItem.setAdapter(ann.adapter());
                resultItem.setValue(ann.value());
            }
            resultItem.setMethod(method);
            defineItemProductComponentOverride(repository, resultItem, ann);
            result.add(resultItem);
        }
        return result;
    }

    private static void defineItemProductComponentOverride(ConfigurationRepositoryElement repo, ConfigurationItemElement resultItem, ConfigurationItem annItem) {
        if (StringUtils.isBlank(resultItem.getProduct()) && StringUtils.isNotBlank(annItem.product())) {
            resultItem.setProduct(annItem.product());
        } else {
            resultItem.setProduct(repo.getProduct());
        }

        if (StringUtils.isBlank(resultItem.getComponent()) && StringUtils.isNotBlank(annItem.component())) {
            resultItem.setComponent(annItem.component());
        } else {
            resultItem.setComponent(repo.getComponent());
        }
    }

    public String getMethodName() {
        return methodName;
    }
    public void setMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public Class<? extends ConfigurationAdapter> getAdapter() {
        return adapter;
    }
    public void setAdapter(Class<? extends ConfigurationAdapter> adapter) {
        this.adapter = adapter;
    }

    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("method", StringUtils.replace(getMethod().toString(), "public abstract ", ""));
        addToString(result, "product", getProduct());
        addToString(result, "component", getComponent());
        result.append("value", getValue());
        return result.toString();
    }

    private void addToString(ToStringBuilder arg, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            arg.append(key, value);
        }
    }
}

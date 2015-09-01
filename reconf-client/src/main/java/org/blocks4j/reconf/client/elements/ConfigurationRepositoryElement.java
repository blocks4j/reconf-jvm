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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.blocks4j.reconf.client.notification.ConfigurationItemListener;
import org.blocks4j.reconf.client.proxy.Customization;
import org.blocks4j.reconf.client.setup.ConnectionSettings;
import org.blocks4j.reconf.infra.system.LineSeparator;


public class ConfigurationRepositoryElement {

    private ConnectionSettings connectionSettings;
    private String product;
    private String component;
    private Integer rate;
    private TimeUnit timeUnit;
    private Collection<ConfigurationItemListener> configurationItemListeners = new ArrayList<ConfigurationItemListener>();

    private Class<?> interfaceClass;
    private List<ConfigurationItemElement> configurationItems = new ArrayList<ConfigurationItemElement>();
    private Customization customization;

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    public Collection<String> getFullProperties() {
        Set<String> result = new LinkedHashSet<String>();
        for (ConfigurationItemElement elem : configurationItems) {
            String productName = null;
            if (StringUtils.isEmpty(elem.getProduct())) {
                productName = getProduct();
            } else {
                productName = elem.getProduct();
            }

            String componentName = null;
            if (StringUtils.isEmpty(elem.getComponent())) {
                componentName = getComponent();
            } else {
                componentName = elem.getComponent();
            }
            result.add(FullPropertyElement.from(productName, componentName, elem.getValue()));
        }
        return result;
    }

    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public List<ConfigurationItemElement> getConfigurationItems() {
        return configurationItems;
    }
    public void setConfigurationItems(List<ConfigurationItemElement> configurationItems) {
        this.configurationItems = configurationItems;
    }

    public Customization getCustomization() {
        return customization;
    }
    public void setCustomization(Customization customization) {
        this.customization = customization;
    }

    public Integer getRate() {
        return rate;
    }
    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Collection<ConfigurationItemListener> getConfigurationItemListeners() {
        return configurationItemListeners;
    }
    public void setConfigurationItemListeners(Collection<ConfigurationItemListener> configurationItemListeners) {
        if (configurationItemListeners != null) {
            this.configurationItemListeners = configurationItemListeners;
        }
    }
    public void addConfigurationItemListener(ConfigurationItemListener configurationItemListener) {
        if (configurationItemListener != null) {
            this.configurationItemListeners.add(configurationItemListener);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
        .append("class", getInterfaceClass())
        .append("product", getProduct())
        .append("component", getComponent())
        .append("pollingRate", getRate())
        .append("pollingTimeUnit", getTimeUnit());
        if (!configurationItemListeners.isEmpty()) {
            List<ConfigurationItemListener> asList = new ArrayList<ConfigurationItemListener>(configurationItemListeners);
            for (int i = 0; i < asList.size(); i++) {
                result.append("item-listener-" + (i+1), asList.get(i).getClass().getName());
            }
        }

        result.append("@ConfigurationItems", LineSeparator.value() + getConfigurationItems());
        return result.toString();
    }
}

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
package org.blocks4j.reconf.client.proxy;

import org.apache.commons.lang3.StringUtils;


public class Customization {

    private String productPrefix;
    private String productSuffix;
    private String componentPrefix;
    private String componentSuffix;
    private String namePrefix;
    private String nameSuffix;

    public String getProductPrefix() {
        return productPrefix;
    }
    public void setProductPrefix(String productPrefix) {
        this.productPrefix = productPrefix;
    }

    public String getProductSuffix() {
        return productSuffix;
    }
    public void setProductSuffix(String productSuffix) {
        this.productSuffix = productSuffix;
    }

    public String getComponentPrefix() {
        return componentPrefix;
    }
    public void setComponentPrefix(String componentPrefix) {
        this.componentPrefix = componentPrefix;
    }

    public String getComponentSuffix() {
        return componentSuffix;
    }
    public void setComponentSuffix(String componentSuffix) {
        this.componentSuffix = componentSuffix;
    }

    public String getComponentItemPrefix() {
        return namePrefix;
    }
    public void setComponentItemPrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getComponentItemSuffix() {
        return nameSuffix;
    }
    public void setComponentItemSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Customization)) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    public boolean isBlank() {
        return StringUtils.isBlank(productPrefix) &&
            StringUtils.isBlank(productSuffix) &&
            StringUtils.isBlank(componentPrefix) &&
            StringUtils.isBlank(componentSuffix) &&
            StringUtils.isBlank(namePrefix) &&
            StringUtils.isBlank(nameSuffix);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("productPrefix[").append(StringUtils.defaultString(productPrefix)).append("] ")
            .append("productSuffix[").append(StringUtils.defaultString(productSuffix)).append("] ")
            .append("componentPrefix [").append(StringUtils.defaultString(componentPrefix)).append("] ")
            .append("componentSuffix [").append(StringUtils.defaultString(componentSuffix)).append("] ")
            .append("keyPrefix [").append(StringUtils.defaultString(namePrefix)).append("] ")
            .append("keySuffix [").append(StringUtils.defaultString(nameSuffix)).append("] ")
            .toString();
    }

    public String getCustomProduct(String originalProduct) {
        if (StringUtils.isBlank(originalProduct) || (StringUtils.isBlank(getProductPrefix()) && StringUtils.isBlank(getProductSuffix()))) {
            return originalProduct;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getProductPrefix())) {
            builder.append(getProductPrefix());
        }
        builder.append(originalProduct);
        if (StringUtils.isNotBlank(getProductSuffix())) {
            builder.append(getProductSuffix());
        }
        return builder.toString();

    }

    public String getCustomComponent(String originalComponent) {
        if (StringUtils.isBlank(originalComponent) || (StringUtils.isBlank(getComponentPrefix()) && StringUtils.isBlank(getComponentSuffix()))) {
            return originalComponent;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getComponentPrefix())) {
            builder.append(getComponentPrefix());
        }
        builder.append(originalComponent);
        if (StringUtils.isNotBlank(getComponentSuffix())) {
            builder.append(getComponentSuffix());
        }
        return builder.toString();

    }

    public String getCustomItem(String originalKey) {
        if (StringUtils.isBlank(originalKey) || (StringUtils.isBlank(getComponentItemPrefix()) && StringUtils.isBlank(getComponentItemSuffix()))) {
            return originalKey;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getComponentItemPrefix())) {
            builder.append(getComponentItemPrefix());
        }
        builder.append(originalKey);
        if (StringUtils.isNotBlank(getComponentItemSuffix())) {
            builder.append(getComponentItemSuffix());
        }
        return builder.toString();
    }
}

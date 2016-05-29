package org.blocks4j.reconf.client.customization;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Customization {

    private String productPrefix;
    private String productSuffix;
    private String componentPrefix;
    private String componentSuffix;
    private String componentItemPrefix;
    private String componentItemSuffix;

    public Customization() {

    }

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
        return componentItemPrefix;
    }

    public void setComponentItemPrefix(String componentItemPrefix) {
        this.componentItemPrefix = componentItemPrefix;
    }

    public String getComponentItemSuffix() {
        return componentItemSuffix;
    }

    public void setComponentItemSuffix(String componentItemSuffix) {
        this.componentItemSuffix = componentItemSuffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customization)) return false;
        Customization that = (Customization) o;
        return Objects.equals(getProductPrefix(), that.getProductPrefix()) &&
                Objects.equals(getProductSuffix(), that.getProductSuffix()) &&
                Objects.equals(getComponentPrefix(), that.getComponentPrefix()) &&
                Objects.equals(getComponentSuffix(), that.getComponentSuffix()) &&
                Objects.equals(getComponentItemPrefix(), that.getComponentItemPrefix()) &&
                Objects.equals(getComponentItemSuffix(), that.getComponentItemSuffix());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductPrefix(), getProductSuffix(), getComponentPrefix(), getComponentSuffix(), getComponentItemPrefix(), getComponentItemSuffix());
    }

    @Override
    public String toString() {
        return "productPrefix[" + StringUtils.defaultString(productPrefix) + "] " +
                "productSuffix[" + StringUtils.defaultString(productSuffix) + "] " +
                "componentPrefix [" + StringUtils.defaultString(componentPrefix) + "] " +
                "componentSuffix [" + StringUtils.defaultString(componentSuffix) + "] " +
                "keyPrefix [" + StringUtils.defaultString(componentItemPrefix) + "] " +
                "keySuffix [" + StringUtils.defaultString(componentItemSuffix) + "] ";
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

package org.blocks4j.reconf.client.config;

import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationRepository {

    private Map<Method, Object> repository;

    public ConfigurationRepository() {
        this.repository = new ConcurrentHashMap<>();
    }

    public Object getValueOf(Method method) {
        return this.repository.get(method);
    }

    public void update(ConfigurationItemUpdateResult result) {
        if (result.isSuccess()) {
            this.repository.put(result.getMethod(), result.getObject());
        }
    }
}

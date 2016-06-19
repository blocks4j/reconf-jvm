package org.blocks4j.reconf.client.config;

import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;

import java.lang.reflect.Method;

public interface ConfigurationRepository extends ShutdownBean {
    Object getValueOf(Method method);

    ConfigurationItemUpdateResult update(ConfigurationItemUpdateResult result);
}

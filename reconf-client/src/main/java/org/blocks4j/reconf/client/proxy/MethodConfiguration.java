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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.adapters.ConfigurationAdapter;
import org.blocks4j.reconf.client.config.source.ConfigurationSourceHolder;
import org.blocks4j.reconf.client.config.source.DatabaseConfigurationSource;
import org.blocks4j.reconf.client.config.source.RemoteConfigurationSource;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.elements.FullPropertyElement;
import org.blocks4j.reconf.client.locator.ServiceLocator;
import org.blocks4j.reconf.client.setup.ConnectionSettings;
import org.blocks4j.reconf.infra.http.ServerStub;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;


public class MethodConfiguration {

    private static final MessagesBundle msg = MessagesBundle.getBundle(MethodConfiguration.class);

    private final ConfigurationRepositoryElement cfgRepository;
    private final ConfigurationItemElement remoteItem;
    private final ServerStub stub;
    private final ServiceLocator locator;


    public MethodConfiguration(ConfigurationRepositoryElement cfgRepository, ConfigurationItemElement itemConfiguration, ServiceLocator locator) {
        this.cfgRepository = cfgRepository;
        this.remoteItem = itemConfiguration;
        this.locator = locator;
        this.stub = createStub();
    }

    public ConfigurationSourceHolder getConfigurationSourceHolder() {
        try {
            ConfigurationAdapter adapter = getRemoteAdapter();
            ConfigurationSourceHolder holder = new ConfigurationSourceHolder(new RemoteConfigurationSource(remoteItem.getValue(), stub, adapter),
                    new DatabaseConfigurationSource(FullPropertyElement.from(stub.getProduct(), stub.getComponent(), remoteItem.getValue()), getMethod(), adapter, locator));
            return holder;

        } catch (Throwable t) {
            throw new IllegalStateException(msg.get("error.source"), t);
        }
    }

    private ConfigurationAdapter getRemoteAdapter() {
        if (null == remoteItem || remoteItem.getAdapter() == null) {
            return null;
        }
        try {
            Constructor<? extends ConfigurationAdapter> constructor = remoteItem.getAdapter().getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
            return constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);
        } catch (Throwable t) {
            throw new IllegalArgumentException(msg.get("error.adapter"), t);
        }
    }

    private ServerStub createStub() {
        ConnectionSettings settings = cfgRepository.getConnectionSettings();
        ServerStub stub = locator.serverStubFactory().serverStub(settings.getUrl(), settings.getTimeout(), settings.getTimeUnit(), settings.getMaxRetry());
        stub.setComponent(StringUtils.isNotBlank(remoteItem.getComponent()) ? remoteItem.getComponent() : cfgRepository.getComponent());
        stub.setProduct(StringUtils.isNotBlank(remoteItem.getProduct()) ? remoteItem.getProduct() : cfgRepository.getProduct());
        return stub;
    }

    public int getReloadRate() {
        return cfgRepository.getRate();
    }

    public TimeUnit getReloadTimeUnit() {
        return cfgRepository.getTimeUnit();
    }

    public Method getMethod() {
        return remoteItem.getMethod();
    }

    public ServerStub getServerStub() {
        return stub;
    }

    public ConfigurationItemElement getConfigurationItemElement() {
        return remoteItem;
    }
}

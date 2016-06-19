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
package org.blocks4j.reconf.client.factory;

import org.apache.commons.collections4.CollectionUtils;
import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.config.update.ConfigurationRepositoryUpdater;
import org.blocks4j.reconf.client.config.update.notification.ConfigurationItemListener;
import org.blocks4j.reconf.client.customization.Customization;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.proxy.ConfigurationRepositoryProxyHandler;
import org.blocks4j.reconf.client.setup.AbstractEnvironment;
import org.blocks4j.reconf.client.setup.DefaultEnvironment;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;

public class ConfigurationRepositoryFactory implements ShutdownBean {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryFactory.class);

    private AbstractEnvironment environment;

    private ConfigurationRepository repository;
    private ConfigurationRepositoryElementFactory factory;
    private final ConcurrentMap<String, Object> proxyCache;
    private final ConcurrentMap<String, Collection<? extends ConfigurationItemListener>> listenerCache;
    private final ScheduledExecutorService scheduledExecutorService;

    public ConfigurationRepositoryFactory() {
        this(new DefaultEnvironment());
    }

    public ConfigurationRepositoryFactory(ReconfConfiguration reconfConfiguration) {
        this(new DefaultEnvironment(reconfConfiguration));
    }

    public ConfigurationRepositoryFactory(AbstractEnvironment environment) {
        this.environment = environment;

        this.proxyCache = new ConcurrentHashMap<>();
        this.listenerCache = new ConcurrentHashMap<>();
        this.factory = new ConfigurationRepositoryElementFactory(environment.getReconfConfiguration());
        this.repository = environment.getRepository();

        this.scheduledExecutorService = this.createSchedulerService();
        this.environment.manageShutdownObject(this);
    }

    private ScheduledExecutorService createSchedulerService() {
        return Executors.newScheduledThreadPool(5, new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("reconf-scheduler-" + ++threadCount);
                return thread;
            }
        });
    }

    public synchronized <T> T get(Class<T> arg) {
        return get(arg, null, null);
    }

    public synchronized <T> T get(Class<T> arg, Customization customization) {
        return get(arg, customization, null);
    }

    public synchronized <T> T get(Class<T> arg, Collection<? extends ConfigurationItemListener> configurationItemListeners) {
        return get(arg, null, configurationItemListeners);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(Class<T> arg, Customization customization, Collection<? extends ConfigurationItemListener> configurationItemListeners) {
        if (customization == null) {
            customization = new Customization();
        }
        if (configurationItemListeners == null) {
            configurationItemListeners = Collections.emptyList();
        }

        String key = arg.getName() + customization;
        if (proxyCache.containsKey(key)) {
            if (CollectionUtils.isEqualCollection(configurationItemListeners, listenerCache.get(key))) {
                LoggerHolder.getLog().info(msg.format("cached.instance", arg.getName()));
                return (T) proxyCache.get(key);
            }

            throw new IllegalArgumentException(msg.format("error.customization", arg.getName()));
        }

        ConfigurationRepositoryElement repo = this.factory.create(arg);
        repo.setCustomization(customization);
        repo.setComponent(customization.getCustomComponent(repo.getComponent()));
        repo.setProduct(customization.getCustomProduct(repo.getProduct()));

        configurationItemListeners.forEach(repo::addConfigurationItemListener);

        for (ConfigurationItemElement item : repo.getConfigurationItems()) {
            item.setProduct(repo.getProduct());
            item.setComponent(customization.getCustomComponent(item.getComponent()));
            item.setValue(customization.getCustomItem(item.getValue()));
        }

        //LoggerHolder.getLog().info(msg.format("new.instance", LineSeparator.value(), repo.toString()));

        Object result = newInstance(arg, repo);
        proxyCache.put(key, result);
        listenerCache.put(key, configurationItemListeners);
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T newInstance(Class<T> arg, ConfigurationRepositoryElement configurationRepositoryElement) {
        ConfigurationRepositoryUpdater repositoryUpdater = new ConfigurationRepositoryUpdater(this.environment, this.repository, configurationRepositoryElement);

        this.scheduleUpdater(configurationRepositoryElement, repositoryUpdater);

        Object proxyInstance = Proxy.newProxyInstance(arg.getClassLoader(), new Class<?>[]{arg}, new ConfigurationRepositoryProxyHandler(this.repository, repositoryUpdater));

        this.validateProxyLoad(proxyInstance, arg);

        return (T) proxyInstance;
    }

    private void validateProxyLoad(Object proxyInstance, Class<?> proxyInterface) {
        for (Method method : proxyInterface.getMethods()) {
            Object methodReturn;
            try {
                methodReturn = method.invoke(proxyInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }

            if (methodReturn == null) {
                throw new IllegalStateException();
            }
        }
    }

    private void scheduleUpdater(ConfigurationRepositoryElement configurationRepositoryElement, ConfigurationRepositoryUpdater repositoryUpdater) {
        this.scheduledExecutorService.scheduleWithFixedDelay(repositoryUpdater, configurationRepositoryElement.getRate(), configurationRepositoryElement.getRate(), configurationRepositoryElement.getTimeUnit());
    }

    public AbstractEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public void shutdown() {
        this.scheduledExecutorService.shutdown();
    }
}

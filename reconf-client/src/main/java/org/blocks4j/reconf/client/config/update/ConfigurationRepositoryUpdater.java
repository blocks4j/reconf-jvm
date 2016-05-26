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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.check.ObservableThread;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.locator.ServiceLocator;
import org.blocks4j.reconf.client.notification.ConfigurationItemListener;
import org.blocks4j.reconf.client.notification.Notifier;
import org.blocks4j.reconf.client.proxy.ConfigurationRepositoryFactory;
import org.blocks4j.reconf.client.proxy.MethodConfiguration;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.system.LineSeparator;
import org.blocks4j.reconf.infra.throwables.ReConfInitializationError;
import org.blocks4j.reconf.infra.throwables.UpdateConfigurationRepositoryException;


public class ConfigurationRepositoryUpdater extends ObservableThread {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryUpdater.class);
    private final ConfigurationRepositoryElement cfgRepository;
    private final ConfigurationRepositoryData data;
    private Map<Method, Object> methodValue = new ConcurrentHashMap<>();
    private final ConfigurationRepositoryFactory factory;
    private ServiceLocator locator;
    private Collection<ConfigurationItemListener> listeners = Collections.EMPTY_LIST;

    public ConfigurationRepositoryUpdater(ConfigurationRepositoryElement elem, ServiceLocator locator, ConfigurationRepositoryFactory factory) {
        setDaemon(true);
        this.locator = locator;
        this.factory = factory;
        cfgRepository = elem;
        setName(elem.getInterfaceClass().getName() + "_updater" + new Object().toString().replace("java.lang.Object", ""));
        data = new ConfigurationRepositoryData(elem, locator);
        listeners = elem.getConfigurationItemListeners();

        load();
        updateLastExecution();
        factory.setUpdater(this);
    }

    public void syncNow(Class<? extends RuntimeException> cls) {
        sync(cls);
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                getReloadTimeUnit().sleep(getReloadRate());
                updateLastExecution();
                update();
            }
        } catch (InterruptedException e) {
            LoggerHolder.getLog().warn(msg.format("interrupted.thread", getName()));
            Thread.currentThread().interrupt();

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.reloading.all.items", getName()), t);
        }
    }

    private void load() {
        CountDownLatch latch = new CountDownLatch(data.getAll().size() + data.getAtomicReload().size());
        List<ConfigurationUpdater> toExecuteGlobal = new ArrayList<>();
        List<ConfigurationUpdater> toExecuteLocal = new ArrayList<>();
        List<ConfigurationUpdater> toExecuteRemote = new ArrayList<>();

        Map<Method, ConfigurationItemUpdateResult> remote = new ConcurrentHashMap<>();
        Map<Method, ConfigurationItemUpdateResult> local = new ConcurrentHashMap<>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                ConfigurationUpdater remoteUpdater = locator.configurationUpdaterFactory().syncRemote(remote, config, latch);
                ConfigurationUpdater localUpdater = locator.configurationUpdaterFactory().syncLocal(local, config, latch);

                toExecuteRemote.add(remoteUpdater);
                toExecuteLocal.add(localUpdater);

                toExecuteGlobal.add(remoteUpdater);
                toExecuteGlobal.add(localUpdater);
            }
            for (ConfigurationUpdater thread : toExecuteGlobal) {
                thread.start();
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), ignored);

        } finally {
            interruptAll(toExecuteGlobal);
        }

        if (ConfigurationItemUpdateResult.countSuccess(remote.values()) < ConfigurationItemUpdateResult.countSuccess(local.values())) {
            for (Entry<Method, ConfigurationItemUpdateResult> each : local.entrySet()) {
                if (each.getValue().isSuccess()) {
                    methodValue.put(each.getKey(), each.getValue().getObject());
                }
            }
            Notifier.notify(listeners, toExecuteLocal, getName());
        } else {
            for (Entry<Method, ConfigurationItemUpdateResult> each : remote.entrySet()) {
                if (each.getValue().isSuccess()) {
                    methodValue.put(each.getKey(), each.getValue().getObject());
                }
            }
            Notifier.notify(listeners, toExecuteRemote, getName());
        }
        validateLoadResult();
    }

    private void waitFor(CountDownLatch latch) {
        try {
            LoggerHolder.getLog().debug(msg.format("waiting.load", getName()));
            latch.await();
            LoggerHolder.getLog().info(msg.format("end.load", getName()));
        } catch (InterruptedException ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), ignored);
        }
    }

    private void validateLoadResult() {
        if ((methodValue.size()) != data.getAll().size()) {
            throw new ReConfInitializationError(msg.format("error.missing.item", getName()));
        }

        for (MethodConfiguration config : data.getAll()) {
            if (null == config.getMethod()) {
                throw new ReConfInitializationError(msg.format("error.internal", getName()));
            }
        }
        commitTemporaryDatabaseChanges();
    }

    private void update() {
        List<ConfigurationUpdater> toExecute = new ArrayList<>(data.getAtomicReload().size());
        CountDownLatch latch = new CountDownLatch(data.getAtomicReload().size());
        Map<Method, ConfigurationItemUpdateResult> updated = new ConcurrentHashMap<>();

        try {
            for (MethodConfiguration config : data.getAtomicReload()) {
                ConfigurationUpdater t = locator.configurationUpdaterFactory().remote(updated, config, latch);
                toExecute.add(t);
                t.start();
            }
            waitFor(latch);
            methodValue = mergeAtomicMethodObjectWith(updated);
            Notifier.notify(listeners, toExecute, getName());

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()));

        } finally {
            interruptAll(toExecute);
        }
    }

    private void interruptAll(List<? extends Thread> arg) {
        for (Thread t : arg) {
            try {
                t.interrupt();
            } catch (Exception ignored) { }
        }
    }

    private Map<Method,Object> mergeAtomicMethodObjectWith(Map<Method, ConfigurationItemUpdateResult> updated) {
        if (!shouldMerge(updated)) {
            return methodValue;
        }

        Map<Method,Object> result = new ConcurrentHashMap<>();
        for (Entry<Method, Object> each : methodValue.entrySet()) {
            ConfigurationItemUpdateResult updateResult = updated.get(each.getKey());
            if (updateResult == null || !updateResult.isSuccess() || updateResult.getType() != ConfigurationItemUpdateResult.Type.update) {
                result.put(each.getKey(), each.getValue());
            } else {
                result.put(each.getKey(), updateResult.getObject());
            }
        }
        commitTemporaryDatabaseChanges();
        return result;
    }

    private boolean shouldMerge(Map<Method, ConfigurationItemUpdateResult> updated) {
        List<String> notFound = new ArrayList<>();
        for (Entry<Method, Object> each : methodValue.entrySet()) {
            if (updated.get(each.getKey()) == null) {
                notFound.add(msg.format("error.retrieving.item", getName(), each.getKey()));
            }
        }
        if (notFound.isEmpty()) {
            return true;
        }

        LoggerHolder.getLog().warn(StringUtils.join(notFound, LineSeparator.value()));
        LoggerHolder.getLog().warn(msg.format("error.retrieving.all.items", getName()));
        return false;
    }

    private void sync(Class<? extends RuntimeException> cls) {
        LoggerHolder.getLog().info(msg.format("sync.start", getName()));
        List<ConfigurationUpdater> toExecute = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(data.getAll().size());
        Map<Method, ConfigurationItemUpdateResult> updateAtomic = new ConcurrentHashMap<>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                toExecute.add(locator.configurationUpdaterFactory().syncRemote(updateAtomic, config, latch));
            }
            for (Thread thread : toExecute) {
                thread.start();
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("sync.error", getName()), ignored);

        } finally {
            interruptAll(toExecute);
        }

        if (ConfigurationItemUpdateResult.countSuccess(updateAtomic.values()) != data.getAll().size()) {
            String error = msg.format("sync.error", getName());
            try {
                Constructor<?> constructor = null;
                constructor = cls.getConstructor(String.class);
                constructor.setAccessible(true);
                throw cls.cast(constructor.newInstance(error));

            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignored) {
                throw new UpdateConfigurationRepositoryException(error);
            }
        }
        finishSync(updateAtomic);
        Notifier.notify(listeners, toExecute, getName());
        LoggerHolder.getLog().info(msg.format("sync.end", getName()));
    }

    private void finishSync(Map<Method, ConfigurationItemUpdateResult> updateAtomic) {
        Map<Method,Object> mergedAtomic = new ConcurrentHashMap<>();
        for (Entry<Method, Object> each : methodValue.entrySet()) {
            mergedAtomic.put(each.getKey(), (!updateAtomic.containsKey(each.getKey()) ? each.getValue() : updateAtomic.get(each.getKey()).getObject()));
        }

        this.methodValue = mergedAtomic;
        commitTemporaryDatabaseChanges();
    }

    private void commitTemporaryDatabaseChanges() {
        locator.databaseManagerLocator().find().commitTemporaryUpdate(cfgRepository.getFullProperties(), cfgRepository.getInterfaceClass());
    }

    public int getReloadRate() {
        return cfgRepository.getRate();
    }

    public TimeUnit getReloadTimeUnit() {
        return cfgRepository.getTimeUnit();
    }

    public Object getValueOf(Method m) {
        return methodValue.containsKey(m) ? methodValue.get(m) : null;
    }

    @Override
    public void stopIt() {
        try {
            super.interrupt();
        } catch (Exception ignored) { }
    }

    @Override
    public Object clone() {
        return new ConfigurationRepositoryUpdater(cfgRepository, locator, factory);
    }

    @Override
    public List<ObservableThread> getChildren() {
        return Collections.EMPTY_LIST;
    }
}

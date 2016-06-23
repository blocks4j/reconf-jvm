package org.blocks4j.reconf.client.config.update;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.setup.Environment;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.infra.log.LoggerHolder;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.throwables.UpdateConfigurationRepositoryException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ConfigurationRepositoryUpdater implements Runnable, ShutdownBean {

    private final ConnectionSettings connectionSettings;
    private ConfigurationRepositoryElement configurationRepositoryElement;
    private ConfigurationRepository repository;
    private List<RemoteConfigurationItemRequisitor> remoteConfigurationItemRequisitors;

    private ExecutorService executorService;

    public ConfigurationRepositoryUpdater(Environment environment, ConfigurationRepository repository, ConfigurationRepositoryElement configurationRepositoryElement) {
        this.repository = repository;
        this.connectionSettings = environment.getConnectionSettings();
        this.configurationRepositoryElement = configurationRepositoryElement;

        this.loadExecutorService();
        this.loadRemoteRequisitors(environment);

        this.run();

        environment.manageShutdownObject(this);
    }

    private void loadExecutorService() {
        this.executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("reconf-requisitor-" + ++threadCount);
                return thread;
            }
        });


    }

    private void loadRemoteRequisitors(Environment environment) {
        this.remoteConfigurationItemRequisitors = new ArrayList<>();

        this.configurationRepositoryElement.getConfigurationItems().forEach(configurationItemElement ->
                                                                                    this.remoteConfigurationItemRequisitors.add(new RemoteConfigurationItemRequisitor(environment, configurationItemElement))
                                                                           );

        this.remoteConfigurationItemRequisitors = Collections.synchronizedList(this.remoteConfigurationItemRequisitors);
    }

    @Override
    public void run() {
        try {
            syncNow();
        } catch (Throwable throwable) {
            LoggerHolder.getLog().warn("Error", throwable);
        }
    }

    public void syncNow() {
        this.syncNow(UpdateConfigurationRepositoryException.class);
    }

    public void syncNow(Class<? extends RuntimeException> exceptionClass) {
        List<ConfigurationItemUpdateResult> fullSyncResult = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture = CompletableFuture.completedFuture(fullSyncResult);

        try {
            for (RemoteConfigurationItemRequisitor requisitor : this.remoteConfigurationItemRequisitors) {
                fullSyncResultFuture = this.appendAsyncConfigurationSyncJob(fullSyncResultFuture, requisitor);
            }

            fullSyncResult = fullSyncResultFuture.get(this.connectionSettings.getTimeout(), this.connectionSettings.getTimeUnit());
        } catch (Throwable throwable) {
            this.throwException(exceptionClass, throwable);
        }

        if (hasError(fullSyncResult)) {
            this.throwException(exceptionClass);
        }
    }

    private boolean hasError(Collection<ConfigurationItemUpdateResult> updateResults) {
        Optional<ConfigurationItemUpdateResult> updateErrorSample = updateResults.stream()
                                                                                 .filter(ConfigurationItemUpdateResult::isFailure).findFirst();
        return updateErrorSample.isPresent();
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass) {
        try {
            Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class);
            constructor.setAccessible(true);
            throw constructor.newInstance("Error");
        } catch (Exception ignored) {
            throw new UpdateConfigurationRepositoryException("Error");
        }
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass, Throwable cause) {
        try {
            Constructor<? extends RuntimeException> constructor = exceptionClass.getConstructor(String.class, Throwable.class);
            constructor.setAccessible(true);
            throw constructor.newInstance("Error", cause);
        } catch (Exception ignored) {
            throw new UpdateConfigurationRepositoryException("Error", cause);
        }
    }

    private CompletableFuture<List<ConfigurationItemUpdateResult>> appendAsyncConfigurationSyncJob(CompletableFuture<List<ConfigurationItemUpdateResult>> fullSyncResultFuture, RemoteConfigurationItemRequisitor requisitor) {
        return fullSyncResultFuture.thenCombine(CompletableFuture.supplyAsync(requisitor::doRequest, this.executorService),
                                                (fullSyncResult, currentResult) -> {
                                                    if (currentResult.isSuccess()) {
                                                        currentResult = this.repository.update(currentResult);
                                                    }
                                                    fullSyncResult.add(currentResult);
                                                    return fullSyncResult;
                                                });
    }

    public Class<?> getRespositoryClass() {
        return this.configurationRepositoryElement.getInterfaceClass();
    }

    @Override
    public void shutdown() {
        this.executorService.shutdown();
    }
}

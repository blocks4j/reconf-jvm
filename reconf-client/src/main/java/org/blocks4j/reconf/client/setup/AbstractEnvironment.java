package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.infra.shutdown.ShutdownInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEnvironment implements Environment {

    private List<ShutdownBean> managedShutdownObjects;

    public AbstractEnvironment() {
        this.reset();

        new ShutdownInterceptor(this).register();
    }

    private void reset() {
        this.managedShutdownObjects = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public void manageShutdownObject(ShutdownBean shutdownBean) {
        if (shutdownBean != null) {
            this.managedShutdownObjects.add(shutdownBean);
        }
    }

    @Override
    public void shutdown() {
        this.managedShutdownObjects.forEach(ShutdownBean::shutdown);

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.reset();
    }

    public ConnectionSettings getConnectionSettings() {
        return this.getReconfConfiguration().getConnectionSettings();
    }
}
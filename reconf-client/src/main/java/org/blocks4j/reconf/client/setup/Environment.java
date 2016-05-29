package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.factory.ServerStubFactory;
import org.blocks4j.reconf.client.factory.ServerStubFactoryImpl;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;
import org.blocks4j.reconf.infra.shutdown.ShutdownInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Environment implements ShutdownBean {

    public static final ServerStubFactoryImpl SERVER_STUB_FACTORY = new ServerStubFactoryImpl();

    private List<ShutdownBean> managedShutdownObjects;

    public Environment() {
        this.reset();

        new ShutdownInterceptor(this).register();
    }

    private void reset() {
        this.managedShutdownObjects = Collections.synchronizedList(new ArrayList<>());
    }

    public void manageShutdownObject(ShutdownBean shutdownBean) {
        if (shutdownBean != null) {
            this.managedShutdownObjects.add(shutdownBean);
        }
    }

    @Override
    public void shutdown() {
        this.managedShutdownObjects.forEach(ShutdownBean::shutdown);

        this.reset();
    }

    public abstract ReconfConfiguration getDefaultConfiguration();

    public ServerStubFactory getServerStubFactory() {
        return SERVER_STUB_FACTORY;
    }
}
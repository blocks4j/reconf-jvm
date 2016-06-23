package org.blocks4j.reconf.client.setup;

import org.blocks4j.reconf.client.config.ConfigurationRepository;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.blocks4j.reconf.infra.http.ReconfServer;
import org.blocks4j.reconf.infra.shutdown.ShutdownBean;

public interface Environment extends ShutdownBean {

    ReconfConfiguration getReconfConfiguration();

    ReconfServer getReconfServerStub();

    ConfigurationRepository getRepository();

    void manageShutdownObject(ShutdownBean shutdownBean);

    ConnectionSettings getConnectionSettings();
}

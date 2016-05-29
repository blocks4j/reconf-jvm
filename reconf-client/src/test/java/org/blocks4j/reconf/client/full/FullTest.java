package org.blocks4j.reconf.client.full;

import org.apache.http.HttpHost;
import org.apache.http.localserver.LocalServerTestBase;
import org.blocks4j.reconf.client.factory.ConfigurationRepositoryFactory;
import org.blocks4j.reconf.client.full.simulator.ReconfServerSimulator;
import org.blocks4j.reconf.client.setup.config.ConnectionSettings;
import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;
import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SeparateClassloaderTestRunner.class)
public class FullTest extends LocalServerTestBase {

    private HttpHost host;
    private ConfigurationRepositoryFactory configurationRepositoryFactory;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.serverBootstrap.registerHandler("/*", new ReconfServerSimulator(this.getClass().getClassLoader().getResource("database.json")));

        this.host = this.start();

        File tempConf = File.createTempFile("reconf", ".properties");
        tempConf.deleteOnExit();

        ReconfConfiguration reconfConfiguration = new ReconfConfiguration();

        LocalCacheSettings localCacheSettings = new LocalCacheSettings();
        localCacheSettings.setBackupLocation(tempConf.getParentFile());
        reconfConfiguration.setLocalCacheSettings(localCacheSettings);

        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setUrl(String.format("http://%s:%s", host.getHostName(), host.getPort()));

        reconfConfiguration.setConnectionSettings(connectionSettings);

        this.configurationRepositoryFactory = new ConfigurationRepositoryFactory(reconfConfiguration);


    }


    @Test
    public void testTypes() throws Exception {
        ReconfTest reconfTest = this.configurationRepositoryFactory.get(ReconfTest.class);


        Assert.assertNotNull(reconfTest.getInteger());
        Assert.assertNotNull(reconfTest.getIntegerPrimitive());
        Assert.assertNotNull(reconfTest.getBoolean());
        Assert.assertNotNull(reconfTest.getBooleanPrimitive());
        Assert.assertNotNull(reconfTest.getDouble());
        Assert.assertNotNull(reconfTest.getDoublePrimitive());
        Assert.assertNotNull(reconfTest.getLong());
        Assert.assertNotNull(reconfTest.getLongPrimitive());
        Assert.assertNotNull(reconfTest.getIntegerList());
        Assert.assertNotNull(reconfTest.getIntegerSet());
        Assert.assertNotNull(reconfTest.getIntegerSet_empty());
        Assert.assertTrue(reconfTest.getIntegerSet_empty().isEmpty());
        Assert.assertNotNull(reconfTest.getIntegerStringMap());

        Map<Integer, Set<String>> integerSetStringMap_allFull = reconfTest.getIntegerSetStringMap_AllFull();
        Assert.assertNotNull(integerSetStringMap_allFull);
        for (Map.Entry<Integer, Set<String>> integerSetStringMap_allFullEntry : integerSetStringMap_allFull.entrySet()) {
            Assert.assertNotNull(integerSetStringMap_allFullEntry.getKey());
            Assert.assertNotNull(integerSetStringMap_allFullEntry.getValue());
            Assert.assertFalse(integerSetStringMap_allFullEntry.getValue().isEmpty());
        }

        Map<Integer, Set<String>> integerSetStringMap_someEmpty = reconfTest.getIntegerSetStringMap_SecondEmpty();
        Assert.assertNotNull(integerSetStringMap_allFull);
        for (Map.Entry<Integer, Set<String>> integerSetStringMap_allFullEntry : integerSetStringMap_allFull.entrySet()) {
            Assert.assertNotNull(integerSetStringMap_allFullEntry.getKey());
            Assert.assertNotNull(integerSetStringMap_allFullEntry.getValue());
        }

        Map<Integer, Map<String, Set<String>>> integerMapStringSetStringMap_AllFull = reconfTest.getIntegerMapStringSetStringMap_AllFull();
        Assert.assertNotNull(integerMapStringSetStringMap_AllFull);
        for (Map.Entry<Integer, Map<String, Set<String>>> integerMapStringSetStringMap_AllFullEntry : integerMapStringSetStringMap_AllFull.entrySet()) {
            Assert.assertNotNull(integerMapStringSetStringMap_AllFullEntry.getKey());
            Assert.assertNotNull(integerMapStringSetStringMap_AllFullEntry.getValue());
            Assert.assertFalse(integerMapStringSetStringMap_AllFullEntry.getValue().isEmpty());

            for (Map.Entry<String, Set<String>> stringSetEntry : integerMapStringSetStringMap_AllFullEntry.getValue().entrySet()) {
                Assert.assertNotNull(stringSetEntry.getKey());
                Assert.assertNotNull(stringSetEntry.getValue());
                Assert.assertFalse(stringSetEntry.getValue().isEmpty());
            }

        }

        Map<Integer, Map<String, Set<String>>> integerMapStringSetStringMap_oneEmpty = reconfTest.getIntegerMapStringSetStringMap_OneEmpty();
        Assert.assertNotNull(integerMapStringSetStringMap_oneEmpty);
        for (Map.Entry<Integer, Map<String, Set<String>>> integerMapStringSetStringMap_AllFullEntry : integerMapStringSetStringMap_oneEmpty.entrySet()) {
            Assert.assertNotNull(integerMapStringSetStringMap_AllFullEntry.getKey());
            Assert.assertNotNull(integerMapStringSetStringMap_AllFullEntry.getValue());

            for (Map.Entry<String, Set<String>> stringSetEntry : integerMapStringSetStringMap_AllFullEntry.getValue().entrySet()) {
                Assert.assertNotNull(stringSetEntry.getKey());
                Assert.assertNotNull(stringSetEntry.getValue());
            }
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        this.configurationRepositoryFactory.getEnvironment().shutdown();
    }
}

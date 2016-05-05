package org.blocks4j.reconf.client.full;

import org.apache.http.HttpHost;
import org.apache.http.localserver.LocalServerTestBase;
import org.blocks4j.reconf.client.full.simulator.ReconfServerSimulator;
import org.blocks4j.reconf.client.proxy.ConfigurationRepositoryFactory;
import org.blocks4j.reconf.client.setup.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@RunWith(SeparateClassloaderTestRunner.class)
public class FullTest extends LocalServerTestBase {

    private HttpHost host;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.serverBootstrap.registerHandler("/*", new ReconfServerSimulator(this.getClass().getClassLoader().getResource("database.json")));

        this.host = this.start();

        File tempConf = File.createTempFile("reconf", ".properties");
        tempConf.deleteOnExit();


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempConf));
        bufferedWriter.write("server.url=");
        bufferedWriter.write(String.format("http://%s:%s", host.getHostName(), host.getPort()));
        bufferedWriter.newLine();
        bufferedWriter.write("local.cache.location=");
        bufferedWriter.write(tempConf.getParent() + "/cache" + System.currentTimeMillis());
        bufferedWriter.flush();
        bufferedWriter.close();

        System.setProperty(Environment.SYSTEM_PROPERTY, tempConf.getAbsolutePath());
    }


    @Test
    public void testTypes() throws Exception {
        ReconfTest reconfTest = ConfigurationRepositoryFactory.get(ReconfTest.class);

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
        Assert.assertNotNull(reconfTest.getIntegerStringMap());
    }


}

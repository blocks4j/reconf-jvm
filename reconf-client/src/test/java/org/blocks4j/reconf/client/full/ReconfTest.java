package org.blocks4j.reconf.client.full;

import org.blocks4j.reconf.client.annotations.ConfigurationItem;
import org.blocks4j.reconf.client.annotations.ConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ConfigurationRepository(product = "reconf", component = "client", pollingRate = 1, pollingTimeUnit = TimeUnit.HOURS)
public interface ReconfTest {

    @ConfigurationItem("Integer")
    Integer getInteger();

    @ConfigurationItem("int")
    int getIntegerPrimitive();

    @ConfigurationItem("Double")
    Double getDouble();

    @ConfigurationItem("double")
    double getDoublePrimitive();

    @ConfigurationItem("Boolean")
    Boolean getBoolean();

    @ConfigurationItem("boolean")
    boolean getBooleanPrimitive();

    @ConfigurationItem("Long")
    Long getLong();

    @ConfigurationItem("long")
    long getLongPrimitive();

    @ConfigurationItem("IntegerList")
    List<Integer> getIntegerList();

    @ConfigurationItem("IntegerSet")
    Set<Integer> getIntegerSet();

    @ConfigurationItem("IntegerStringMap")
    Map<Integer, String> getIntegerStringMap();


}

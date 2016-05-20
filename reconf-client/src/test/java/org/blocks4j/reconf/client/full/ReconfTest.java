package org.blocks4j.reconf.client.full;

import org.blocks4j.reconf.client.adapters.DefaultAntlr4ConfigurationAdapter;
import org.blocks4j.reconf.client.annotations.ConfigurationItem;
import org.blocks4j.reconf.client.annotations.ConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ConfigurationRepository(product = "reconf", component = "client", pollingRate = 1, pollingTimeUnit = TimeUnit.HOURS)
public interface ReconfTest {

    @ConfigurationItem(value = "Integer", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Integer getInteger();

    @ConfigurationItem(value = "int", adapter = DefaultAntlr4ConfigurationAdapter.class)
    int getIntegerPrimitive();

    @ConfigurationItem(value = "Double", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Double getDouble();

    @ConfigurationItem(value = "double", adapter = DefaultAntlr4ConfigurationAdapter.class)
    double getDoublePrimitive();

    @ConfigurationItem(value = "Boolean", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Boolean getBoolean();

    @ConfigurationItem(value = "boolean", adapter = DefaultAntlr4ConfigurationAdapter.class)
    boolean getBooleanPrimitive();

    @ConfigurationItem(value = "Long", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Long getLong();

    @ConfigurationItem(value = "long", adapter = DefaultAntlr4ConfigurationAdapter.class)
    long getLongPrimitive();

    @ConfigurationItem(value = "IntegerList", adapter = DefaultAntlr4ConfigurationAdapter.class)
    List<Integer> getIntegerList();

    @ConfigurationItem(value = "IntegerSet", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Set<Integer> getIntegerSet();

    @ConfigurationItem(value = "IntegerSet_empty", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Set<Integer> getIntegerSet_empty();

    @ConfigurationItem(value = "IntegerStringMap", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Map<Integer, String> getIntegerStringMap();

    @ConfigurationItem(value = "IntegerSetStringMap_AllFull", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Map<Integer, Set<String>> getIntegerSetStringMap_AllFull();

    @ConfigurationItem(value = "IntegerSetStringMap_oneEmpty", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Map<Integer, Set<String>> getIntegerSetStringMap_SecondEmpty();

    @ConfigurationItem(value = "IntegerMapStringSetStringMap_AllFull", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Map<Integer, Map<String, Set<String>>> getIntegerMapStringSetStringMap_AllFull();

    @ConfigurationItem(value = "IntegerMapStringSetStringMap_oneEmpty", adapter = DefaultAntlr4ConfigurationAdapter.class)
    Map<Integer, Map<String, Set<String>>> getIntegerMapStringSetStringMap_OneEmpty();


}

package org.blocks4j.reconf.client.full.simulator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class ReconfServerMemoryDatabase {
    private Map<String, String> database;

    public ReconfServerMemoryDatabase() {
        this.database = new HashMap<String, String>();
    }

    public Map<String, String> getDatabase() {
        return this.database;
    }

    public void setDatabase(Map<String, String> database) {
        this.database = database;
    }

    @JsonIgnore
    public String getPropertyValue(String extractKey) {
        return this.database.get(extractKey);
    }
}

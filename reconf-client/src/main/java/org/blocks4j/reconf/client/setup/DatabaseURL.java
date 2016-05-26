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
package org.blocks4j.reconf.client.setup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.system.LocalHostname;

/**
 * http://hsqldb.org/doc/2.0/guide/dbproperties-chapt.html
 */
@SuppressWarnings("serial")
public class DatabaseURL {

    private static final MessagesBundle msg = MessagesBundle.getBundle(DatabaseURL.class);
    private static final String baseURL = "jdbc:hsqldb:file:";
    private static final String driverClassName = "org.hsqldb.jdbc.JDBCDriver";

    private String location;
    private Map<String, String> initialParams = new LinkedHashMap<>();
    private Map<String, String> runtimeParams = new LinkedHashMap<>();

    private static final String cryptKey;

    static {
        try {
            SecretKeySpec key = new SecretKeySpec(getSecretKey(16).getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cryptKey = new String(Hex.encodeHex(cipher.doFinal("reconf_localdb".getBytes())));
        } catch (Exception e) {
            throw new Error(msg.get("error.crypt.key"), e);
        }
    }

    private static String getSecretKey(int keySize) {
        String key = LocalHostname.getName();
        int missing = keySize - StringUtils.length(key);
        if (missing == 0) {
            return key;
        }
        if (missing < 0) {
            return StringUtils.substring(key, 0, keySize);
        }
        return key + StringUtils.repeat("#", missing);
    }

    private static final Map<String, String> baseParams = new LinkedHashMap<String, String>() {
        {
            put("hsqldb.lock_file", "false");
            put("shutdown", "true");
        }
    };

    private static final Map<String, String> cryptParams = new LinkedHashMap<String, String>() {
        {
            put("crypt_key", cryptKey);
            put("crypt_type", "AES");
            put("crypt_lobs", "true");
        }
    };

    private static final Map<String, String> baseInitialParams = new LinkedHashMap<String, String>() {
        {
            putAll(baseParams);
        }
    };

    private static final Map<String, String> baseRuntimeParams = new LinkedHashMap<String, String>() {
        {
            putAll(baseParams);
            put("ifexists", "true");
        }
    };

    private DatabaseURL() { }

    static DatabaseURL location(String arg) {
        DatabaseURL dbURL = new DatabaseURL();
        dbURL.location = arg;
        dbURL.initialParams.putAll(baseInitialParams);
        dbURL.runtimeParams.putAll(baseRuntimeParams);
        return dbURL;
    }

    DatabaseURL encrypted() {
        initialParams.putAll(cryptParams);
        runtimeParams.putAll(cryptParams);
        return this;
    }

    DatabaseURL notEncrypted() {
        initialParams.putAll(baseInitialParams);
        runtimeParams.putAll(baseRuntimeParams);
        return this;
    }

    DatabaseURL compressed() {
        initialParams.put("hsqldb.script_format", "3");
        runtimeParams.put("hsqldb.script_format", "3");
        return this;
    }

    DatabaseURL notCompressed() {
        initialParams.put("hsqldb.script_format", "0");
        runtimeParams.put("hsqldb.script_format", "0");
        return this;
    }

    DatabaseURL maxLogFileSize(int size) {
        initialParams.put("hsqldb.log_size", String.valueOf(size));
        runtimeParams.put("hsqldb.script_format", String.valueOf(size));
        return this;
    }

    String buildInitalURL() {
        return baseURL + location + buildString(initialParams);
    }

    String buildRuntimeURL() {
        return baseURL + location + buildString(runtimeParams);
    }

    private String buildString(Map<String, String> params) {
        List<String> result = new ArrayList<>();
        for (Entry<String, String> each : params.entrySet()) {
            result.add(each.getKey() + "=" + each.getValue());
        }
        return ";" + StringUtils.join(result, ";");
    }

    String getDriverClassName() {
        return driverClassName;
    }

    String getLogin() {
        return "reconfdb";
    }

    String getPass() {
        return "local";
    }
}

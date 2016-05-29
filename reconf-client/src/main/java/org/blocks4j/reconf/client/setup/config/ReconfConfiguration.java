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
package org.blocks4j.reconf.client.setup.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;

public class ReconfConfiguration {

    private LocalCacheSettings localCacheSettings;
    private ConnectionSettings connectionSettings;
    private boolean experimentalFeatures;
    private boolean debug;
    private String locale;

    public LocalCacheSettings getLocalCacheSettings() {
        return localCacheSettings;
    }
    public void setLocalCacheSettings(LocalCacheSettings localCacheSettings) {
        this.localCacheSettings = localCacheSettings;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public boolean isExperimentalFeatures() {
        return experimentalFeatures;
    }
    public void setExperimentalFeatures(boolean experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("local-cache", getLocalCacheSettings())
        .append("server", getConnectionSettings());
        if (experimentalFeatures) {
            result.append("experimental-features", experimentalFeatures);
        }
        if (debug) {
            result.append("debug", debug);
        }
        return result.toString();
    }
}

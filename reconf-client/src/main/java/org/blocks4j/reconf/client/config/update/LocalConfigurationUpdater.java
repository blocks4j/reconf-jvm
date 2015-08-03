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
package org.blocks4j.reconf.client.config.update;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.blocks4j.reconf.client.config.source.ConfigurationSource;
import org.blocks4j.reconf.client.config.source.ConfigurationSourceHolder;
import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult.Source;
import org.blocks4j.reconf.client.proxy.MethodConfiguration;
import org.blocks4j.reconf.infra.log.LoggerHolder;


public class LocalConfigurationUpdater extends ConfigurationUpdater {

    public LocalConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync) {
        super(toUpdate, target, sync);
    }

    public LocalConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync, CountDownLatch latch) {
        super(toUpdate, target, sync, latch);
    }

    protected String getUpdaterType() {
        return "local-cache";
    }

    protected void update() {

        String value = null;
        ConfigurationSource obtained = null;
        ConfigurationSourceHolder holder = null;

        try {
            if (Thread.currentThread().isInterrupted()) {
                releaseLatch();
                logInterruptedThread();
                return;
            }

            LoggerHolder.getLog().debug(msg.format("method.reload", getName(), methodCfg.getMethod().getName()));
            holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getDb().get();

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);
            updateLastResultWithError(Source.localCache, methodCfg.getConfigurationItemElement(), null, t);
            releaseLatch();
            return;
        }

        try {
            if (value != null) {
                obtained = holder.getDb();
            }

            if (value != null && obtained != null) {
                updateMap(value, false, obtained, ConfigurationItemUpdateResult.Source.localCache);
                LoggerHolder.getLog().debug(msg.format("method.done", getName(), methodCfg.getMethod().getName()));
            }

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);

        } finally {
            releaseLatch();
        }
    }
}

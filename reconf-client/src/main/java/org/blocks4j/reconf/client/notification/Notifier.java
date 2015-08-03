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
package org.blocks4j.reconf.client.notification;

import java.util.Collection;
import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;
import org.blocks4j.reconf.client.config.update.ConfigurationUpdater;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;

public class Notifier {

    private static final MessagesBundle msg = MessagesBundle.getBundle(Notifier.class);

    public static void notify(Collection<ConfigurationItemListener> listeners, Collection<ConfigurationUpdater> toExecute, String name) {
        for (ConfigurationItemListener listener : listeners) {
            for (ConfigurationUpdater updater : toExecute) {
                ConfigurationItemUpdateResult result = updater.getLastResult();
                if (result == null) {
                    continue;
                }
                NotificationHeader header = new SimpleNotificationHeader(result);
                if (result.getType() == ConfigurationItemUpdateResult.Type.update && listener.shouldNotifyUpdate(header)) {
                    notifyUpdate(name, listener, new StandardUpdateNotification(updater.getLastResult()));
                }
                if (result.getType() == ConfigurationItemUpdateResult.Type.error && listener.shouldNotifyError(header)) {
                    notifyError(name, listener, new StandardErrorNotification(updater.getLastResult()));
                }
            }
        }
    }

    private static void notifyUpdate(String name, ConfigurationItemListener listener, UpdateNotification event) {
        try {
            listener.onEvent(event);
        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.notify", name), t);
        }
    }

    private static void notifyError(String name, ConfigurationItemListener listener, ErrorNotification event) {
        try {
            listener.onEvent(event);
        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.notify", name), t);
        }
    }
}

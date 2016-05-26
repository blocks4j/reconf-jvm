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
package org.blocks4j.reconf.client.check;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.log.LoggerHolder;

public class ObserverThread extends Thread {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ObserverThread.class);
    private CopyOnWriteArrayList<ObservableThread> toWatch = new CopyOnWriteArrayList<>();

    public ObserverThread() {
        setName("reconf-thread-checker");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MINUTES.sleep(1);
                LoggerHolder.getLog().debug(msg.format("start", getName()));
                List<ObservableThread> threads = new ArrayList<>(toWatch);

                List<ObservableThread> toRemove = new ArrayList<>();
                List<ObservableThread> toAdd = new ArrayList<>();

                for (ObservableThread thread : threads) {
                    if (System.currentTimeMillis() - thread.getLastExecution() > (1.5F * thread.getReloadTimeUnit().toMillis(thread.getReloadRate()))) {
                        LoggerHolder.getLog().error(msg.format("not.running", getName(), thread.getName()));
                        toRemove.add(thread);
                        toAdd.add((ObservableThread) thread.clone());
                    }
                }

                for (ObservableThread rem : toRemove) {
                    toWatch.remove(rem);
                    rem.stopIt();
                    if (CollectionUtils.isNotEmpty(rem.getChildren())) {
                        for (ObservableThread child : rem.getChildren()) {
                            toWatch.remove(child);
                        }
                    }
                }

                for (ObservableThread add : toAdd) {
                    LoggerHolder.getLog().info(msg.format("thread.starting", getName(), add.getName()));
                    toWatch.add(add);
                    add.start();
                    LoggerHolder.getLog().info(msg.format("thread.running", getName(), add.getName()));
                }

            } catch (Throwable t) {
                LoggerHolder.getLog().error(msg.format("error", getName(), ExceptionUtils.getStackTrace(t)));
            }
        }
    }

    public void add(ObservableThread thread) {
        if (thread != null) {
            toWatch.add(thread);
        }
    }

    public List<ObservableThread> getActiveThreads() {
        return new ArrayList<>(toWatch);
    }
}

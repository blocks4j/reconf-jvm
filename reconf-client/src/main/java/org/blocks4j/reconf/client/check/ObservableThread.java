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

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class ObservableThread extends Thread implements Cloneable {

    private volatile long lastExecution = System.currentTimeMillis();

    protected void updateLastExecution() {
        lastExecution = System.currentTimeMillis();
    }

    public long getLastExecution() {
        return lastExecution;
    }

    public abstract int getReloadRate();

    public abstract TimeUnit getReloadTimeUnit();

    public abstract void stopIt();

    public abstract List<ObservableThread> getChildren();

    @Override
    public abstract Object clone() throws CloneNotSupportedException;
}

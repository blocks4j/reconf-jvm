/*
 *   Copyright 2013-2016 Blocks4J Team (www.blocks4j.org)
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
package org.blocks4j.reconf.spring.boot.actuator.domain;

public class SyncResult {

    private final String name;
    private final Throwable throwable;

    public SyncResult(String name) {
        this.name = name;
        this.throwable = null;
    }

    public SyncResult(String name, Throwable throwable) {
        this.name = name;
        this.throwable = throwable;
    }

    public String getName() {
        return name;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
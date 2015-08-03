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
package org.blocks4j.reconf.infra.i18n;

import org.apache.commons.lang3.StringUtils;

public class BundleSettings {

    private final String className;
    private final String headPackageName;
    private final String tailPackageName;

    public BundleSettings(Class<?> cls) {
        className = cls.getSimpleName();
        String[] packages = StringUtils.split(cls.getPackage().getName(), '.');
        if (packages.length == 0 || packages.length == 1) {
            throw new IllegalArgumentException("only meant to be used inside reconf");
        }

        headPackageName = packages[3];
        if (packages.length >= 2) {
            tailPackageName = StringUtils.substringAfter(cls.getPackage().getName(), "reconf." + headPackageName + ".");
        } else {
            tailPackageName = StringUtils.EMPTY;
        }
    }

    public String getClassName() {
        return className;
    }

    public String getHeadPackageName() {
        return headPackageName;
    }

    public String getTailPackageName() {
        return tailPackageName;
    }

    public String getBundleResourceName() {
        return "messages_" + getHeadPackageName();
    }
}

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

import java.util.Locale;
import java.util.ResourceBundle;


public class MessagesBundle {

    private ResourceBundle bundle;
    private final BundleSettings settings;
    private Locale locale;

    private MessagesBundle(Class<?> cls) {
        this.locale = LocaleHolder.value();
        this.settings = new BundleSettings(cls);
        this.bundle = ResourceBundle.getBundle(settings.getBundleResourceName(), locale);
    }

    public static MessagesBundle getBundle(Class<?> cls) {
        return new MessagesBundle(cls);
    }

    public String get(String key) {
        updateBundleIfNeeded();
        return bundle.getString(getPath(key));
    }

    public String format(String key, Object...args) {
        updateBundleIfNeeded();
        return String.format(bundle.getString(getPath(key)), args);
    }

    private String getPath(String key) {
        updateBundleIfNeeded();
        return settings.getTailPackageName() + "." + settings.getClassName() + "." + key;
    }

    private void updateBundleIfNeeded() {
        if (LocaleHolder.value() != null && !LocaleHolder.value().equals(this.locale)) {
            this.locale = LocaleHolder.value();
            this.bundle = ResourceBundle.getBundle(settings.getBundleResourceName(), locale);
        }
    }
}

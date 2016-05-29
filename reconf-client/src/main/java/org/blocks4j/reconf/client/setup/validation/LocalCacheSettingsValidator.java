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
package org.blocks4j.reconf.client.setup.validation;

import org.blocks4j.reconf.client.setup.config.LocalCacheSettings;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class LocalCacheSettingsValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(LocalCacheSettings.class);

    public static void validate(LocalCacheSettings arg, Collection<String> errors) {
        if (arg == null) {
            errors.add(msg.get("null"));
        } else {
            checkMaxLogFileSize(arg, errors);
            checkBackupLocation(arg, errors);
        }
    }

    public static Set<String> validate(LocalCacheSettings arg) {
        Set<String> errors = new LinkedHashSet<String>();

        validate(arg, errors);

        return errors;
    }

    private static void checkMaxLogFileSize(LocalCacheSettings arg, Collection<String> errors) {
        if (arg.getMaxLogFileSize() < 1 || arg.getMaxLogFileSize() > 50) {
            errors.add(msg.get("backup.max.log.error"));
        }
    }

    private static void checkBackupLocation(LocalCacheSettings arg, Collection<String> errors) {
        if (arg.getBackupLocation() == null) {
            errors.add(msg.get("backup.location.error.null"));
        }
    }
}

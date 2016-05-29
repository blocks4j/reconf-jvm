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

import org.blocks4j.reconf.client.setup.config.ReconfConfiguration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class PropertiesConfigurationValidator {

    public static void validate(ReconfConfiguration arg, Set<String> errors) {
        checkLocalCacheSettings(arg, errors);
        checkConnectionSettings(arg, errors);
    }

    public static Set<String> validate(ReconfConfiguration arg) {
        Set<String> errors = new LinkedHashSet<String>();

        validate(arg, errors);

        return errors;
    }

    private static void checkLocalCacheSettings(ReconfConfiguration arg, Collection<String> errors) {
        LocalCacheSettingsValidator.validate(arg.getLocalCacheSettings(), errors);
    }

    private static void checkConnectionSettings(ReconfConfiguration arg, Collection<String> errors) {
        ConnectionSettingsValidator.validate(arg.getConnectionSettings(), errors);
    }
}

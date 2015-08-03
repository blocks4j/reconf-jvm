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
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.log.LoggerHolder;


public final class LocaleHolder {

    private static String providedLocale;

    public static void set(String value) {
        if (providedLocale == null) {
            providedLocale = value;
        }
    }

    public static Locale value() {
        if (StringUtils.isNotBlank(providedLocale)) {
            try {
                return LocaleUtils.toLocale(providedLocale);
            } catch (IllegalArgumentException e) {
                LoggerHolder.getLog().error(String.format("invalid locale [%s]. assuming default [%s]", providedLocale, Locale.getDefault()));
            }
        }
        return Locale.getDefault();
    }
}

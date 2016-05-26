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
package org.blocks4j.reconf.client.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.setup.ConnectionSettings;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;

public class ConnectionSettingsValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConnectionSettings.class);

    public static Set<String> validate(ConnectionSettings arg) {
        Set<String> errors = new LinkedHashSet<>();
        if (arg == null) {
            errors.add(msg.get("null"));
            return errors;
        }

        checkURL(arg, errors);
        checkTimeout(arg, errors);
        checkTimeUnit(arg, errors);
        checkMaxRetry(arg, errors);
        return errors;
    }

    private static void checkURL(ConnectionSettings arg, Collection<String> errors) {
        if (arg.getUrl() == null) {
            errors.add(getDefaultUrlError());
            return;
        }
        if (StringUtils.isBlank(arg.getUrl())) {
            errors.add(getDefaultUrlError());
        }
        try {
            new URL(arg.getUrl());
        } catch (MalformedURLException e) {
            errors.add(getDefaultUrlError());
        }
    }

    private static String getDefaultUrlError() {
        return msg.get("url.error");
    }

    private static void checkTimeout(ConnectionSettings arg, Collection<String> errors) {
        if (arg.getTimeout() < 1) {
            errors.add(msg.get("timeout.error"));
        }
    }

    private static void checkTimeUnit(ConnectionSettings arg, Collection<String> errors) {
        if (arg.getTimeUnit() == null) {
            errors.add(msg.get("timeUnit.null"));
            return;
        }
        if (!EnumSet.of(TimeUnit.SECONDS,TimeUnit.MINUTES,TimeUnit.HOURS,TimeUnit.DAYS).contains(arg.getTimeUnit())) {
            errors.add(msg.get("timeUnit.null"));
        }
    }

    private static void checkMaxRetry(ConnectionSettings arg, Collection<String> errors) {
        if (arg.getMaxRetry() < 1 || arg.getMaxRetry() > 5) {
            errors.add(msg.get("retry.error"));
        }
    }
}

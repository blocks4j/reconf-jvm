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
package org.blocks4j.reconf.infra.system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import org.apache.commons.lang3.StringUtils;


public class LocalHostname {

    public static String getName() {
        String runtime = runtimeStrategy();
        String fallback = fallbackStrategy();
        return runtime != null ? runtime : fallback != null ? fallback : "";
    }

    private static String fallbackStrategy() {
        try {
            return StringUtils.stripAccents(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            return null;
        }
    }

    private static String runtimeStrategy() {
        BufferedReader in = null;
        try {
            Process proc = Runtime.getRuntime().exec("hostname");
            proc.waitFor();
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            return StringUtils.stripAccents(in.readLine());
        } catch (Exception e) {
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {}
            }
        }
    }
}

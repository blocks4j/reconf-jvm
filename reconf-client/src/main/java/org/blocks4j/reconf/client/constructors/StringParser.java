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
package org.blocks4j.reconf.client.constructors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.infra.i18n.MessagesBundle;
import org.blocks4j.reconf.infra.system.LineSeparator;


public class StringParser {

    private static final MessagesBundle msg = MessagesBundle.getBundle(StringParser.class);
    private List<String> tokens = new ArrayList<String>();
    private final Stack<Character> openClose = new Stack<Character>();
    private MethodData data;

    public StringParser(MethodData arg) {
        this.data = arg;

        if (StringUtils.isEmpty(data.getValue())) {
            return;
        }

        String trimmed = StringUtils.defaultString(StringUtils.trim(data.getValue()));
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new RuntimeException(msg.format("error.complex.type", data.getMethod()));
        }

        String wholeValue = StringUtils.substring(trimmed, 1, trimmed.length()-1);
        extractTokens(wholeValue);
        if (openClose.size() % 2 != 0) {
            throw new IllegalArgumentException(msg.format("error.invalid.string", data.getValue(), data.getMethod()));
        }
    }


    private void extractTokens(String arg) {
        boolean colOpen = false;
        boolean valueOpen = false;
        boolean escaped = false;

        Stack<Character> token = new Stack<Character>();
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);

            if (colOpen) {
                if (']' == c) {
                    if (!valueOpen) {
                        token.push(c);
                        openClose.push(c);
                        colOpen = false;
                        StringBuilder sb = new StringBuilder(token.size());
                        for (Character each : token) {
                            sb.append(each);
                        }
                        tokens.add(sb.toString());
                        token.clear();
                        continue;
                    }
                }
                if ('\'' == c) {
                    if (!valueOpen) {
                        valueOpen = true;

                    } else if (valueOpen && escaped) {
                        token.pop();
                        escaped = false;

                    } else {
                        valueOpen = false;
                    }
                }
                if ('\\' == c) {
                    if (!escaped) {
                        token.push(c);
                        escaped = true;
                    } else {
                        escaped = false;
                    }
                    continue;
                }
                if (escaped) {
                    escaped = false;
                }
                token.push(c);
                continue;

            } else if (valueOpen) {
                if ('\'' == c) {
                    if (escaped) {
                        token.pop();
                        token.push(c);
                        escaped = false;
                    } else {
                        valueOpen = false;
                        token.push(c);
                        openClose.push(c);
                        StringBuilder sb = new StringBuilder(token.size());
                        for (Character each : token) {
                            sb.append(each);
                        }
                        tokens.add(sb.toString());
                        token.clear();
                    }
                    continue;
                }
                if ('\\' == c) {
                    if (!escaped) {
                        token.push(c);
                        escaped = true;
                    } else {
                        escaped = false;
                    }
                    continue;
                }
                if (escaped) {
                    escaped = false;
                }
                token.push(c);
                continue;
            }

            if ('[' == c) {
                colOpen = true;
                token.push(c);
                openClose.push(c);
                continue;
            }

            if ('\'' == c) {
                valueOpen = true;
                token.push(c);
                openClose.push(c);
                continue;
            }
        }

    }

    public List<String> getTokens() {
        return new ArrayList<String>(tokens);
    }

    public Map<String, String> getTokensAsMap() {
        if (tokens.size() % 2 != 0) {
            throw new IllegalStateException(msg.format("error.map", LineSeparator.value(), tokens, data.getMethod()));
        }

        Map<String, String> result = new LinkedHashMap<String, String>();
        String current = "";
        for (int i = 0; i < tokens.size(); i++) {
            if (i % 2 == 0) {
                current = tokens.get(i);
            } else {
                result.put(current, tokens.get(i));
            }
        }

        return result;
    }
}

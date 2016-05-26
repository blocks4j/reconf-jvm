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
package org.blocks4j.reconf.client.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.adapters.ConfigurationAdapter;
import org.blocks4j.reconf.client.adapters.DefaultAntlr4ConfigurationAdapter;


@Target(METHOD)
@Retention(RUNTIME)
public @interface ConfigurationItem {

    String value();
    String component() default StringUtils.EMPTY;
    String product() default StringUtils.EMPTY;
    Class<? extends ConfigurationAdapter<?>> adapter() default DefaultAntlr4ConfigurationAdapter.class;
}

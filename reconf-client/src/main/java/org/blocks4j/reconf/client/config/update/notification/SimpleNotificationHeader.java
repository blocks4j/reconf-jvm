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
package org.blocks4j.reconf.client.config.update.notification;

import org.blocks4j.reconf.client.config.update.ConfigurationItemUpdateResult;

public class SimpleNotificationHeader implements NotificationHeader {

    private final String product;
    private final String component;
    private final String item;

    public SimpleNotificationHeader(ConfigurationItemUpdateResult result) {
        this.product = result.getProduct();
        this.component = result.getComponent();
        this.item = result.getItem();
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public String getItem() {
        return item;
    }

}

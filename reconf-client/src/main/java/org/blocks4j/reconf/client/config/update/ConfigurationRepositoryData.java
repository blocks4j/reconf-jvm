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
package org.blocks4j.reconf.client.config.update;

import java.util.ArrayList;
import java.util.List;
import org.blocks4j.reconf.client.elements.ConfigurationItemElement;
import org.blocks4j.reconf.client.elements.ConfigurationRepositoryElement;
import org.blocks4j.reconf.client.locator.ServiceLocator;
import org.blocks4j.reconf.client.proxy.MethodConfiguration;


public class ConfigurationRepositoryData {

    private final ConfigurationRepositoryElement cfgRepository;
    private final List<MethodConfiguration> atomicReload = new ArrayList<>();
    private final List<MethodConfiguration> all = new ArrayList<>();
    private final ServiceLocator locator;

    public ConfigurationRepositoryData(ConfigurationRepositoryElement arg, ServiceLocator locator) {
        this.cfgRepository = arg;
        this.locator = locator;
        findMethodsToProxy();
    }

    private void findMethodsToProxy() {
        for (ConfigurationItemElement item : cfgRepository.getConfigurationItems()) {
            MethodConfiguration methodCfg = new MethodConfiguration(cfgRepository, item, locator);
            all.add(methodCfg);
            atomicReload.add(methodCfg);
        }
    }

    public List<MethodConfiguration> getAtomicReload() {
        return new ArrayList<>(atomicReload);
    }

    public List<MethodConfiguration> getAll() {
        return new ArrayList<>(all);
    }
}

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
package org.blocks4j.reconf.client.elements;

import java.io.File;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;


public class DatabaseConfigurationElement {

    private int maxLogFileSize = -1;
    private boolean compressed;
    private File backupLocation;

    @XmlAttribute(name="max-log-file-size-in-mb")
    public int getMaxLogFileSize() {
        return maxLogFileSize;
    }
    public void setMaxLogFileSize(Integer maxLogFileSize) {
        this.maxLogFileSize = maxLogFileSize;
    }

    @XmlAttribute(name="compressed")
    public boolean isCompressed() {
        return compressed;
    }
    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @XmlValue
    public File getBackupLocation() {
        return backupLocation;
    }
    public void setBackupLocation(File backupLocation) {
        this.backupLocation = backupLocation;
    }
}
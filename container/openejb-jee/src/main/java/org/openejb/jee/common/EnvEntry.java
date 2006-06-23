/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.common;

/**
 * @version $Revision$ $Date$
 */
public class EnvEntry extends JndiEnvironmentRef {
    private String envEntryName;
    private String envEntryType;
    private String envEntryValue;


    public EnvEntry() {
    }

    public EnvEntry(String envEntryName, String envEntryType, String envEntryValue) {
        this.envEntryName = envEntryName;
        this.envEntryType = envEntryType;
        this.envEntryValue = envEntryValue;
    }

    public String getEnvEntryName() {
        return envEntryName;
    }

    public void setEnvEntryName(String envEntryName) {
        this.envEntryName = envEntryName;
    }

    public String getEnvEntryType() {
        return envEntryType;
    }

    public void setEnvEntryType(String envEntryType) {
        this.envEntryType = envEntryType;
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setEnvEntryValue(String envEntryValue) {
        this.envEntryValue = envEntryValue;
    }
}

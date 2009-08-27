/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages properties so any property modifications are handled here.
 * 
 * It lets us track the properties used and possibly remove some. They are all
 * scattered in many places and it's so hard to keep track of them.
 * 
 * The class holds all OpenEJB properties and optionally can amend the
 * environment.
 * 
 * The aim of this class is to establish one place to keep the properties and
 * eventually remove the need to set System properties to communicate between
 * parts and possibly yet lay out a foundation for setting them up in JNDI or
 * some other means
 * 
 * TODO: Should this class be concerned with concurrency issues?
 * 
 * @org.apache.xbean.XBean element="propertiesService"
 * 
 * @version $Rev$ $Date$
 */
public class PropertiesService {
    private Properties props = new Properties();

    /**
     * Should properties be passed on to the environment?
     */
    private boolean passOn = true;

    /**
     * Should the service query environment properties upon initialization?
     */
    private boolean queryEnvOnInit = true;

    public PropertiesService() {
        if (queryEnvOnInit) {
            props.putAll(System.getProperties());
        }
    }

    /**
     * Set value to a property. Optionally set System property via
     * {@link System#setProperty(String, String)}
     * 
     * @param name
     *            property name
     * @param value
     *            property value
     * @return previous property value or null if the value hasn't been assigned
     *         yet
     */
    public String setProperty(String name, String value) {
        if (passOn) {
            System.setProperty(name, value);
        }
        return (String) props.setProperty(name, value);
    }

    public String getProperty(String name) {
        return (String) props.get(name);
    }

    /**
     * ISSUE: It might be of help to differentiate between unavailable property
     * and boolean property set to false
     * 
     * @param name
     *            property name
     * @return true if property keyed by name is set; false otherwise
     */
    public boolean isSet(String name) {
        return props.containsKey(name);
    }

    public void putAll(Properties props) {
        props.putAll(props);
    }
    
    public Properties getProperties() {
        return props;
    }

    public boolean isPassOn() {
        return passOn;
    }

    public void setPassOn(boolean passOn) {
        this.passOn = passOn;
    }

    public boolean isQueryEnvOnInit() {
        return queryEnvOnInit;
    }

    public void setQueryEnvOnInit(boolean queryEnvOnInit) {
        this.queryEnvOnInit = queryEnvOnInit;
    }
}

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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyConfiguration;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class PolicyConfigurationFactoryImpl extends PolicyConfigurationFactory {

    private final Log log = LogFactory.getLog(PolicyConfigurationFactoryImpl.class);
    private static PolicyConfigurationFactoryImpl singleton;
    private Map configurations = new HashMap();

    public static void install() {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", PolicyConfigurationFactoryImpl.class.getName()) ;
    }

    public PolicyConfigurationFactoryImpl() {
        synchronized (PolicyConfigurationFactoryImpl.class) {
            if (singleton != null) {
                log.error("Singleton already assigned.  There may be more than one PolicyConfigurationFactoryImpl being used.");
                throw new IllegalStateException("Singleton already assigned");
            }
            singleton = this;
        }
    }

    public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
        PolicyConfigurationImpl configuration = (PolicyConfigurationImpl) configurations.get(contextID);

        if (configuration == null) {
            configuration = new PolicyConfigurationImpl(contextID);
            configurations.put(contextID, configuration);
        } else {
            configuration.open(remove);
        }

        log.trace("Get " + (remove ? "CLEANED" : "") + " policy configuration " + contextID);
        return configuration;
    }

    public boolean inService(String contextID) throws PolicyContextException {
        PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);

        log.trace("Policy configuration " + contextID + " put into service");
        return configuration.inService();
    }

    static PolicyConfigurationFactoryImpl getSingleton() {
        return singleton;
    }

}

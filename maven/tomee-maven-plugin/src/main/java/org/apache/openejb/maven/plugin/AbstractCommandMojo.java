/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.openejb.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Map;
import java.util.Properties;

/**
 * The type AbstractCommandMojo.
 */
public abstract class AbstractCommandMojo extends AbstractAddressMojo {
    /**
     * Where is deployed ejbd endpoint relatively to root (/tomee/ejb typically).
     */
    @Parameter(property = "tomee-plugin.ejbd-endpoint", defaultValue = "/tomee/ejb")
    protected String ejbdEndpoint;

    /**
     * Flag to force https usage.
     */
    @Parameter(property = "tomee-plugin.command-force-https", defaultValue = "false")
    protected boolean forceHttps;

    /**
     * Properties used to do remote lookup (typically for the Deployer).
     */
    @Parameter
    protected Map<String, String> lookupVariables;

    /**
     * Lookup object.
     *
     * @param name the name
     * @return the object
     */
    protected Object lookup(final String name) {
        if (tomeeHttpPort == null && tomeeHttpsPort == null) {
            tomeeHttpPort = "8080";
        }
        if (tomeeHost == null) {
            tomeeHost = "localhost";
        }
        if (ejbdEndpoint == null) {
            ejbdEndpoint = "/tomee/ejb";
        }

        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put(Context.PROVIDER_URL, providerUrl());
        if (user != null) {
            props.put(Context.SECURITY_PRINCIPAL, user);
        }
        if (password != null) {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }
        if (realm != null) {
            props.put("openejb.authentication.realmName", realm);
        }
        if (lookupVariables != null) {
            props.putAll(lookupVariables);
        }

        try {
            return new InitialContext(props).lookup(name);
        } catch (final Exception e) {
            throw new TomEEException("Not able to execute " + getClass().getSimpleName() +
                    ", maybe add -Dopenejb.system.apps=true -Dtomee.remote.support=true to tomee", e);
        }
    }

    private String providerUrl() {
        if (forceHttps || (tomeeHttpPort == null && tomeeHttpsPort != null)) {
            return "https://" + tomeeHost + ":" + tomeeHttpsPort + ejbdEndpoint;
        }
        return "http://" + tomeeHost + ":" + tomeeHttpPort + ejbdEndpoint;
    }
}

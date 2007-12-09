/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev$ $Date$
 */
public class OpenEJBDeploymentFactory implements DeploymentFactory {
    public static final String URI_SCHEME = "openejb";
    private static org.apache.openejb.config.DeploymentFactory factory = new DeploymentImpl.DeploymentFactoryImpl();
//    static {
//        DeploymentFactoryManager manager = DeploymentFactoryManager.getInstance();
//        manager.registerDeploymentFactory(new OpenEJBDeploymentFactory());
//
//        try {
//            ResourceFinder resourceFinder = new ResourceFinder("META-INF", OpenEJBDeploymentFactory.class.getClassLoader());
//            Class impl = resourceFinder.findImplementation(org.apache.openejb.config.DeploymentFactory.class);
//            factory = (org.apache.openejb.config.DeploymentFactory) impl.newInstance();
//        } catch (Exception ignored) {
//            // todo maybe log this
//        }
//    }

    public static org.apache.openejb.config.DeploymentFactory getFactory() {
        return factory;
    }

    public static void setFactory(org.apache.openejb.config.DeploymentFactory factory) {
        OpenEJBDeploymentFactory.factory = factory;
    }

    public String getDisplayName() {
        return "OpenEJB";
    }

    public String getProductVersion() {
        return "3.0";
    }

    public boolean handlesURI(String uri) {
        try {
            URI fullUri = new URI(uri);
            return OpenEJBDeploymentFactory.URI_SCHEME.equals(fullUri.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI: " + uri);
        }

        return new OpenEJBDeploymentManager();
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
        URI protocolUri = getProtocolUri(uri);
        if (protocolUri == null) {
            throw new DeploymentManagerCreationException("Invalid URI: " + uri);
        }

        try {
            Deployment deployment = factory.createDeployment(protocolUri, username, password);
            return new OpenEJBDeploymentManager(deployment);
        } catch (RuntimeException e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            DeploymentManagerCreationException creationException = new DeploymentManagerCreationException("Unexpected exception while creating deployment manager");
            creationException.initCause(e);
            throw creationException;
        } catch (AssertionError e) {
            // some DeploymentManagerFactories suppress unchecked exceptions - log and rethrow
            DeploymentManagerCreationException creationException = new DeploymentManagerCreationException("Assertion error while creating deployment manager");
            creationException.initCause(e);
            throw creationException;
        }
    }

    private URI getProtocolUri(String uri) {
        try {
            URI fullUri = new URI(uri);
            if (!OpenEJBDeploymentFactory.URI_SCHEME.equals(fullUri.getScheme())) {
                return null;
            }

            URI protocolUri = new URI(fullUri.getSchemeSpecificPart());
            return protocolUri;
        } catch (URISyntaxException e) {
            return null;
        }
    }

//    public static void main(String[] args) {
//        System.out.println("Parsed: "+new DeploymentFactoryImpl().parseURI("deployer:geronimo:inVM"));
//    }
}

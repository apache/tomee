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
package org.apache.openejb.server;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Service;
import org.apache.openejb.util.PropertiesService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 * @org.apache.xbean.XBean element="server"
 * description="OpenEJB Server"
 */
public class Server implements Service {
    // FIXME: Remove it completely once we ensure PropertiesService (below) works well

    Properties props;
    private PropertiesService propertiesService;
    private static Server server;
    private static ServiceManager manager;

    public static Server getInstance() {

        if (server == null) {
            server = new Server();
        }

        return server;
    }

    // TODO: Remove it once init() suits our (initialisation) needs 
    @Override
    public void init(Properties props) throws Exception {
        this.props = props;

        SystemInstance system = SystemInstance.get();
        File home = system.getHome().getDirectory();
        system.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, system.getProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*/" + home.getName() + "/lib/.*"));
        system.setProperty(DeploymentFilterable.CLASSPATH_REQUIRE_DESCRIPTOR, system.getProperty(DeploymentFilterable.CLASSPATH_REQUIRE_DESCRIPTOR, "true"));
        system.setProperty(DeploymentFilterable.CLASSPATH_FILTER_SYSTEMAPPS, system.getProperty(DeploymentFilterable.CLASSPATH_FILTER_SYSTEMAPPS, "false"));

        OpenEJB.init(props, new ServerFederation());

        if (SystemInstance.get().getProperty("openejb.nobanner") == null) {
            System.out.println("[init] OpenEJB Remote Server");
        }

        if (manager == null) {
            manager = ServiceManager.getManager();
        }

        manager.init();
    }

    /**
     * Copy of {@link #init(Properties)} to XBean-ize it
     *
     * @throws Exception
     */
    public void init() throws Exception {

        OpenEJB.init(propertiesService.getProperties(), new ServerFederation());

        if (!propertiesService.isSet("openejb.nobanner")) {
            System.out.println("[init] OpenEJB Remote Server");
        }

        manager.init();
    }

    public void start() throws Exception {
        manager.start();
    }

    public void stop() throws Exception {
        OpenEJB.destroy();
        manager.stop();
    }

    public void addService(URI uri) {
    }

    public static class ServerServiceFactory {

        public ServerService createService(URI location) throws IOException {
            return null;
        }
    }

    public void setServiceManager(ServiceManager serviceManager) {
        manager = serviceManager;
    }

    public void setPropertiesService(PropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }
}

/*
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
package org.apache.tomee.catalina.deploy;

import static javax.ejb.TransactionManagementType.BEAN;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.catalina.util.ContextName;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.util.modeler.Registry;

@Stateless(name = "openejb/WebappDeployer")
@Remote(Deployer.class)
@TransactionManagement(BEAN)
public class WebappDeployer implements Deployer {

	private static final String WEBAPPS = "webapps";
	private static final String OPENEJB_HOME = "openejb.home";
	private final Assembler assembler;
	private MBeanServer mBeanServer;

	public WebappDeployer() {
		assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
		mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
	}

	public String getUniqueFile() {
		throw new RuntimeException("This method is not used");
	}

	public Collection<AppInfo> getDeployedApps() {
		return assembler.getDeployedApplications();
	}

	public AppInfo deploy(String location) throws OpenEJBException {
		return deploy(location, null);
	}

	public AppInfo deploy(Properties properties) throws OpenEJBException {
		return deploy(null, properties);
	}

	public AppInfo deploy(String location, Properties properties) throws OpenEJBException {
		try {
			if (location == null && properties == null) {
			    throw new NullPointerException("location and properties are null");
			}
			if (location == null) {
			    location = properties.getProperty(FILENAME);
			}
			if (properties == null) {
			    properties = new Properties();
			}
			
			// copy to the webapps folder
			File source = new File(location);
			File destination = new File(System.getProperty(OPENEJB_HOME) + File.separator + WEBAPPS + File.separator + source.getName());
			FileUtils.copyFile(destination, source);
			
			// call check method
			String destinationWithoutExtension = destination.getAbsolutePath();
			String destinationFilenameWithoutExtension = destination.getName();
			
			if (destination.getName().contains(".")) {
				destinationWithoutExtension = destinationWithoutExtension.substring(0, destinationWithoutExtension.lastIndexOf('.'));
				destinationFilenameWithoutExtension = destinationFilenameWithoutExtension.substring(0, destinationFilenameWithoutExtension.lastIndexOf('.'));
			}
			
			ContextName cn = new ContextName(destinationFilenameWithoutExtension);
			
			String name = "Catalina:type=Deployer,host=localhost";
			ObjectName oname = new ObjectName(name);
			
			String[] params = { cn.getName() };
	        String[] signature = { "java.lang.String" };
	        mBeanServer.invoke(oname, "check", params, signature);
			
	        Collection<AppInfo> deployedApps = getDeployedApps();

	        // find the correct appinfo
			Iterator<AppInfo> iterator = deployedApps.iterator();
			while (iterator.hasNext()) {
				AppInfo appInfo = (AppInfo) iterator.next();
				if (appInfo.path.equals(destinationWithoutExtension)) {
					return appInfo;
				}
			}
			
			return null;
		} catch (Exception e) {
			throw new OpenEJBException(e);
		}
    }

	public void undeploy(String moduleId) throws UndeployException,	NoSuchApplicationException {
		assembler.destroyApplication(moduleId);
	}

}

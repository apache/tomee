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
package org.apache.tomee.catalina.deployer;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.ContextName;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.DeploymentExceptionManager;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.loader.TomcatHelper;

import static javax.ejb.TransactionManagementType.BEAN;

@Lock(LockType.READ)
@Singleton(name = "openejb/WebappDeployer")
@Remote(Deployer.class)
@TransactionManagement(BEAN)
public class WebappDeployer implements Deployer {

	private static final String WEBAPPS = "webapps";
	private static final String OPENEJB_HOME = "openejb.home";
	private final TomcatWebAppBuilder webappBuilder;
	private final Assembler assembler;
	private final MBeanServer mBeanServer;

	public WebappDeployer() {
		assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
		webappBuilder = (TomcatWebAppBuilder) SystemInstance.get().getComponent(WebAppBuilder.class);
		mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
	}

	@Override
    public String getUniqueFile() {
		throw new TomEERuntimeException("This method is not used");
	}

	@Override
    public Collection<AppInfo> getDeployedApps() {
		return assembler.getDeployedApplications();
	}

	@Override
    public AppInfo deploy(final String location) throws OpenEJBException {
		return deploy(location, null);
	}

	@Override
    public AppInfo deploy(final Properties properties) throws OpenEJBException {
		return deploy(null, properties);
	}

	@Override
    public AppInfo deploy(String location, Properties properties) throws OpenEJBException {
		try {
			if (location == null && properties == null) {
				throw new NullPointerException("location and properties are null");
			}
			if (location == null) {
				location = properties.getProperty(FILENAME);
			}

			final File source = new File(location);
			final File destination = new File(System.getProperty(OPENEJB_HOME) + File.separator + WEBAPPS + File.separator + source.getName());
            IO.copy(source, destination);

            String destinationWithoutExtension = destination.getAbsolutePath();
			String destinationFilenameWithoutExtension = destination.getName();

			if (destination.getName().contains(".")) {
				destinationWithoutExtension = destinationWithoutExtension.substring(0, destinationWithoutExtension.lastIndexOf('.'));
				destinationFilenameWithoutExtension = destinationFilenameWithoutExtension.substring(0, destinationFilenameWithoutExtension.lastIndexOf('.'));
			}

			if (destination.getName().toLowerCase().endsWith(".war")) {
				checkWebapp(destinationFilenameWithoutExtension);
			} else {
				check();
			}

			final AppInfo info = findAppInfo(destination.getAbsolutePath(), destinationWithoutExtension);
            if (info == null) {
                throw new NullPointerException("appinfo not found");
            }

            final DeploymentExceptionManager dem = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
            if (dem.hasDeploymentFailed()) {
                final Exception tre = dem.getLastException();
                // we don't need this exception anymore
                dem.clearLastException(info);
                throw tre;
            }

            return info;
		} catch (Exception e) {
			throw new OpenEJBException(e);
		}
	}

	private AppInfo findAppInfo(final String... paths) {
		final Collection<AppInfo> deployedApps = getDeployedApps();
        for (final AppInfo appInfo : deployedApps) {
            for (final String path : paths) {
                if (appInfo.path.equals(path)) {
                    return appInfo;
                }
            }
        }
		return null;
	}

	private void check() {
		final StandardServer server = TomcatHelper.getServer();
		for (final Service service : server.findServices()) {
			if (service.getContainer() instanceof Engine) {
				final Engine engine = (Engine) service.getContainer();
				for (final Container engineChild : engine.findChildren()) {
					if (engineChild instanceof StandardHost) {
						final StandardHost host = (StandardHost) engineChild;
						webappBuilder.checkHost(host);
					}
				}
            }
		}
	}

	private void checkWebapp(final String webappName) {
		try {
			final ContextName cn = new ContextName(webappName, true);

			final String name = "Catalina:type=Deployer,host=localhost";
			final ObjectName oname = new ObjectName(name);

			final String[] params = { cn.getName() };
			final String[] signature = { "java.lang.String" };
			mBeanServer.invoke(oname, "check", params, signature);
		} catch (Exception e) {
            //Ignore
		}
	}

	@Override
    public void undeploy(final String moduleId) throws UndeployException, NoSuchApplicationException {
		try {
			final AppInfo appInfo = findAppInfo(moduleId);
			if (appInfo !=  null) {
				webappBuilder.undeployWebApps(appInfo);
			}
			
			assembler.destroyApplication(moduleId);

			final File moduleFile = new File(moduleId);
			
			if (moduleFile.getName().contains(".")) {
				// delete matching directory
				final File dir = new File(moduleFile.getAbsolutePath().substring(0, moduleFile.getAbsolutePath().lastIndexOf('.')));
				if (dir.exists() && dir.isDirectory()) {
					delete(dir);
				}
			} else {
				delete(new File(moduleFile + ".war"));
				delete(new File(moduleFile + ".ear"));
				delete(new File(moduleFile + ".rar"));
			}
			
			delete(moduleFile);

		} catch (Exception e) {
			throw new UndeployException(e);
		}
	}

    @Override
    public void reload(final String s) {
        throw new UnsupportedOperationException();
    }

    private void delete(final File f) {
		if (f == null || (!f.exists())) {
			return;
		}
		
		if (f.isFile()) {
			if(!f.delete()){
                f.deleteOnExit();
            }
			return;
		}
		
		if (f.isDirectory()) {
			final File[] listFiles = f.listFiles();
            if (listFiles != null) {
                for (final File file : listFiles) {
                    if (file.getName().equals(".") || file.getName().equals("..")) continue;
                    delete(file);
                }
            }

            if(!f.delete()){
                f.deleteOnExit();
            }
		}
	}

}

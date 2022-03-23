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

package org.apache.openejb.assembler;

import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.config.sys.AdditionalDeployments;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.provisining.ProvisioningResolver;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.Lock;
import jakarta.ejb.Remote;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionManagement;
import jakarta.enterprise.inject.Alternative;
import jakarta.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static jakarta.ejb.LockType.READ;
import static jakarta.ejb.TransactionManagementType.BEAN;
import static org.apache.openejb.config.ConfigurationFactory.ADDITIONAL_DEPLOYMENTS;
import static org.apache.openejb.loader.ProvisioningUtil.realLocation;

@SuppressWarnings("EjbProhibitedPackageUsageInspection")
@Singleton(name = "openejb/Deployer")
@Lock(READ)
@Remote(Deployer.class)
@TransactionManagement(BEAN)
@Alternative
public class DeployerEjb implements Deployer {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DeployerEjb.class);

    public static final String OPENEJB_DEPLOYER_FORCED_APP_ID_PROP = "openejb.deployer.forced.appId";

    public static final String OPENEJB_DEPLOYER_HOST = "openejb.deployer.host";

    public static final String OPENEJB_USE_BINARIES = "openejb.deployer.binaries.use";
    public static final String OPENEJB_PATH_BINARIES = "openejb.deployer.binaries.path";
    public static final String OPENEJB_VALUE_BINARIES = "openejb.deployer.binaries.value";

    public static final String OPENEJB_APP_AUTODEPLOY = "openejb.app.autodeploy";
    public static final ThreadLocal<Boolean> AUTO_DEPLOY = new ThreadLocal<>();

    private static final File uniqueFile;
    private static final boolean oldWarDeployer = "old".equalsIgnoreCase(SystemInstance.get().getOptions().get("openejb.deployer.war", "new"));
    private static final String OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS = "openejb.deployer.save-deployments";
    private static final boolean SAVE_DEPLOYMENTS = SystemInstance.get().getOptions().get(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, false);

    static {
        final String uniqueName = "OpenEJB-" + new BigInteger(128, new SecureRandom()).toString(Character.MAX_RADIX);
        final String tempDir = JavaSecurityManagers.getSystemProperty("java.io.tmpdir");
        File unique;
        try {
            unique = new File(tempDir, uniqueName).getCanonicalFile();
            if (!unique.createNewFile()) {
                throw new IOException("Failed to create file in temp: " + unique);
            }
        } catch (final IOException e) {
            // same trying in work directory
            unique = new File(SystemInstance.get().getBase().getDirectory(), "work");
            if (unique.exists()) {
                try {
                    unique = new File(unique, uniqueName).getCanonicalFile();
                    if (!unique.createNewFile()) {
                        throw new IOException("Failed to create file in work: " + unique);
                    }
                } catch (final IOException e1) {
                    throw new OpenEJBRuntimeException(e);
                }
            } else {
                throw new OpenEJBRuntimeException("cannot create unique file, please set java.io.tmpdir to a writable folder or create work folder", e);
            }
        }
        uniqueFile = unique;
        uniqueFile.deleteOnExit();
    }


    private final DeploymentLoader deploymentLoader;
    private final Assembler assembler;

    public DeployerEjb() {
        deploymentLoader = new DeploymentLoader();
        assembler = (Assembler) SystemInstance.get().getComponent(org.apache.openejb.spi.Assembler.class);
    }

    @Override
    public String getUniqueFile() {
        return uniqueFile.getAbsolutePath();
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
    public AppInfo deploy(final String inLocation, Properties properties) throws OpenEJBException {
        String rawLocation = inLocation;
        if (rawLocation == null && properties == null) {
            throw new NullPointerException("location and properties are null");
        }
        if (rawLocation == null) {
            rawLocation = properties.getProperty(FILENAME);
        }
        if (properties == null) {
            properties = new Properties();
        }

        AppModule appModule = null;

        final File file;
        if ("true".equalsIgnoreCase(properties.getProperty(OPENEJB_USE_BINARIES, "false"))) {
            file = copyBinaries(properties);
        } else {
            file = new File(realLocation(rawLocation).iterator().next());
        }

        final boolean autoDeploy = Boolean.parseBoolean(properties.getProperty(OPENEJB_APP_AUTODEPLOY, "false"));
        final String host = properties.getProperty(OPENEJB_DEPLOYER_HOST, null);

        if (WebAppDeployer.Helper.isWebApp(file) && !oldWarDeployer) {
            AUTO_DEPLOY.set(autoDeploy);
            try {
                final AppInfo appInfo = SystemInstance.get().getComponent(WebAppDeployer.class)
                                                      .deploy(host, contextRoot(properties, file), file);
                if (appInfo != null) {
                    saveIfNeeded(properties, file, appInfo);
                    return appInfo;
                }
                throw new OpenEJBException("can't deploy " + file.getAbsolutePath());
            } finally {
                AUTO_DEPLOY.remove();
            }
        }

        AppInfo appInfo = null;

        try {
            appModule = deploymentLoader.load(file, null);

            // Add any alternate deployment descriptors to the modules
            final Map<String, DeploymentModule> modules = new TreeMap<>();
            for (final DeploymentModule module : appModule.getEjbModules()) {
                modules.put(module.getModuleId(), module);
            }
            for (final DeploymentModule module : appModule.getClientModules()) {
                modules.put(module.getModuleId(), module);
            }
            for (final WebModule module : appModule.getWebModules()) {
                final String contextRoot = contextRoot(properties, module.getJarLocation());
                if (contextRoot != null) {
                    module.setContextRoot(contextRoot);
                    module.setHost(host);
                }
                modules.put(module.getModuleId(), module);
            }
            for (final DeploymentModule module : appModule.getConnectorModules()) {
                modules.put(module.getModuleId(), module);
            }

            for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                if (name.startsWith(ALT_DD + "/")) {
                    name = name.substring(ALT_DD.length() + 1);

                    final DeploymentModule module;
                    final int slash = name.indexOf('/');
                    if (slash > 0) {
                        final String moduleId = name.substring(0, slash);
                        name = name.substring(slash + 1);
                        module = modules.get(moduleId);
                    } else {
                        module = appModule;
                    }

                    if (module != null) {
                        final String value = (String) entry.getValue();
                        final File dd = new File(value);
                        if (dd.canRead()) {
                            module.getAltDDs().put(name, dd.toURI().toURL());
                        } else {
                            module.getAltDDs().put(name, value);
                        }
                    }
                }
            }

            final OpenEjbConfiguration configuration = new OpenEjbConfiguration();
            configuration.containerSystem = new ContainerSystemInfo();
            configuration.facilities = new FacilitiesInfo();

            final ConfigurationFactory configurationFactory = new ConfigurationFactory(false, configuration);
            appInfo = configurationFactory.configureApplication(appModule);
            appInfo.autoDeploy = autoDeploy;

            if (properties != null && properties.containsKey(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP)) {
                appInfo.appId = properties.getProperty(OPENEJB_DEPLOYER_FORCED_APP_ID_PROP);
            }

            if (!appInfo.webApps.isEmpty()) {
                appInfo.properties.setProperty("tomcat.unpackWar", "false");
            }

            // create any resources and containers defined in the application itself
            if (!appInfo.webApps.isEmpty()) {
                appInfo.properties.setProperty("tomcat.unpackWar", "false");
            }

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            final ClassLoader appClassLoader = assembler.createAppClassLoader(appInfo);
            try {
                Thread.currentThread().setContextClassLoader(appClassLoader);

                for (final ResourceInfo resource : configuration.facilities.resources) {
                    assembler.createResource(resource);
                }

                for (final ContainerInfo container : configuration.containerSystem.containers) {
                    assembler.createContainer(container);
                }

            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }

            assembler.createApplication(appInfo, appClassLoader);

            saveIfNeeded(properties, file, appInfo);

            return appInfo;

        } catch (final Throwable e) {
            // destroy the class loader for the failed application
            if (appModule != null) {
                ClassLoaderUtil.destroyClassLoader(appModule.getJarLocation());
            }

            if (null != appInfo) {
                ClassLoaderUtil.destroyClassLoader(appInfo.appId, appInfo.path);
            }

            LOGGER.error("Can't deploy " + inLocation, e);

            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            }

            final Throwable ex;
            final DeploymentExceptionManager dem = SystemInstance.get().getComponent(DeploymentExceptionManager.class);
            if (dem != null) {
                if (dem.hasDeploymentFailed()) {
                    ex = dem.getLastException();
                } else {
                    ex = e;
                }
                if (appInfo != null) {
                    dem.clearLastException(appInfo);
                }
            } else {
                ex = e;
            }

            if (ex instanceof OpenEJBException) {
                if (ex.getCause() instanceof ValidationException) {
                    throw (ValidationException) ex.getCause();
                }
                throw (OpenEJBException) ex;
            }
            throw new OpenEJBException(ex);
        }
    }

    private void saveIfNeeded(final Properties properties, final File file, final AppInfo appInfo) {
        if ((SAVE_DEPLOYMENTS && null == properties.getProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS))
            || "true".equalsIgnoreCase(properties.getProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, "false"))) {
            appInfo.properties.setProperty("save-deployment","true");
            saveDeployment(file, true);
        }
    }

    private synchronized File copyBinaries(final Properties props) throws OpenEJBException {
        final File dump = ProvisioningResolver.cacheFile(props.getProperty(OPENEJB_PATH_BINARIES, "dump.war"));
        if (dump.exists()) {
            Files.delete(dump);
            final String name = dump.getName();
            if (name.endsWith("ar") && name.length() > 4) {
                final File exploded = new File(dump.getParentFile(), name.substring(0, name.length() - 4));
                if (exploded.exists()) {
                    Files.delete(exploded);
                }
            }
        }
        try {
            IO.copy(byte[].class.cast(props.get(OPENEJB_VALUE_BINARIES)), dump);
        } catch (final IOException e) {
            throw new OpenEJBException(e);
        }
        return dump;
    }

    private synchronized void saveDeployment(final File file, final boolean add) {
        final Deployments deps = new Deployments();
        if (file.isDirectory()) {
            deps.setDir(file.getAbsolutePath());
        } else {
            deps.setFile(file.getAbsolutePath());
        }

        File config;
        try {
            config = SystemInstance.get().getBase().getFile(ADDITIONAL_DEPLOYMENTS, false);
        } catch (final IOException e) {
            config = null;
        }
        if (config == null || !config.getParentFile().exists()) {
            LOGGER.info("Cannot save the added app because the conf folder does not exist, it will not be present on a restart");
            return;
        }

        // dump it
        OutputStream os = null;
        try {
            final AdditionalDeployments additionalDeployments;
            if (config.exists() && config.length() > 0) {
                final InputStream fis = IO.read(config);
                try {
                    additionalDeployments = JaxbOpenejb.unmarshal(AdditionalDeployments.class, fis);
                } finally {
                    IO.close(fis);
                }
            } else {
                additionalDeployments = new AdditionalDeployments();
            }

            if (add) {
                if (!additionalDeployments.getDeployments().contains(deps)) {
                    additionalDeployments.getDeployments().add(deps);
                }
            } else {
                final Iterator<Deployments> it = additionalDeployments.getDeployments().iterator();
                while (it.hasNext()) {
                    final Deployments current = it.next();
                    if (deps.getDir() != null && deps.getDir().equals(current.getDir())) {
                        it.remove();
                        break;
                    } else if (deps.getFile() != null && deps.getFile().equals(current.getFile())) {
                        it.remove();
                        break;
                    } else { // exploded dirs
                        final String jar = deps.getFile();
                        if (jar != null && jar.length() > 3) {
                            final String substring = jar.substring(0, jar.length() - 4);
                            if (substring.equals(current.getDir()) || substring.equals(current.getFile())) {
                                it.remove();
                                break;
                            }
                        } else {
                            final String jarC = current.getFile();
                            if (jarC != null && jarC.length() > 3) {
                                final String substring = jarC.substring(0, jarC.length() - 4);
                                if (substring.equals(deps.getDir()) || substring.equals(deps.getFile())) {
                                    it.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            os = IO.write(config);
            JaxbOpenejb.marshal(AdditionalDeployments.class, additionalDeployments, os);
        } catch (final Exception e) {
            LOGGER.error("cannot save the added app, will not be present next time you'll start", e);
        } finally {
            IO.close(os);
        }
    }

    @Override
    public void undeploy(final String moduleId) throws UndeployException, NoSuchApplicationException {
        AppInfo appInfo = assembler.getAppInfo(moduleId);
        if (appInfo == null) {
            appInfo = assembler.getAppInfo(realLocation(moduleId).iterator().next());
            if (appInfo == null) {
                appInfo = assembler.getAppInfo(new File(moduleId).getAbsolutePath());
                if (appInfo == null) {
                    appInfo = assembler.getAppInfo(new File(realLocation(moduleId).iterator().next()).getAbsolutePath());
                }
            }
        }
        if (appInfo != null) {
            try {
                assembler.destroyApplication(appInfo);
            } finally {
                if (appInfo.properties.containsKey("save-deployment")) {
                    saveDeployment(new File(moduleId), false);
                }
            }
        } else {
            throw new NoSuchApplicationException(moduleId);
        }
    }

    private String contextRoot(final Properties properties, final String jarPath) {
        final File file = new File(jarPath);
        return file.exists()
               ? contextRoot(properties, file)
               : properties.getProperty("webapp." + jarPath + ".context-root");
    }

    private String contextRoot(final Properties properties, final File jarPath) {
        return properties.getProperty("webapp." + jarPath.getName() + ".context-root");
    }

    @Override
    public void reload(final String moduleId) {
        for (final AppInfo info : assembler.getDeployedApplications()) {
            if (info.path.equals(moduleId)) {
                reload(info);
                break;
            }
        }
    }

    private void reload(final AppInfo info) {

        if (info.webAppAlone) {
            final WebAppDeployer component = SystemInstance.get().getComponent(WebAppDeployer.class);

            if (null != component) {
                component.reload(info.path);
                return;
            }
        }

            try {
                assembler.destroyApplication(info);
                assembler.createApplication(info);
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

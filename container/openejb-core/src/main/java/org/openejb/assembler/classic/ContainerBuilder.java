package org.openejb.assembler.classic;

import org.openejb.Container;
import org.openejb.OpenEJBException;
import org.openejb.RpcContainer;
import org.openejb.loader.SystemInstance;
import org.openejb.core.DeploymentInfo;
import org.openejb.util.Logger;
import org.openejb.util.SafeToolkit;
import org.apache.xbean.recipe.ObjectRecipe;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class ContainerBuilder {

    private static final Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    private final Properties props;
    private final EjbJarInfo[] ejbJars;
    private final ContainerInfo[] containerInfos;
    private final String[] decorators;

    public ContainerBuilder(ContainerSystemInfo containerSystemInfo, Properties props) {
        this.props = props;
        this.ejbJars = containerSystemInfo.ejbJars;
        this.containerInfos = containerSystemInfo.containers;
        String decorators = props.getProperty("openejb.container.decorators");
        this.decorators = (decorators == null) ? new String[]{} : decorators.split(":");
    }

    public Object build() throws OpenEJBException {
        HashMap deployments = new HashMap();
        URL[] jars = new URL[this.ejbJars.length];
        for (int i = 0; i < this.ejbJars.length; i++) {
            try {
                jars[i] = new File(this.ejbJars[i].jarPath).toURL();
            } catch (MalformedURLException e) {
                throw new OpenEJBException(AssemblerTool.messages.format("cl0001", ejbJars[i].jarPath, e.getMessage()));
            }
        }

        ClassLoader classLoader = new URLClassLoader(jars, org.openejb.OpenEJB.class.getClassLoader());

        for (int i = 0; i < this.ejbJars.length; i++) {
            EjbJarInfo ejbJar = this.ejbJars[i];

            EnterpriseBeanInfo[] ejbs = ejbJar.enterpriseBeans;
            for (int j = 0; j < ejbs.length; j++) {
                EnterpriseBeanInfo ejbInfo = ejbs[j];
                EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(classLoader, ejbInfo);
                DeploymentInfo deployment = (DeploymentInfo) deploymentBuilder.build();
                deployments.put(ejbInfo.ejbDeploymentId, deployment);
            }
        }

        List containers = new ArrayList();
        for (int i = 0; i < containerInfos.length; i++) {
            ContainerInfo containerInfo = containerInfos[i];

            HashMap deploymentsList = new HashMap();
            for (int z = 0; z < containerInfo.ejbeans.length; z++) {
                String ejbDeploymentId = containerInfo.ejbeans[z].ejbDeploymentId;
                DeploymentInfo deployment = (DeploymentInfo) deployments.get(ejbDeploymentId);
                deploymentsList.put(ejbDeploymentId, deployment);
            }

            containers.add(buildContainer(containerInfo, deploymentsList));
        }
        return containers;
    }

//    private Container buildContainer(ContainerInfo containerInfo, HashMap deploymentsList) throws OpenEJBException {
//        String containerName = containerInfo.containerName;
//        ContainerInfo service = containerInfo;
//
//
//
//            Container container = null;
//            Properties systemProperties = System.getProperties();
//            synchronized (systemProperties) {
//                String userDir = systemProperties.getProperty("user.dir");
//                try {
//                    File base = SystemInstance.get().getBase().getDirectory();
//                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
//
//                    ObjectRecipe containerRecipe = new ObjectRecipe(service.className, service.constructorArgs, null);
//                    containerRecipe.setAllProperties(service.properties);
//                    return (Container) containerRecipe.create();
////                    container.init(containerName, deploymentsList, service.properties);
//                } catch (Exception e) {
//                    throw new OpenEJBException(AssemblerTool.messages.format("as0002", containerName, e.getMessage()));
//                } finally {
//                    systemProperties.setProperty("user.dir", userDir);
//                }
//            }
//    }
    private Container buildContainer(ContainerInfo containerInfo, HashMap deploymentsList) throws OpenEJBException {
        String className = containerInfo.className;
        String codebase = containerInfo.codebase;
        String containerName = containerInfo.containerName;
        ContainerInfo service = containerInfo;


        try {
            Class factory = SafeToolkit.loadClass(className, codebase);
            if (!Container.class.isAssignableFrom(factory)) {
                throw new OpenEJBException(AssemblerTool.messages.format("init.0100", "Container", containerName, factory.getName(), Container.class.getName()));
            }

            Properties clonedProps = (Properties) (props.clone());
            clonedProps.putAll(containerInfo.properties);

            Container container = (Container) factory.newInstance();

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            for (int i = 0; i < decorators.length && container instanceof RpcContainer; i++) {
                try {
                    String decoratorName = decorators[i];
                    Class decorator = contextClassLoader.loadClass(decoratorName);
                    Constructor constructor = decorator.getConstructor(new Class[]{RpcContainer.class});
                    container = (Container) constructor.newInstance(new Object[]{container});
                } catch (NoSuchMethodException e) {
                    String name = decorators[i].replaceAll(".*\\.", "");
                    logger.error("Container wrapper " + decorators[i] + " does not have the required constructor 'public " + name + "(RpcContainer container)'");
                } catch (InvocationTargetException e) {
                    logger.error("Container wrapper " + decorators[i] + " could not be constructed and will be skipped.  Received message: " + e.getCause().getMessage(), e.getCause());
                } catch (ClassNotFoundException e) {
                    logger.error("Container wrapper class " + decorators[i] + " could not be loaded and will be skipped.");
                }
            }

            Properties systemProperties = System.getProperties();
            synchronized (systemProperties) {
                String userDir = systemProperties.getProperty("user.dir");
                try {
                    File base = SystemInstance.get().getBase().getDirectory();
                    systemProperties.setProperty("user.dir", base.getAbsolutePath());
                    container.init(containerName, deploymentsList, clonedProps);
                } finally {
                    systemProperties.setProperty("user.dir", userDir);
                }
            }

            return container;
        } catch (OpenEJBException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0002", containerName, e.getMessage()));
        } catch (InstantiationException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0003", containerName, e.getMessage()));
        } catch (IllegalAccessException e) {
            throw new OpenEJBException(AssemblerTool.messages.format("as0003", containerName, e.getMessage()));
        }
    }
}

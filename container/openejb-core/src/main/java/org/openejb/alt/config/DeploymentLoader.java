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
package org.openejb.alt.config;

import org.openejb.alt.config.sys.Deployments;
import org.openejb.alt.config.ejb.OpenejbJar;
import org.openejb.loader.FileUtils;
import org.openejb.loader.SystemInstance;
import org.openejb.OpenEJBException;
import org.openejb.OpenEJB;
import org.openejb.util.Logger;
import org.openejb.jee.EjbJar;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentLoader {

    public DeploymentLoader(){

    }

    private static void loadFrom(Deployments dep, FileUtils path, List jarList) {

        ////////////////////////////////
        //
        //  Expand the path of a jar
        //
        ////////////////////////////////
        if (dep.getDir() == null && dep.getJar() != null) {
            try {
                File jar = path.getFile(dep.getJar(), false);
                if (!jarList.contains(jar.getAbsolutePath())) {
                    jarList.add(jar.getAbsolutePath());
                }
            } catch (Exception ignored) {
            }
            return;
        }

        File dir = null;
        try {
            dir = path.getFile(dep.getDir(), false);
        } catch (Exception ignored) {
        }

        if (dir == null || !dir.isDirectory()) return;

        ////////////////////////////////
        //
        //  Unpacked "Jar" directory
        //
        ////////////////////////////////
        File ejbJarXml = new File(dir, "META-INF" + File.separator + "ejb-jar.xml");
        if (ejbJarXml.exists()) {
            if (!jarList.contains(dir.getAbsolutePath())) {
                jarList.add(dir.getAbsolutePath());
            }
            return;
        }

        ////////////////////////////////
        //
        //  Directory container Jar files
        //
        ////////////////////////////////
        String[] jarFiles = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jarFiles == null) {
            return;
        }

        for (int x = 0; x < jarFiles.length; x++) {
            String f = jarFiles[x];
            File jar = new File(dir, f);

            if (jarList.contains(jar.getAbsolutePath())) continue;
            jarList.add(jar.getAbsolutePath());
        }
    }
    public static enum Type {
        JAR, DIR, CLASSPATH
    }

    public List<DeployedJar> load(Type type, Object source) throws OpenEJBException {
        Deployments deployments = new Deployments();
        switch(type){
            case JAR: deployments.setJar((String) source); break;
            case DIR: deployments.setDir((String) source); break;
            case CLASSPATH: deployments.setClasspath((ClassLoader) source); break;
        }

        List<Deployments> list = new ArrayList();
        list.add(deployments);
        return loadDeploymentsList(list, null);
    }


    public List<DeployedJar> loadDeploymentsList(List<Deployments> deployments, DynamicDeployer deployer) throws OpenEJBException {
        if (deployer == null){
            deployer = new DynamicDeployer(){
                public OpenejbJar deploy(EjbJarUtils ejbJarUtils, String jarLocation, ClassLoader classLoader) throws OpenEJBException {
                    return ejbJarUtils.getOpenejbJar();
                }
            };
        }
        EjbValidator validator = new EjbValidator();

        List<DeployedJar> deployedJars = new ArrayList();

        // resolve jar locations //////////////////////////////////////  BEGIN  ///////

        FileUtils base = SystemInstance.get().getBase();


        List<String> jarList = new ArrayList(deployments.size());
        try {
            for (Deployments deployment : deployments) {
                if (deployment.getClasspath() != null){
                    loadFromClasspath(base, jarList, deployment.getClasspath());
                } else {
                    loadFrom(deployment, base, jarList);
                }
            }
        } catch (SecurityException se) {

        }

        String[] jarsToLoad = (String[]) jarList.toArray(new String[]{});
        // resolve jar locations //////////////////////////////////////  END  ///////

        /*[1]  Put all EjbJar & OpenejbJar objects in a vector ***************/
        for (int i = 0; i < jarsToLoad.length; i++) {

            String jarLocation = jarsToLoad[i];
            try {
                EjbJarUtils ejbJarUtils = new EjbJarUtils(jarLocation);
                EjbJar ejbJar = ejbJarUtils.getEjbJar();

                ClassLoader classLoader;

                File jarFile = new File(jarLocation);
                if (jarFile.isDirectory()) {
                    try {
                        URL[] urls = new URL[]{jarFile.toURL()};
                        classLoader = new URLClassLoader(urls, OpenEJB.class.getClassLoader());
        //                        classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

                    } catch (MalformedURLException e) {
                        throw new OpenEJBException(ConfigurationFactory.messages.format("cl0001", jarLocation, e.getMessage()));
                    }
                } else {
                    TempCodebase tempCodebase = new TempCodebase(jarLocation);
                    classLoader = tempCodebase.getClassLoader();
                }

                OpenejbJar openejbJar = deployer.deploy(ejbJarUtils, jarLocation, classLoader);

                EjbSet set = validator.validateJar(ejbJarUtils, classLoader);
                if (set.hasErrors() || set.hasFailures()) {
                    Logger logger = Logger.getInstance("OpenEJB.startup.validation", "org.openejb.alt.config.rules");

                    ValidationError[] errors = set.getErrors();
                    for (int j = 0; j < errors.length; j++) {
                        ValidationError e = errors[j];
                        String ejbName = (e.getBean() != null)? e.getBean().getEjbName(): "null";
                        logger.error(e.getPrefix() + " ... " + ejbName + ":\t" + e.getMessage(2));
                    }
                    ValidationFailure[] failures = set.getFailures();
                    for (int j = 0; j < failures.length; j++) {
                        ValidationFailure e = failures[j];
                        logger.error(e.getPrefix() + " ... " + e.getBean().getEjbName() + ":\t" + e.getMessage(2));
                    }

                    throw new OpenEJBException("Jar failed validation.  Use the validation tool for more details");
                }

                /* Add it to the Vector ***************/
                ConfigurationFactory.logger.info("Loaded EJBs from " + jarLocation);
                deployedJars.add(new DeployedJar(jarLocation, ejbJar, openejbJar));
            } catch (OpenEJBException e) {
                ConfigUtils.logger.i18n.warning("conf.0004", jarLocation, e.getMessage());
            }
        }
        return deployedJars;
    }

    private void loadFromClasspath(FileUtils base, List<String> jarList, ClassLoader classLoader) {
        try {
            Enumeration resources = classLoader.getResources("META-INF/ejb-jar.xml");
            while (resources.hasMoreElements()) {
                URL ejbJar1 = (URL) resources.nextElement();

                String path = null;
                Deployments deployment = new Deployments();
                if (ejbJar1.getProtocol().equals("jar")){
                    ejbJar1 = new URL(ejbJar1.getFile().replaceFirst("!.*$", ""));
                    File file = new File(ejbJar1.getFile());
                    path = file.getAbsolutePath();
                    deployment.setJar(path);
                } else if (ejbJar1.getProtocol().equals("file")) {
                    File file = new File(ejbJar1.getFile());
                    File metainf = file.getParentFile();
                    File ejbPackage = metainf.getParentFile();
                    path = ejbPackage.getAbsolutePath();
                    deployment.setDir(path);
                } else {
                    ConfigurationFactory.logger.warning("Not loading ejbs.  Unknown protocol "+ejbJar1.getProtocol());
                    continue;
                }

                ConfigurationFactory.logger.info("Found ejb in classpath: "+path);
                loadFrom(deployment, base, jarList);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            ConfigurationFactory.logger.warning("Unable to search classpath for ejbs: Received Exception: "+e1.getClass().getName()+" "+e1.getMessage(),e1);
        }
    }

}

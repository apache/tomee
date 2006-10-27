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
package org.apache.openejb.alt.config;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Stateless;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.ejb.MethodParams;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.alt.config.ejb.QueryMethod;
import org.apache.openejb.alt.config.ejb.ResourceLink;
import org.apache.openejb.alt.config.sys.Connector;
import org.apache.openejb.alt.config.sys.Container;
import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

/**
 * Deploy EJB beans
 */
public class Deploy {
    private static final String HELP_BASE = "META-INF/org.apache.openejb.cli/";

    private static final String DEPLOY_TOOL_MSG_HEADER = "Apache OpenEJB Deploy Tool";

    protected static final Messages _messages = new Messages("org.apache.openejb.alt.util.resources");

    private static final String DEPLOYMENT_ID_HELP = "\nDeployment ID ----- \n\nA name for the ejb that is unique not only in this jar, but \nin all the jars in the container system.  This name will \nallow OpenEJB to place the bean in a global index and \nreference the bean quickly.  OpenEJB will also use this name \nas the global JNDI name for the Remote Server and the Local \nServer.  Clients of the Remote or Local servers can use this\nname to perform JNDI lookups.\n\nThe other EJB Server's using OpenEJB as the EJB Container \nSystem may also use this name to as part of a global JNDI \nnamespace available to remote application clients.\n\nExample: /my/acme/bugsBunnyBean";
    private static final String CONTAINER_ID_HELP = "\nContainer ID ----- \n\nThe name of the container where this ejb should run. \nContainers are declared and configured in the openejb.conf\nfile.\n";
    private static final String CONNECTOR_ID_HELP = "\nConnector ID ----- \n\nThe name of the connector or JDBC resource this resoure \nreference should be mapped to. Connectors and JDBC resources \nare declared and configured in the openejb.conf file.\n";

    /*=======----------TODO----------=======      Neat options that this Deploy tool
      could support

      Contributions and ideas welcome!!!
     =======----------TODO----------=======*/

    private boolean AUTO_ASSIGN;

    private boolean MOVE_JAR;

    private boolean FORCE_OVERWRITE_JAR;

    private boolean COPY_JAR;

    private boolean AUTO_CONFIG;

    private boolean GENERATE_DEPLOYMENT_ID;

    private boolean GENERATE_STUBS;

    private DataInputStream in;
    private PrintStream out;
    private Openejb config;
    private String configFile;
    private boolean configChanged;
    private boolean autoAssign;
    private Container[] containers;
    private Connector[] resources;
    private ClassLoader classLoader;
    private String jarLocation;

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public Deploy() throws OpenEJBException {
    }

    public void init() throws OpenEJBException {
        init(null);
    }

    public void init(String openejbConfigFile) throws OpenEJBException {
        try {

            Logger.init();

            SystemInstance system = SystemInstance.get();

            if (system.hasProperty("openejb.nobanner")) {
                printVersion();
                System.out.println("");
            }

            in = new DataInputStream(System.in);
            out = System.out;

            configFile = openejbConfigFile;
            if (configFile == null) {
                try {
                    configFile = system.getProperty("openejb.configuration");
                } catch (Exception e) {
                }
            }
            if (configFile == null) {
                configFile = ConfigUtils.searchForConfiguration();
            }
            config = ConfigUtils.readConfig(configFile);

            /* Load container list */
            containers = config.getContainer();

            /* Load resource list */
            resources = config.getConnector();

        } catch (Exception e) {

            e.printStackTrace();
            throw new OpenEJBException(e.getMessage());
        }

    }

    /*------------------------------------------------------*/
    /*    Methods for starting the deployment process       */
    /*------------------------------------------------------*/

    private void deploy(String jarLocation) throws OpenEJBException {

        this.jarLocation = jarLocation;
        List<String> ejbs = searchForAnnotatedEjbs(jarLocation);
        System.out.println("@Stateless-annotated beans count: " + ejbs.size());

        EjbJarUtils ejbJarUtils = new EjbJarUtils(jarLocation);

        EjbValidator validator = new EjbValidator();

        classLoader = null;
        try {
            File jarFile = new File(ejbJarUtils.getJarLocation());
            URL[] classpath = new URL[]{jarFile.toURL()};
            classLoader = new URLClassLoader(classpath, this.getClass().getClassLoader());
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Unable to create a classloader to load classes from '" + jarLocation + "'", e);
        }

        final EjbModule ejbModule = new EjbModule(classLoader, ejbJarUtils.getJarLocation(), ejbJarUtils.getEjbJar(), ejbJarUtils.getOpenejbJar());
        EjbSet set = validator.validateJar(ejbModule);

        if (set.hasErrors() || set.hasFailures()) {
            validator.printResults(set);
            System.out.println();
            System.out.println("Jar not deployable.");
            System.out.println();
            System.out.println("Use the validator with -vvv option for more details.");
            System.out.println("See http://incubator.apache.org/openejb/validation-tool.html for usage.");
            return;
        }

        OpenejbJar openejbJar = new OpenejbJar();

        Bean[] beans = EjbJarUtils.getBeans(ejbJarUtils.getEjbJar());;

        listBeanNames(beans);

        for (int i = 0; i < beans.length; i++) {
            openejbJar.addEjbDeployment(deployBean(beans[i], jarLocation));
        }

        if (MOVE_JAR) {
            jarLocation = moveJar(jarLocation);
        } else if (COPY_JAR) {
            jarLocation = copyJar(jarLocation);
        }

        /* TODO: Automatically updating the users
        config file might not be desireable for
        some people.  We could make this a
        configurable option.
        */
        addDeploymentEntryToConfig(jarLocation);

        saveChanges(jarLocation, openejbJar);

    }

    /**
     * @param path path of jar to deploy
     */
    private List<String> searchForAnnotatedEjbs(String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            return searchEjbsInDirectory(file);
        } else {
            return searchEjbsInJar(file);
        }
    }

    private List<String> searchEjbsInJar(File file) {
    	// TODO: Implement it!
        return new ArrayList<String>();
    }


    private List<String> searchEjbsInDirectory(File dir) {
        List<String> beans = new ArrayList<String>();
        try {
            ClassLoader loader = new URLClassLoader(new URL[] {dir.toURL()});
            List<File> files = getClasses(dir);
            for (File file : files) {
                String className = file.getPath().substring(dir.getPath().length() + 1);
                className = className.replace(File.separatorChar, '.');
                className = className.substring(0, className.lastIndexOf('.'));
                Class<?> clazz = Class.forName(className, true, loader);
                if (clazz.isAnnotationPresent(Stateless.class)) {
                    beans.add(className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return beans;
    }

    /**
     * @param dir directory to look at
     * @return classes as a {@link List} of {@link File}'s
     */
    private List<File> getClasses(File dir) {
        List<File> classes = new ArrayList<File>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                return getClasses(file);
            } else {
                classes.add(file);
            }
        }
        return classes;
    }

    private EjbDeployment deployBean(Bean bean, String jarLocation) throws OpenEJBException {
        EjbDeployment deployment = new EjbDeployment();

        out.println("\n-----------------------------------------------------------");
        out.println("Deploying bean: " + bean.getEjbName());
        out.println("-----------------------------------------------------------");
        deployment.setEjbName(bean.getEjbName());

        if (GENERATE_DEPLOYMENT_ID) {
            deployment.setDeploymentId(autoAssignDeploymentId(bean));
        } else {
            deployment.setDeploymentId(promptForDeploymentId());
        }

        if (AUTO_ASSIGN) {
            deployment.setContainerId(autoAssignContainerId(bean));
        } else {
            deployment.setContainerId(promptForContainerId(bean));
        }

        ResourceRef[] refs = bean.getResourceRef();
        if (refs.length > 0) {
            out.println("\n==--- Step 3 ---==");
            out.println("\nThis bean contains the following references to external \nresources:");

            out.println("\nName\t\t\tType\n");

            for (int i = 0; i < refs.length; i++) {
                out.print(refs[i].getResRefName() + "\t");
                out.println(refs[i].getResType());
            }

            out.println(
                    "\nThese references must be linked to the available resources\ndeclared in your config file.");

            out.println("Available resources are:");
            listResources(resources);
            for (int i = 0; i < refs.length; i++) {
                deployment.addResourceLink(resolveResourceRef(refs[i]));
            }
        }

        if (bean.getType().equals("CMP_ENTITY")) {
            if (bean.getHome() != null) {
                Class tempBean = loadClass(bean.getHome());
                if (hasFinderMethods(tempBean)) {
                    promptForOQLForEntityBeans(tempBean, deployment);
                }
            }
            if (bean.getLocalHome() != null) {
                Class tempBean = loadClass(bean.getLocalHome());
                if (hasFinderMethods(tempBean)) {
                    promptForOQLForEntityBeans(tempBean, deployment);
                }
            }
        }

        return deployment;
    }

    private Class loadClass(String className) throws OpenEJBException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, this.jarLocation));
        }
    }

    private boolean hasFinderMethods(Class bean)
            throws OpenEJBException {

        Method[] methods = bean.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find")
                    && !methods[i].getName().equals("findByPrimaryKey")) {
                return true;
            }
        }
        return false;
    }

    private void promptForOQLForEntityBeans(Class bean, EjbDeployment deployment)
            throws OpenEJBException {
        org.apache.openejb.alt.config.ejb.Query query;
        QueryMethod queryMethod;
        MethodParams methodParams;
        boolean instructionsPrinted = false;

        Method[] methods = bean.getMethods();
        Class[] parameterList;
        String answer = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find") && !methods[i].getName().equals("findByPrimaryKey")) {
                if (!instructionsPrinted) {
                    out.println("\n==--- Step 4 ---==");
                    out.println(
                            "\nThis part of the application allows you to add OQL (Object\n"
                                    + "Query Language) statements to your CMP Entity find methods.\n"
                                    + "Below is a list of find methods, each of which should get an \n"
                                    + "OQL statement.\n"
                                    + "\n"
                                    + "OQL statements are very similar to SQL statments with the \n"
                                    + "exception that they reference class names rather than table\n"
                                    + "names.  Find method parameters can be referenced in OQL \n"
                                    + "statements as $1, $2, $3, and so on.  \n"
                                    + "\n"
                                    + "If you had a find method in your home interface like this one:\n"
                                    + "\n"
                                    + "  public Employee findByLastName( String lName )\n"
                                    + "\n"
                                    + "Then you could use an OQL method like the following:\n"
                                    + "\n"
                                    + "  SELECT o FROM org.acme.employee.EmployeeBean o WHERE o.lastname = $1\n"
                                    + "\n"
                                    + "In this example, the $1 is referring to the first parameter, \n"
                                    + "which is the String lName.\n"
                                    + "\n"
                                    + "For more information on OQL see:\n"
                                    + "http://www.openejb.org/cmp_guide.html\n"
                    );

                    instructionsPrinted = true;
                }

                out.print("Method: ");
                parameterList = methods[i].getParameterTypes();

                out.print(methods[i].getName() + "(");
                for (int j = 0; j < parameterList.length; j++) {
                    out.print(parsePartialClassName(parameterList[j].getName()));
                    if (j != (parameterList.length - 1)) {
                        out.print(", ");
                    }
                }
                out.println(") ");

                try {
                    boolean replied = false;

                    while (!replied) {
                        out.println("Please enter your OQL Statement here.");
                        out.print("\nOQL Statement: ");
                        answer = in.readLine();
                        if (answer.length() > 0) {
                            replied = true;
                        }
                    }

                } catch (Exception e) {
                    throw new OpenEJBException(e.getMessage());
                }

                if (answer != null && !answer.equals("")) {
                    query = new org.apache.openejb.alt.config.ejb.Query();
                    methodParams = new MethodParams();
                    queryMethod = new QueryMethod();

                    for (int j = 0; j < parameterList.length; j++) {
                        methodParams.addMethodParam(parameterList[j].getName());
                    }

                    queryMethod.setMethodParams(methodParams);
                    queryMethod.setMethodName(methods[i].getName());
                    query.setQueryMethod(queryMethod);
                    query.setObjectQl(answer);

                    deployment.addQuery(query);

                    out.println("\nYour OQL statement was successfully added to the jar.\n");
                }
            }
        }
    }

    private String parsePartialClassName(String className) {
        if (className.indexOf('.') < 1) return className;
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /*------------------------------------------------------*/
    /*    Methods for deployment id mapping                 */
    /*------------------------------------------------------*/
    private void listBeanNames(Bean[] beans) {
        out.println("This jar contains the following beans:");
        for (int i = 0; i < beans.length; i++) {
            out.println("  " + beans[i].getEjbName());
        }
        out.println();
    }

    private String promptForDeploymentId() throws OpenEJBException {
        String answer = null;
        try {
            boolean replied = false;
            out.println("\n==--- Step 1 ---==");
            out.println("\nPlease specify a deployment id for this bean.");

            while (!replied) {
                out.println("Type the id or -help for more information.");
                out.print("\nDeployment ID: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(DEPLOYMENT_ID_HELP);
                } else if (answer.length() > 0) {
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }
        return answer;
    }

    private String autoAssignDeploymentId(Bean bean) throws OpenEJBException {
        String answer = bean.getEjbName();
        out.println("\n==--- Step 1 ---==");
        out.println("\nAuto assigning the ejb-name as the deployment id for this bean.");
        out.print("\nDeployment ID: " + answer);

        return answer;
    }

    /*------------------------------------------------------*/
    /*    Methods for container mapping                     */
    /*------------------------------------------------------*/

    private String promptForContainerId(Bean bean) throws OpenEJBException {
        String answer = null;
        boolean replied = false;
        out.println("\n==--- Step 2 ---==");
        out.println("\nPlease specify which container the bean will run in.");
        out.println("Available containers are:");

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            /* TODO: Allow or Automatically create a useable container
             * Stopping the deployment process because there is no
             * container of the right bean type is a terrible way
             * deal with the problem.  Instead, we should either
             * 1) Automatically create a container for them and notify them
             *    that we have done so.
             * 2) Allow them to create their own container.
             * 3) Some combination of 1 and 2.
             */
            out.println(
                    "!! There are no "
                            + bean.getType()
                            + " containers declared in "
                            + configFile
                            + " !!");
            out.println(
                    "A "
                            + bean.getType()
                            + " container must be declared and \nconfigured in your configuration file before this jar can\nbe deployed.");
            System.exit(-1);
        } else if (cs.length == 0) {
            /* TODO: Automatically assign the bean to the container
             * Since this is the only container in the system that
             * can service this bean type, either
             * 1) simply assign the bean to that container and notify the user.
             * 2) allow the user to create another container.
             */
        }

        listContainers(cs);
        int choice = 0;
        try {

            while (!replied) {
                out.println(
                        "\nType the number of the container\n-options to view the list again\nor -help for more information.");
                out.print("\nContainer: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(CONTAINER_ID_HELP);
                } else if ("-options".equals(answer)) {
                    listContainers(cs);
                } else if (answer.length() > 0) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch (NumberFormatException nfe) {
                        out.println("\'" + answer + "\' is not a numer.");
                        continue;
                    }
                    if (choice > cs.length || choice < 1) {
                        out.println(choice + " is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }
        return cs[choice - 1].getId();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        out.println("\n==--- Step 2 ---==");
        out.println("\nAuto assigning the container the bean will run in.");

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            /* TODO: Allow or Automatically create a useable container
             * Stopping the deployment process because there is no
             * container of the right bean type is a terrible way
             * deal with the problem.  Instead, we should either
             * 1) Automatically create a container for them and notify them
             *    that we have done so.
             * 2) Allow them to create their own container.
             * 3) Some combination of 1 and 2.
             */
            out.println(
                    "!! There are no "
                            + bean.getType()
                            + " containers declared in "
                            + configFile
                            + " !!");
            out.println(
                    "A "
                            + bean.getType()
                            + " container must be declared and \nconfigured in your configuration file before this jar can\nbe deployed.");
            System.exit(-1);
        }

        out.println("\nContainer: " + cs[0].getId());
        return cs[0].getId();
    }

    private void listContainers(Container[] containers) {
        out.println("\nNum \tType     \tID\n");

        for (int i = 0; i < containers.length; i++) {
            out.print((i + 1) + "\t");
            out.print(containers[i].getCtype() + "\t");
            out.println(containers[i].getId());
        }
    }

    /*------------------------------------------------------*/
    /*    Methods for connection(resource) mapping          */
    /*------------------------------------------------------*/
    private ResourceLink resolveResourceRef(ResourceRef ref) throws OpenEJBException {
        String answer = null;
        boolean replied = false;

        out.println("\nPlease link reference: " + ref.getResRefName());

        if (resources.length == 0) {
            /* TODO: 1, 2 or 3
             * 1) Automatically create a connector and link the reference to it.
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
            out.println("!! There are no resources declared in " + configFile + " !!");
            out.println(
                    "A resource connector must be declared and configured in \nyour configuration file before this jar can be deployed.");
            System.exit(-2);
        } else if (resources.length == 0) {
            /* TODO: 1, 2 or 3
             * 1) Automatically link the reference to the connector
             * 2) Something more creative
             * 3) Some ultra flexible combination of 1 and 2.
             */
        }

        int choice = 0;
        try {
            while (!replied) {
                out.println(
                        "\nType the number of the resource to link the bean's \nreference to, -options to view the list again, or -help\nfor more information.");
                out.print("\nResource: ");
                answer = in.readLine();
                if ("-help".equals(answer)) {
                    out.println(CONNECTOR_ID_HELP);
                } else if ("-options".equals(answer)) {
                    listResources(resources);
                } else if (answer.length() > 0) {
                    try {
                        choice = Integer.parseInt(answer);
                    } catch (NumberFormatException nfe) {
                        out.println("\'" + answer + "\' is not a number.");
                        continue;
                    }
                    if (choice > resources.length || choice < 1) {
                        out.println(choice + " is not an option.");
                        continue;
                    }
                    replied = true;
                }
            }
        } catch (Exception e) {
            throw new OpenEJBException(e.getMessage());
        }

        ResourceLink link = new ResourceLink();
        link.setResRefName(ref.getResRefName());
        link.setResId(resources[choice - 1].getId());
        return link;
    }

    private void listResources(Connector[] connectors) {
        out.println("\nNum \tID\n");

        for (int i = 0; i < connectors.length; i++) {
            out.print((i + 1) + "\t");
            out.println(connectors[i].getId());
        }
    }

    private void saveChanges(String jarFile, OpenejbJar openejbJar) throws OpenEJBException {
        out.println("\n-----------------------------------------------------------");
        out.println("Done collecting deployment information!");

        out.print("Creating the openejb-jar.xml file...");
        EjbJarUtils.writeOpenejbJar("META-INF/openejb-jar.xml", openejbJar);

        out.println("done");

        out.print("Writing openejb-jar.xml to the jar...");
        JarUtils.addFileToJar(jarFile, "META-INF/openejb-jar.xml");

        out.println("done");

        if (configChanged) {
            out.print("Updating your system config...");
            ConfigUtils.writeConfig(configFile, config);

            out.println("done");
        }

        out.println("\nCongratulations! Your jar is ready to use with OpenEJB.");
        out.println(
                "\nIf the OpenEJB remote server is already running, you will\nneed to restart it in order for OpenEJB to recognize your bean.");
        out.println(
                "\nNOTE: If you move or rename your jar file, you will have to\nupdate the path in this jar's deployment entry in your \nOpenEJB config file.");

    }

    /*------------------------------------------------------*/
    /*    Static methods                                    */
    /*------------------------------------------------------*/

    private static final String ERROR_NO_EJBJARFILES_SPECIFIED = "No EJB JARFILES specified";
    private static final String INCORRECT_OPTION = "Incorrect option: ";

    public static void main(String args[]) {
        SystemInstance system = SystemInstance.get();
        try {
            File directory = SystemInstance.get().getHome().getDirectory("lib");
            system.getClassPath().addJarsToPath(directory);
            File directory1 = SystemInstance.get().getHome().getDirectory("dist");
            system.getClassPath().addJarsToPath(directory1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Deploy d = new Deploy();

            if (args.length == 0) {
                error(ERROR_NO_EJBJARFILES_SPECIFIED);
                printHelp();
                return;
            }

            boolean noBeansToDeploySpecified = true;
            for (int i = 0; i < args.length; i++) {

                if (args[i].equals("-a")) {
                    d.AUTO_ASSIGN = true;
                    d.GENERATE_DEPLOYMENT_ID = true;
                } else if (args[i].equals("-m")) {
                    d.MOVE_JAR = true;
                } else if (args[i].equals("-f")) {
                    d.FORCE_OVERWRITE_JAR = true;
                } else if (args[i].equals("-c")) {
                    d.COPY_JAR = true;
                } else if (args[i].equals("-C")) {
                    d.AUTO_ASSIGN = true;
                } else if (args[i].equals("-D")) {
                    d.GENERATE_DEPLOYMENT_ID = true;
                } else if (args[i].equals("-conf")) {
                    if (args.length > i + 1) {
                        system.setProperty("openejb.configuration", args[++i]);
                    }
                } else if (args[i].equals("-l")) {
                    if (args.length > i + 1) {
                        system.setProperty("log4j.configuration", args[++i]);
                    }
                } else if (args[i].equals("-d")) {
                    if (args.length > i + 1) {
                        system.setProperty("openejb.home", args[++i]);
                    }
                } else if (args[i].equals("-examples")) {
                    printExamples();
                    noBeansToDeploySpecified = false;
                } else if (args[i].equals("-version")) {
                    printVersion();
                    noBeansToDeploySpecified = false;
                } else if (args[i].equals("--help")) {
                    printHelp();
                    noBeansToDeploySpecified = false;
                } else if (args[i].startsWith("-")) {
                    error(INCORRECT_OPTION + args[i]);
                    printHelp();
                    noBeansToDeploySpecified = false;
                } else {
                    noBeansToDeploySpecified = false;
                    d.init();
                    for (; i < args.length; i++) {
                        try {
                            d.deploy(args[i]);
                        } catch (Exception e) {
                            System.out.print("\nERROR in ");
                            System.out.println(args[i]);
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            if (noBeansToDeploySpecified) {
                error(ERROR_NO_EJBJARFILES_SPECIFIED);
                printHelp();
                return;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    private static void printVersion() {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            JarUtils.setHandlerSystemProperty();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
        } catch (java.io.IOException e) {
        }

        System.out.println(
                DEPLOY_TOOL_MSG_HEADER + " "
                        + versionInfo.get("version")
                        + "    build: "
                        + versionInfo.get("date")
                        + "-"
                        + versionInfo.get("time"));
        System.out.println("" + versionInfo.get("url"));
    }

    private static void printHelp() {
        String header = DEPLOY_TOOL_MSG_HEADER + " ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(HELP_BASE + "deploy.help").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void printExamples() {
        String header = DEPLOY_TOOL_MSG_HEADER + " ";
        try {
            JarUtils.setHandlerSystemProperty();
            Properties versionInfo = new Properties();
            versionInfo.load(
                    new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            header += versionInfo.get("version");
        } catch (java.io.IOException e) {
        }

        System.out.println(header);

        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResource(HELP_BASE + "deploy.examples").openConnection().getInputStream();

            int b = in.read();
            while (b != -1) {
                System.out.write(b);
                b = in.read();
            }
        } catch (java.io.IOException e) {
        }
    }

    private static void error(String msg) {
        System.err.println("\nERROR: " + msg + "\n");
    }

    private String moveJar(String jar) throws OpenEJBException {
        return EjbJarUtils.moveJar(jar, FORCE_OVERWRITE_JAR);
    }

    private String copyJar(String jar) throws OpenEJBException {
        return EjbJarUtils.copyJar(jar, FORCE_OVERWRITE_JAR);
    }

    private Container[] getUsableContainers(Bean bean) {
        return EjbJarUtils.getUsableContainers(containers, bean);
    }

    private void addDeploymentEntryToConfig(String jarLocation) {
        configChanged = ConfigUtils.addDeploymentEntryToConfig(jarLocation, config);
    }
}
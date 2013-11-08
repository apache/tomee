/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.enhance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.JavaVendors;
import org.apache.openjpa.lib.util.JavaVersions;
import org.apache.openjpa.lib.util.Localizer;


/**
 * Factory for obtaining an {@link Instrumentation} instance.
 *
 * @author Marc Prud'hommeaux
 * @since 1.0.0
 */
public class InstrumentationFactory {
    private static Instrumentation _inst;
    private static boolean _dynamicallyInstall = true;
    private static final String _name = InstrumentationFactory.class.getName();
    private static final Localizer _loc = Localizer.forPackage(
        InstrumentationFactory.class);

    /**
     * This method is not synchronized because when the agent is loaded from
     * getInstrumentation() that method will cause agentmain(..) to be called.
     * Synchronizing this method would cause a deadlock.
     * 
     * @param inst The instrumentation instance to be used by this factory.
     */
    public static void setInstrumentation(Instrumentation inst) {
        _inst = inst;
    }

    /**
     * Configures whether or not this instance should attempt to dynamically
     * install an agent in the VM. Defaults to <code>true</code>.
     */
    public static synchronized void setDynamicallyInstallAgent(boolean val) {
        _dynamicallyInstall = val;
    }

    /**
     * @param log OpenJPA log.
     * @return null if Instrumentation can not be obtained, or if any 
     * Exceptions are encountered.
     */
    public static synchronized Instrumentation getInstrumentation(final Log log) {
        if (log.isTraceEnabled() == true) {
            log.trace(_name + ".getInstrumentation() _inst:" + _inst
                + " _dynamicallyInstall:" + _dynamicallyInstall);
        }
        if ( _inst != null || !_dynamicallyInstall)
            return _inst;

        // dynamic loading of the agent is only available in JDK 1.6+
        if (JavaVersions.VERSION < 6) {
            if (log.isTraceEnabled() == true) {
                log.trace(_name + ".getInstrumentation() Dynamic loading only supported on Java SE 6 or later");
            }
            return null;
        }

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                // Dynamic agent enhancement should only occur when the OpenJPA library is 
                // loaded using the system class loader.  Otherwise, the OpenJPA
                // library may get loaded by separate, disjunct loaders, leading to linkage issues.
                try {
                    if (!InstrumentationFactory.class.getClassLoader().equals(
                        ClassLoader.getSystemClassLoader())) {
                        return null;
                    }
                } catch (Throwable t) {
                    return null;
                }
                JavaVendors vendor = JavaVendors.getCurrentVendor();                
                File toolsJar = null;
                // When running on IBM, the attach api classes are packaged in vm.jar which is a part
                // of the default vm classpath.
                if (vendor.isIBM() == false) {
                    // If we can't find the tools.jar and we're not on IBM we can't load the agent. 
                    toolsJar = findToolsJar(log);
                    if (toolsJar == null) {
                        return null;
                    }
                }

                Class<?> vmClass = loadVMClass(toolsJar, log, vendor);
                if (vmClass == null) {
                    return null;
                }
                String agentPath = getAgentJar(log);
                if (agentPath == null) {
                    return null;
                }
                loadAgent(log, agentPath, vmClass);
                return null;
            }// end run()
        });
        // If the load(...) agent call was successful, this variable will no 
        // longer be null.
        return _inst;
    }//end getInstrumentation()

    /**
     *  The method that is called when a jar is added as an agent at runtime.
     *  All this method does is store the {@link Instrumentation} for
     *  later use.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        InstrumentationFactory.setInstrumentation(inst);
    }

    /**
     * Create a new jar file for the sole purpose of specifying an Agent-Class
     * to load into the JVM.
     * 
     * @return absolute path to the new jar file.
     */
    private static String createAgentJar() throws IOException {
        File file =
            File.createTempFile(InstrumentationFactory.class.getName(), ".jar");
        file.deleteOnExit();

        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
        zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(zout));

        writer
            .println("Agent-Class: " + InstrumentationFactory.class.getName());
        writer.println("Can-Redefine-Classes: true");
        // IBM doesn't support retransform
        writer.println("Can-Retransform-Classes: " + Boolean.toString(JavaVendors.getCurrentVendor().isIBM() == false));

        writer.close();

        return file.getAbsolutePath();
    }

    /**
     * This private worker method attempts to find [java_home]/lib/tools.jar.
     * Note: The tools.jar is a part of the SDK, it is not present in the JRE.
     * 
     * @return If tools.jar can be found, a File representing tools.jar. <BR>
     *         If tools.jar cannot be found, null.
     */
    private static File findToolsJar(Log log) {
        String javaHome = System.getProperty("java.home");
        File javaHomeFile = new File(javaHome);

        File toolsJarFile = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
        if (toolsJarFile.exists() == false) {
            if (log.isTraceEnabled() == true) {
                log.trace(_name + ".findToolsJar() -- couldn't find default " + toolsJarFile.getAbsolutePath());
            }
            // If we're on an IBM SDK, then remove /jre off of java.home and try again.
            if (javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre") == true) {
                javaHomeFile = javaHomeFile.getParentFile();
                toolsJarFile = new File(javaHomeFile, "lib" + File.separator + "tools.jar");
                if (toolsJarFile.exists() == false) {
                    if (log.isTraceEnabled() == true) {
                        log.trace(_name + ".findToolsJar() -- for IBM SDK couldn't find " +
                            toolsJarFile.getAbsolutePath());
                    }
                }
            } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
                // If we're on a Mac, then change the search path to use ../Classes/classes.jar.
                if (javaHomeFile.getAbsolutePath().endsWith(File.separator + "Home") == true) {
                    javaHomeFile = javaHomeFile.getParentFile();
                    toolsJarFile = new File(javaHomeFile, "Classes" + File.separator + "classes.jar");
                    if (toolsJarFile.exists() == false) {
                        if (log.isTraceEnabled() == true) {
                            log.trace(_name + ".findToolsJar() -- for Mac OS couldn't find " +
                                toolsJarFile.getAbsolutePath());
                        }
                    }
                }
            }
        }

        if (toolsJarFile.exists() == false) {
            return null;
        } else {
            if (log.isTraceEnabled() == true) {
                log.trace(_name + ".findToolsJar() -- found " + toolsJarFile.getAbsolutePath());
            }
            return toolsJarFile;
        }
    }

    /**
     * This private worker method will return a fully qualified path to a jar
     * that has this class defined as an Agent-Class in it's
     * META-INF/manifest.mf file. Under normal circumstances the path should
     * point to the OpenJPA jar. If running in a development environment a
     * temporary jar file will be created.
     * 
     * @return absolute path to the agent jar or null if anything unexpected
     * happens.
     */
    private static String getAgentJar(Log log) {
        File agentJarFile = null;
        // Find the name of the File that this class was loaded from. That
        // jar *should* be the same location as our agent.
        CodeSource cs =
            InstrumentationFactory.class.getProtectionDomain().getCodeSource();
        if (cs != null) {   
            URL loc = cs.getLocation();
            if(loc!=null){
                agentJarFile = new File(loc.getFile());
            }
        }
        
        // Determine whether the File that this class was loaded from has this
        // class defined as the Agent-Class.
        boolean createJar = false;
        if (cs == null || agentJarFile == null
            || agentJarFile.isDirectory() == true) {
            createJar = true;
        }else if(validateAgentJarManifest(agentJarFile, log, _name) == false){
            // We have an agentJarFile, but this class isn't the Agent-Class.
            createJar=true;           
        }
        
        String agentJar;
        if (createJar == true) {
            // This can happen when running in eclipse as an OpenJPA
            // developer or for some reason the CodeSource is null. We
            // should log a warning here because this will create a jar
            // in your temp directory that doesn't always get cleaned up.
            try {
                agentJar = createAgentJar();
                if (log.isInfoEnabled() == true) {
                    log.info(_loc.get("temp-file-creation", agentJar));
                }
            } catch (IOException ioe) {
                if (log.isTraceEnabled() == true) {
                    log.trace(_name + ".getAgentJar() caught unexpected "
                        + "exception.", ioe);
                }
                agentJar = null;
            }
        } else {
            agentJar = agentJarFile.getAbsolutePath();
        }

        return agentJar;
    }//end getAgentJar

    /**
     * Attach and load an agent class. 
     * 
     * @param log Log used if the agent cannot be loaded.
     * @param agentJar absolute path to the agent jar.
     * @param vmClass VirtualMachine.class from tools.jar.
     */
    private static void loadAgent(Log log, String agentJar, Class<?> vmClass) {
        try {
            // first obtain the PID of the currently-running process
            // ### this relies on the undocumented convention of the
            // RuntimeMXBean's
            // ### name starting with the PID, but there appears to be no other
            // ### way to obtain the current process' id, which we need for
            // ### the attach process
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String pid = runtime.getName();
            if (pid.indexOf("@") != -1)
                pid = pid.substring(0, pid.indexOf("@"));

            // JDK1.6: now attach to the current VM so we can deploy a new agent
            // ### this is a Sun JVM specific feature; other JVMs may offer
            // ### this feature, but in an implementation-dependent way
            Object vm =
                vmClass.getMethod("attach", new Class<?>[] { String.class })
                    .invoke(null, new Object[] { pid });
            // now deploy the actual agent, which will wind up calling
            // agentmain()
            vmClass.getMethod("loadAgent", new Class[] { String.class })
                .invoke(vm, new Object[] { agentJar });
            vmClass.getMethod("detach", new Class[] {}).invoke(vm,
                new Object[] {});
        } catch (Throwable t) {
            if (log.isTraceEnabled() == true) {
                // Log the message from the exception. Don't log the entire
                // stack as this is expected when running on a JDK that doesn't
                // support the Attach API.
                log.trace(_name + ".loadAgent() caught an exception. Message: "
                    + t.getMessage());
            }
        }
    }

    /**
     * If <b>ibm</b> is false, this private method will create a new URLClassLoader and attempt to load the
     * com.sun.tools.attach.VirtualMachine class from the provided toolsJar file. 
     * 
     * <p>
     * If <b>ibm</b> is true, this private method will ignore the toolsJar parameter and load the 
     * com.ibm.tools.attach.VirtualMachine class. 
     * 
     * 
     * @return The AttachAPI VirtualMachine class <br>
     *         or null if something unexpected happened.
     */
    private static Class<?> loadVMClass(File toolsJar, Log log, JavaVendors vendor) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String cls = vendor.getVirtualMachineClassName();
            if (vendor.isIBM() == false) {
                loader = new URLClassLoader(new URL[] { toolsJar.toURI().toURL() }, loader);
            }
            return loader.loadClass(cls);
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace(_name
                    + ".loadVMClass() failed to load the VirtualMachine class");
            }
        }
        return null;
    }

    /**
     * This private worker method will validate that the provided agentClassName
     * is defined as the Agent-Class in the manifest file from the provided jar.
     * 
     * @param agentJarFile
     *            non-null agent jar file.
     * @param log
     *            non-null logger.
     * @param agentClassName
     *            the non-null agent class name.
     * @return True if the provided agentClassName is defined as the Agent-Class
     *         in the manifest from the provided agentJarFile. False otherwise.
     */
    private static boolean validateAgentJarManifest(File agentJarFile, Log log,
        String agentClassName) {
        try {
            JarFile jar = new JarFile(agentJarFile);
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return false;
            }
            Attributes attributes = manifest.getMainAttributes();
            String ac = attributes.getValue("Agent-Class");
            if (ac != null && ac.equals(agentClassName)) {
                return true;
            }
        } catch (Exception e) {
            if (log.isTraceEnabled() == true) {
                log.trace(_name
                    + ".validateAgentJarManifest() caught unexpected "
                    + "exception " + e.getMessage());
            }
        }
        return false;
    }// end validateAgentJarManifest   
}

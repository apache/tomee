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

import java.lang.instrument.Instrumentation;
import java.security.AccessController;
import java.util.List;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.util.ClassResolver;

/**
 * <p>
 * Java agent that makes persistent classes work with OpenJPA at runtime. This
 * is achieved by either running the enhancer on the classes as they are loaded,
 * or by redefining the classes on the fly. The agent is launched at JVM startup
 * from the command line:
 * </p>
 * 
 * <p>
 * <code>java -javaagent:openjpa.jar[=&lt;options&gt;]</code> The options string
 * should be formatted as a OpenJPA plugin, and may contain any properties
 * understood by the OpenJPA enhancer or any configuration properties. For
 * example:
 * </p>
 * 
 * <p>
 * <code>java -javaagent:openjpa.jar</code>
 * </p>
 * 
 * <p>
 * By default, if specified, the agent runs the OpenJPA enhancer on all classes
 * listed in the first persistence unit as they are loaded, and redefines all
 * other persistent classes when they are encountered. To disable enhancement at
 * class-load time and rely solely on the redefinition logic, set the
 * ClassLoadEnhancement flag to false. To disable redefinition and rely solely
 * on pre-deployment or class-load enhancement, set the RuntimeRedefinition flag
 * to false.
 * </p>
 * 
 * <p>
 * <code>java -javaagent:openjpa.jar=ClassLoadEnhancement=false</code>
 * </p>
 * 
 * @author Abe White
 * @author Patrick Linskey
 */
public class PCEnhancerAgent {

    private static boolean loadAttempted = false;
    private static boolean loadSuccessful = false;
    private static boolean disableDynamicAgent = false;

    /**
     * @return True if the Agent has ran successfully. False otherwise.
     */
    public static synchronized boolean getLoadSuccessful() {
        return loadSuccessful;
    }
    /**
     * @return True if the dynamic agent was disabled via configuration. 
     */
    public static void disableDynamicAgent(){
        disableDynamicAgent=true;
    }
    
    /**
     * @param log
     * @return True if the agent is loaded successfully
     */
    public static synchronized boolean loadDynamicAgent(Log log) {
        if (loadAttempted == false && disableDynamicAgent == false) {
            Instrumentation inst =
                InstrumentationFactory.getInstrumentation(log);
            if (inst != null) {
                premain("", inst);
                return true;
            } 
            // If we successfully get the Instrumentation, we will call premain
            // where loadAttempted will be set to true. This case is the path 
            // where we were unable to get Instrumentation so we need to set the
            // loadAttempted flag to true. We do this so we will only run
            // through this code one time.
            loadAttempted = true;
        }

        return false;
    }

    public static void premain(String args, Instrumentation inst) {
        // If the enhancer has already completed, noop. This can happen
        // if runtime enhancement is specified via javaagent, and
        // openJPA tries to dynamically enhance.
        // The agent will be disabled when running in an application
        // server.
        synchronized (PCEnhancerAgent.class) {
            if (loadAttempted == true) {
                return;
            }
            // See the comment in loadDynamicAgent as to why we set this to true
            // in multiple places.
            loadAttempted = true;
        }

        Options opts = Configurations.parseProperties(args);

        if (opts.containsKey("ClassLoadEnhancement") ||
            opts.containsKey("classLoadEnhancement")) {
            if (opts.getBooleanProperty(
                "ClassLoadEnhancement", "classLoadEnhancement", true))
                registerClassLoadEnhancer(inst, opts);
        }
        else if (opts.containsKey("RuntimeEnhancement") ||
            opts.containsKey("runtimeEnhancement")) {
            // Deprecated property setting
            if (opts.getBooleanProperty(
                "RuntimeEnhancement", "runtimeEnhancement", true))
                registerClassLoadEnhancer(inst, opts);
        } else {
            // if neither is set, then we should be turning it on. We need this
            // logic instead of just a getBooleanProperty() because of the
            // backwards-compat logic flow.
            registerClassLoadEnhancer(inst, opts);
        }

        if (opts.getBooleanProperty(
            "RuntimeRedefinition", "runtimeRedefinition", true)) {
            InstrumentationFactory.setInstrumentation(inst);
        } else {
            InstrumentationFactory.setDynamicallyInstallAgent(false);
        }
        loadSuccessful = true;
    }

    private static void registerClassLoadEnhancer(Instrumentation inst,
        Options opts) {
    	List<String> anchors = Configurations.
            getFullyQualifiedAnchorsInPropertiesLocation(opts);
    	for (String a : anchors) {
    		Options clonedOptions = (Options) opts.clone();
    		clonedOptions.setProperty("properties", a);
    		OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
    		Configurations.populateConfiguration(conf, clonedOptions);
    		// don't allow connections
    		conf.setConnectionUserName(null);
    		conf.setConnectionPassword(null);
    		conf.setConnectionURL(null);
    		conf.setConnectionDriverName(null);
    		conf.setConnectionFactoryName(null);
    		// set single class resolver
    		final ClassLoader tmpLoader = AccessController
    		    .doPrivileged(J2DoPrivHelper
    		    .newTemporaryClassLoaderAction(AccessController
    		    .doPrivileged(J2DoPrivHelper.getContextClassLoaderAction())
    		    ));
    		conf.setClassResolver(new ClassResolver() {
    		    public ClassLoader getClassLoader(Class context,
                    ClassLoader env) {
    		        return tmpLoader;
    		    }
    		});
    		conf.setReadOnly(Configuration.INIT_STATE_FREEZING);
    		conf.instantiateAll(); // avoid threading issues

    		PCClassFileTransformer transformer = new PCClassFileTransformer
    		    (conf.newMetaDataRepositoryInstance(), clonedOptions,
    		    tmpLoader);
    		inst.addTransformer(transformer);
    		conf.close();
    	}
    }
}

/*
 * Copyright (c) 1998, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998, 2018 IBM Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
//     tware - 1.0RC1 - refactor for OSGi
//     zhao jianyong - Bug 324627 - JarList stream is not explicitly closed after use in JavaSECMPInitializer
package org.eclipse.persistence.internal.jpa.deployment;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.exceptions.EntityManagerSetupException;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedGetConstructorFor;
import org.eclipse.persistence.internal.security.PrivilegedInvokeConstructor;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;

/**
 * INTERNAL:
 *
 * JavaSECMPInitializer is used to bootstrap the deployment of EntityBeans in EJB 3.0
 * when deployed in a non-managed setting
 *
 * It is called internally by our Provider
 *
 * @see org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider
 */
public class JavaSECMPInitializer extends JPAInitializer {

    // Used when byte code enhancing
    public static Instrumentation globalInstrumentation;
    // Adding this flag because globalInstrumentation could be set to null after weaving is done.
    protected static boolean usesAgent;
    // Adding this flag to know that within a JEE container so weaving should be enabled without an agent for non managed persistence units.
    protected static boolean isInContainer;
    // Indicates whether has been initialized - that could be done only once.
    protected static boolean isInitialized;
    // Singleton corresponding to the main class loader. Created only if agent is used.
    protected static JavaSECMPInitializer initializer;
    // Used as a lock in getJavaSECMPInitializer.
    private static final Object initializationLock = new Object();

    public static boolean isInContainer() {
        return isInContainer;
    }
    public static void setIsInContainer(boolean isInContainer) {
        JavaSECMPInitializer.isInContainer = isInContainer;
    }

    /**
     * Get the singleton entityContainer.
     */
    public static JavaSECMPInitializer getJavaSECMPInitializer() {
        return getJavaSECMPInitializer(Thread.currentThread().getContextClassLoader(), null, false);
    }
    public static JavaSECMPInitializer getJavaSECMPInitializer(ClassLoader classLoader) {
        return getJavaSECMPInitializer(classLoader, null, false);
    }
    public static JavaSECMPInitializer getJavaSECMPInitializerFromAgent() {
        return getJavaSECMPInitializer(Thread.currentThread().getContextClassLoader(), null, true);
    }
    public static JavaSECMPInitializer getJavaSECMPInitializerFromMain(Map m) {
        return getJavaSECMPInitializer(Thread.currentThread().getContextClassLoader(), m, false);
    }
    public static JavaSECMPInitializer getJavaSECMPInitializer(ClassLoader classLoader, Map m, boolean fromAgent) {
        if(!isInitialized) {
            if(globalInstrumentation != null) {
                synchronized(initializationLock) {
                    if(!isInitialized) {
                        initializeTopLinkLoggingFile();
                        if(fromAgent) {
                            AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_initialize_from_agent", (Object[])null);
                        }
                        usesAgent = true;
                        initializer = new JavaSECMPInitializer(classLoader);
                        initializer.initialize(m != null ? m : new HashMap(0));
                        // all the transformers have been added to instrumentation, don't need it any more.
                        globalInstrumentation = null;
                    }
                }
            }
            isInitialized = true;
        }
        if(initializer != null && initializer.getInitializationClassLoader() == classLoader) {
            return initializer;
        } else {
            // when agent is not used initializer does not need to be initialized.
            return new JavaSECMPInitializer(classLoader);
        }
    }

    /**
     * User should not instantiate JavaSECMPInitializer.
     */
    protected JavaSECMPInitializer() {
        super();
    }

    protected JavaSECMPInitializer(ClassLoader loader) {
        super();
        this.initializationClassloader = loader;
    }

    /**
     * Check whether weaving is possible and update the properties and variable as appropriate
     * @param properties The list of properties to check for weaving and update if weaving is not needed
     */
    @Override
    public void checkWeaving(Map properties){
        String weaving = EntityManagerFactoryProvider.getConfigPropertyAsString(PersistenceUnitProperties.WEAVING, properties, null);
        // Check usesAgent instead of globalInstrumentation!=null because globalInstrumentation is set to null after initialization,
        // but we still have to keep weaving so that the resulting projects correspond to the woven (during initialization) classes.
        if (!usesAgent && !isInContainer) {
            if (weaving == null) {
               properties.put(PersistenceUnitProperties.WEAVING, "false");
               weaving = "false";
            } else if (weaving.equalsIgnoreCase("true")) {
                throw new PersistenceException(EntityManagerSetupException.wrongWeavingPropertyValue());
            }
        }
        if ((weaving != null) && ((weaving.equalsIgnoreCase("false")) || (weaving.equalsIgnoreCase("static")))){
            shouldCreateInternalLoader = false;
        }
    }

    /**
     * Create a temporary class loader that can be used to inspect classes and then
     * thrown away.  This allows classes to be introspected prior to loading them
     * with application's main class loader enabling weaving.
     */
    @Override
    protected ClassLoader createTempLoader(Collection col) {
        return createTempLoader(col, true);
    }

    @Override
    protected ClassLoader createTempLoader(Collection col, boolean shouldOverrideLoadClassForCollectionMembers) {
        if (!shouldCreateInternalLoader) {
            return Thread.currentThread().getContextClassLoader();
        }

        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        if (!(currentLoader instanceof URLClassLoader)) {
            //we can't create a TempEntityLoader so just use the current one
            //shouldn't be a problem (and should only occur) in JavaSE
            return currentLoader;
        }
        URL[] urlPath = ((URLClassLoader)currentLoader).getURLs();

        ClassLoader tempLoader = null;
        if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()) {
            try {
                Class[] argsClasses = new Class[] { URL[].class, ClassLoader.class, Collection.class, boolean.class };
                Object[] args = new Object[] { urlPath, currentLoader, col, shouldOverrideLoadClassForCollectionMembers };
                Constructor classLoaderConstructor = AccessController.doPrivileged(new PrivilegedGetConstructorFor(TempEntityLoader.class, argsClasses, true));
                tempLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedInvokeConstructor(classLoaderConstructor, args));
            } catch (PrivilegedActionException privilegedException) {
                throw new PersistenceException(EntityManagerSetupException.failedToInstantiateTemporaryClassLoader(privilegedException));
            }
        } else {
            tempLoader = new TempEntityLoader(urlPath, currentLoader, col, shouldOverrideLoadClassForCollectionMembers);
        }

        AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_tempLoader_created", tempLoader);
        AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_shouldOverrideLoadClassForCollectionMembers", Boolean.valueOf(shouldOverrideLoadClassForCollectionMembers));

        return tempLoader;
    }

    /**
     * INTERNAL:
     * Should be called only by the agent. (when weaving classes)
     * If succeeded return true, false otherwise.
     */
    protected static void initializeFromAgent(Instrumentation instrumentation) throws Exception {
        // Squirrel away the instrumentation for later
        globalInstrumentation = instrumentation;
        getJavaSECMPInitializerFromAgent();
    }

    /**
     * Usually JavaSECMPInitializer is initialized from agent during premain
     * to ensure that the classes to be weaved haven't been loaded before initialization.
     * However, in this case initialization can't be debugged.
     * In order to be able to debug initialization specify
     * in java options -javaagent with parameter "main":  (note: a separate eclipselink-agent.jar is no longer required)
     *   -javaagent:c:\trunk\eclipselink.jar=main
     * that causes instrumentation to be cached during premain and postpones initialization until main.
     * With initialization done in main (during the first createEntityManagerFactory call)
     * there's a danger of the classes to be weaved being already loaded.
     * In that situation initializeFromMain should be called before any classes are loaded.
     * The sure-to-work method would be to create a new runnable class with a main method
     * consisting of just two lines: calling initializeFromMain
     * followed by reflective call to the main method of the original runnable class.
     * The same could be achieved by calling PersistenceProvider.createEntityManagerFactory method instead
     * of JavaSECMPInitializer.initializeFromMain method,
     * however initializeFromMain might be more convenient because it
     * doesn't require a persistence unit name.
     * The method doesn't do anything if JavaSECMPInitializer has been already initialized.
     * @param m - a map containing the set of properties to instantiate with.
     */
    public static void initializeFromMain(Map m) {
        getJavaSECMPInitializerFromMain(m);
    }

    /**
     * The version of initializeFromMain that passes an empty map.
     */
    public static void initializeFromMain() {
        initializeFromMain(new HashMap());
    }

    /**
     * Register a transformer.  In this case, we use the instrumentation to add a transformer for the
     * JavaSE environment
     * @param transformer
     * @param persistenceUnitInfo
     */
    @Override
    public void registerTransformer(final ClassTransformer transformer, PersistenceUnitInfo persistenceUnitInfo, Map properties){
        if ((transformer != null) && (globalInstrumentation != null)) {
            AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_register_transformer", persistenceUnitInfo.getPersistenceUnitName());
            globalInstrumentation.addTransformer(new ClassFileTransformer() {
                // adapt ClassTransformer to ClassFileTransformer interface
                @Override
                public byte[] transform(
                        ClassLoader loader, String className,
                        Class<?> classBeingRedefined,
                        ProtectionDomain protectionDomain,
                        byte[] classfileBuffer) throws IllegalClassFormatException {
                    return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                }
            });
        } else if (transformer == null) {
            AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_transformer_is_null", null, true);
        } else if (globalInstrumentation == null) {
            AbstractSessionLog.getLog().log(SessionLog.FINER, SessionLog.WEAVER, "cmp_init_globalInstrumentation_is_null", null, true);
        }
    }

    /**
     * Indicates whether puName uniquely defines the persistence unit.
     * usesAgent means that it is a stand alone SE case.
     * Otherwise it could be an application server case where different persistence units
     * may have the same name: that could happen if they are loaded by different classloaders;
     * the common case is the same persistence unit jar deployed in several applications.
     */
    @Override
    public boolean isPersistenceUnitUniquelyDefinedByName() {
        return usesAgent;
    }

    /**
     * Indicates whether initialization has already occurred.
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Indicates whether Java agent and globalInstrumentation was used.
     */
    public static boolean usesAgent() {
        return usesAgent;
    }

    /**
     * Indicates whether initialPuInfos and initialEmSetupImpls are used.
     */
    @Override
    protected boolean keepAllPredeployedPersistenceUnits() {
        return usesAgent;
    }

    /*********************************/
    /***** Temporary Classloader *****/
    /*********************************/
    /**
     * This class loader is provided at initialization time to allow us to temporarily load
     * domain classes so we can examine them for annotations.  After they are loaded we will throw this
     * class loader away.  Transformers can then be registered on the real class loader to allow
     * weaving to occur.
     *
     * It selectively loads classes based on the list of classnames it is instantiated with.  Classes
     * not on that list are allowed to be loaded by the parent.
     */
    public static class TempEntityLoader extends URLClassLoader {
        Collection classNames;
        boolean shouldOverrideLoadClassForCollectionMembers;

        //added to resolved gf #589 - without this, the orm.xml url would be returned twice
        @Override
        public Enumeration<URL> getResources(String name) throws java.io.IOException {
            return this.getParent().getResources(name);
        }

        public TempEntityLoader(URL[] urls, ClassLoader parent, Collection classNames, boolean shouldOverrideLoadClassForCollectionMembers) {
            super(urls, parent);
            this.classNames = classNames;
            this.shouldOverrideLoadClassForCollectionMembers = shouldOverrideLoadClassForCollectionMembers;
        }

        public TempEntityLoader(URL[] urls, ClassLoader parent, Collection classNames) {
            this(urls, parent, classNames, true);
        }

        // Indicates if the classLoad should be overridden for the passed className.
        // Returns true in case the class should NOT be loaded by parent classLoader.
        protected boolean shouldOverrideLoadClass(String name) {
            if (shouldOverrideLoadClassForCollectionMembers) {
                // Override classLoad if the name is in collection
                return (classNames != null) && classNames.contains(name);
            } else {
                // Directly opposite: Override classLoad if the name is NOT in collection.
                // Forced to check for java. and javax. packages here, because even if the class
                // has been loaded by parent loader we would load it again
                // (see comment in loadClass)
                return !name.startsWith("java.") && !name.startsWith("javax.") && !name.startsWith("jakarta.") && ((classNames == null) || !classNames.contains(name));
            }
        }

        @Override
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (shouldOverrideLoadClass(name)) {
                // First, check if the class has already been loaded.
                // Note that the check only for classes loaded by this loader,
                // it doesn't return true if the class has been loaded by parent loader
                // (forced to live with that because findLoadedClass method defined as final protected:
                //  neither can override it nor call it on the parent loader)
                Class c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(name);
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }
}
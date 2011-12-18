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

package org.apache.openejb.junit.context;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.junit.ContextConfig;
import org.apache.openejb.junit.Property;
import org.apache.openejb.junit.RunTestAs;
import org.apache.openejb.junit.TestResource;
import org.apache.openejb.junit.TestResourceTypes;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * To implement your own context, you need to create an implementation of TestContext
 * which would configure the test when instructed. You can then use whatever method
 * of configuration you choose.
 *
 */
public class OpenEjbTestContext implements TestContext {
    protected static final String REALM_PROPERTY_KEY = "openejb.authentication.realmName";

    protected static final String LOGIN_CONFIG_RESOURCE = "/META-INF/openejb-test-login.config";

    protected static final String DEFAULT_CONFIG_FILE_RESOURCE = "/META-INF/default-openejb-test-config.properties";

    /**
     * Properties object used to initialize InitialContext
     */
    protected Properties contextConfig;

    /**
     * Context's InitialContext
     */
    private InitialContext initialContext;

    /**
     * Test class
     */
    private Class<?> clazz;

    /**
     * Method being run on test
     */
    private Method method;

    /**
     * Security role to execute as
     */
    private String securityRole;

    /**
     * Constructs a context for a class
     *
     * @param testClazz
     */
    public OpenEjbTestContext(Class clazz) {
        this(clazz, null);
    }

    /**
     * Constructs a context for a method
     *
     * @param testClazz
     * @param method
     */
    public OpenEjbTestContext(Method method) {
        this(method, null);
    }

    /**
     * Constructs a context for a class
     *
     * @param testClazz
     */
    public OpenEjbTestContext(Class clazz, String securityRole) {
        this.clazz = clazz;
        this.securityRole = securityRole;
    }

    /**
     * Constructs a context for a method
     *
     * @param testClazz
     * @param method
     */
    public OpenEjbTestContext(Method method, String securityRole) {
        this.clazz = method.getDeclaringClass();
        this.method = method;
        this.securityRole = securityRole;
    }

    public void configureTest(Object testObj) {
        try {
            if (testObj.getClass().isAnnotationPresent(LocalClient.class)) {
                getInitialContext().bind("inject", testObj);
            }

            // perform custom injections
            performInjections(testObj);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load configuration.", e);
        }
        catch (NamingException e) {
            throw new RuntimeException("Failed to configure object.", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Unknown error trying to configure object.", e);
        }
    }

    /**
     * Returns this context's InitialContext, creating it if necessary.
     *
     * @return InitialContext for this TestContext
     * @throws NamingException
     */
    protected InitialContext getInitialContext() throws NamingException {
        if (initialContext == null) {
            // set the property for security realm RIGHT before we load the InitialContext
            String loginConfig = OpenEjbTestContext.class.getResource(LOGIN_CONFIG_RESOURCE).toExternalForm();
            System.setProperty("java.security.auth.login.config", URLDecoder.decode(loginConfig));

            try {
                Properties config = getContextConfig();
                initialContext = new InitialContext(config);
            }
            catch (IOException e) {
                throw new NamingException("Failed to load initial context configuration: " + e.getMessage());
            }
        }

        return initialContext;
    }

    /**
     * Constructs the configuration needed to create the InitialContext. This will
     * be determined from the class/method supplied to the constructor.
     *
     * @return
     */
    protected Properties getContextConfig() throws IOException {
        if (contextConfig != null) {
            return contextConfig;
        }

        Properties env = new Properties();
        boolean loadedConfig = false;

        if (clazz.isAnnotationPresent(ContextConfig.class)) {
            loadedConfig |= loadConfig(env, clazz.getAnnotation(ContextConfig.class));
        }

        if (method != null && method.isAnnotationPresent(ContextConfig.class)) {
            loadedConfig |= loadConfig(env, method.getAnnotation(ContextConfig.class));
        }

        // no properties loaded, use the "default" configuration
        if (!loadedConfig) {
            InputStream in = OpenEjbTestContext.class.getResourceAsStream(DEFAULT_CONFIG_FILE_RESOURCE);
            if (in == null) {
                throw new FileNotFoundException("Default configuration file not found. Specify configuration " +
                        "properties to initialize OpenEJB using @ContextConfig.");
            }
            env.load(in);

            // if it's still empty, something bad has happened, and OpenEJB won't initialize. Complain.
            if (env.size() == 0) {
                throw new IOException("Context configuration failed to load, so OpenEJB won't load either. Specify configuration " +
                        "properties for initializing OpenEJB using @ContextConfig.");
            }
        }

        configureSecurity(env);

        contextConfig = env;
        return env;
    }

    /**
     * Interprets and loads InitialContext properties from the ContextConfig annotation
     *
     * @param env
     * @param contextConfig
     * @return true if any properties were loaded
     */
    protected boolean loadConfig(Properties env, ContextConfig contextConfig) throws IOException {
        boolean loadedConfig = false;

        loadedConfig = loadConfigFile(env, contextConfig);
        loadedConfig |= loadConfigProperties(env, contextConfig);

        return loadedConfig;
    }

    /**
     * Loads the direct properties from the annotation configuration into the given Properties object
     *
     * @param env
     * @param contextConfig
     * @return true if any properties were loaded
     */
    protected boolean loadConfigProperties(Properties env, ContextConfig contextConfig) {
        boolean loadedConfig = false;

        if (contextConfig.properties().length > 0) {
            for (Property p : contextConfig.properties()) {
                if (p.value() != null) {
                    loadedConfig = true;
                    Util.addProperty(env, p.value());
                }
            }
        }

        return loadedConfig;
    }

    /**
     * Loads the configuration file specified in the {@link org.apache.openejb.junit.ContextConfig} annotation
     * into the specified Properties instance
     *
     * @param env
     * @param contextConfig
     * @return true if any properties were loaded
     */
    protected boolean loadConfigFile(Properties env, ContextConfig contextConfig)
            throws IOException {
        // properties file
        if (contextConfig.configFile().length() > 0) {
            InputStream in = clazz.getResourceAsStream(contextConfig.configFile());
            if (in == null) {
                throw new FileNotFoundException("Cannot find resource '" + contextConfig.configFile() + "' in classpath: " + clazz.getName());
            }
            env.load(in);

            return env.size() > 0;
        }

        return false;
    }

    /**
     * Loads the security configuration into the given Properties object
     *
     * @param env
     */
    protected void configureSecurity(Properties env) {
        // if a securityRole isn't already configured, use the RunTestAs annotation if available
        if (securityRole == null) {
            if (method != null && method.isAnnotationPresent(RunTestAs.class)) {
                securityRole = method.getAnnotation(RunTestAs.class).value();
            } else if (clazz.isAnnotationPresent(RunTestAs.class)) {
                securityRole = clazz.getAnnotation(RunTestAs.class).value();
            }
        }

        if (securityRole != null) {
            env.put(REALM_PROPERTY_KEY, "OpenEjbJunitSecurityRealm");
            env.put(InitialContext.SECURITY_PRINCIPAL, securityRole);
            env.put(InitialContext.SECURITY_CREDENTIALS, "[no-password-needed]");
        }
    }

    /**
     * Performs any non-OpenEJB type injections on the test object. It will "prefer"
     * a setter, and therefore I made it work on private fields as well. So if you
     * are injecting to a private field and wish to have some control over it, create
     * a setter according to the JavaBeans idioms.
     * <p/>
     * If the setter isn't found OR it fails, then an attempt will be made to set
     * it directly, and a message will be printed when it fails.
     */
    private void performInjections(Object testObj) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            Object injectValue = getInjectionValue(field);

            // no value determined, try next field
            if (injectValue == null) {
                continue;
            }

            // now inject it through the setter
            try {
                Method setterMethod = Util.findSetter(clazz, field, injectValue);
                if (setterMethod != null) {
                    setterMethod.invoke(testObj, injectValue);
                    continue;
                }
            }
            catch (Exception e) {
                System.err.println("Failed to perform setter injection on: " + clazz.getCanonicalName() + "." + field.getName());
                e.printStackTrace();
            }

            // do direct injection
            try {
                if (!Modifier.isPublic(field.getModifiers())) {
                    field.setAccessible(true);
                }

                field.set(testObj, injectValue);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to inject on: " + clazz.getCanonicalName() + "." + field.getName(), e);
            }
        }
    }

    /**
     * Analyzes the field and returns any values which should be injected on it
     *
     * @param field
     * @return reference to value to inject, or null if nothing should be injected
     */
    protected Object getInjectionValue(Field field) throws Exception {
        // determine the value to inject
        if (field.isAnnotationPresent(TestResource.class)) {
            TestResource resourceConfig = field.getAnnotation(TestResource.class);
            String resourceType = resourceConfig.value();

            if (resourceType == null) {
                throw new IllegalArgumentException("Null TestResource type '" + resourceType +
                        "' on field: " + clazz.getCanonicalName() + "." + field.getName());
            } else {
                if (TestResourceTypes.CONTEXT_CONFIG.equals(resourceType)) {
                    return getContextConfig();
                } else if (TestResourceTypes.INITIALCONTEXT.equals(resourceType)) {
                    return getInitialContext();
                } else {
                    return getOtherTestResource(resourceConfig);
                }
            }
        }

        return null;
    }

    /**
     * Override to perform custom resource types injection. This method will be called
     * when whatever value was specified in the {@link TestResource} annotation wasn't
     * understood by the {@link #performInjections(java.lang.Object) } method. This
     * method will be called, supplying the annotation, and you can then interpret and
     * create the value to be injected. By default this method just returns null.
     * <p/>
     * You can use this to inject values into annotated fields which contain custom
     * values in their names.
     *
     * @param resourceConfig
     * @return instance to inject into annotated field.
     */
    protected Object getOtherTestResource(TestResource resourceConfig) {
        return null;
    }

    /**
     * @return the test class
     */
    protected Class<?> getTestClass() {
        return clazz;
    }

    /**
     * @return the test method for which this context was created
     */
    protected Method getTestMethod() {
        return method;
    }
}

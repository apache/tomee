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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class Main {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        args = siftArgs(args);

        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.openejb.client");

        // the new initial context is automatically hooked up to the server side
        // java:openejb/client/${clientModuleId} tree
        final InitialContext initialContext = new InitialContext();

        // path to the client jar file
        final String path = (String) initialContext.lookup("java:info/path");
        // TODO: Download the file
        final File file = new File(path);

        // Create a child class loader containing the application jar
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        } else {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, classLoader);
        }
        Thread.currentThread().setContextClassLoader(classLoader);

        // load the main class and get the main method
        // do this first so we fail fast on a bad class path
        final String mainClassName = (String) initialContext.lookup("java:info/mainClass");
        final Class mainClass = classLoader.loadClass(mainClassName);
        final Method mainMethod = mainClass.getMethod("main", String[].class);

        // load the callback handler class
        // again do this before any major work so we can fail fase
        Class callbackHandlerClass = null;
        try {
            final String callbackHandlerName = (String) initialContext.lookup("java:info/callbackHandler");
            callbackHandlerClass = classLoader.loadClass(callbackHandlerName);
        } catch (NameNotFoundException ignored) {
        }

        final InjectionMetaData injectionMetaData = (InjectionMetaData) initialContext.lookup("java:info/injections");
        ClientInstance.get().setComponent(InjectionMetaData.class, injectionMetaData);
        for (final Injection injection : injectionMetaData.getInjections()) {
            try {
                final Object value = initialContext.lookup("java:" + injection.getJndiName());
                final Class target = classLoader.loadClass(injection.getTargetClass());
                final Field field = target.getDeclaredField(injection.getName());
                setAccessible(field);
                field.set(null, value);
            } catch (Throwable e) {
                //noinspection UseOfSystemOutOrSystemErr
                System.err.println("Injection FAILED: class=" + injection.getTargetClass() + ", name=" + injection.getName() + ", jndi-ref=" + injection.getJndiName());
                e.printStackTrace();
            }
        }

        // if there is no security then just call the main method
        final Object[] mainArgs = new Object[]{args};
        if (callbackHandlerClass == null) {
            invoke(mainMethod, mainArgs);
        } else {
            // create the callback handler
            final CallbackHandler callbackHandler = (CallbackHandler) callbackHandlerClass.newInstance();

            // initialize the jaas system
            loadJassLoginConfig(classLoader);

            // login
            final LoginContext loginContext = new LoginContext("ClientLogin", callbackHandler);
            loginContext.login();

            // success - get the subject
            final Subject subject = loginContext.getSubject();

            // call the main method in a doAs so the subject is associated with the thread
            try {
                Subject.doAs(subject, new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        invoke(mainMethod, mainArgs);
                        return null;
                    }
                });
            } finally {
                // And finally, logout
                loginContext.logout();
            }
        }
    }

    private static void invoke(final Method mainMethod, final Object[] mainArgs) throws Exception {
        try {
            mainMethod.invoke(null, mainArgs);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new Error(e);
        }
    }

    private static void loadJassLoginConfig(final ClassLoader classLoader) {
        final String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            final URL resource = classLoader.getResource("client.login.conf");
            if (resource != null) {
                System.setProperty("java.security.auth.login.config", URLDecoder.decode(resource.toExternalForm()));
            }
        }
    }

    private static String[] siftArgs(final String[] args) {
        final List<String> argsList = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.indexOf("-D") == -1) {
                argsList.add(arg);
            } else {
                final String prop = arg.substring(arg.indexOf("-D") + 2, arg.indexOf("="));
                final String val = arg.substring(arg.indexOf("=") + 1);
                System.setProperty(prop, val);
            }
        }
        return argsList.toArray(new String[argsList.size()]);
    }

    private static void setAccessible(final Field field) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                field.setAccessible(true);
                return null;
            }
        });
    }
}

/**
 *
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
package org.apache.openejb;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.AsmParameterNameLoader;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class InjectionProcessor<T> {

    static {
        AsmParameterNameLoader.install();
    }
    
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, InjectionProcessor.class);
    private final Class<? extends T> beanClass;
    private final List<Injection> injections;
    private final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    private final List<Method> postConstructMethods;
    private final List<Method> preDestroyMethods;
    private final Context context;
    private T instance;
    private T suppliedInstance;

    public InjectionProcessor(T suppliedInstance, List<Injection> injections, Context context) {
        this.beanClass = null;
        this.suppliedInstance = suppliedInstance;
        this.injections = injections;
        this.context = context;
        postConstructMethods = null;
        preDestroyMethods = null;
    }

    public InjectionProcessor(Class<? extends T> beanClass, List<Injection> injections, Context context) {
        this.beanClass = beanClass;
        this.injections = injections;
        this.context = context;
        postConstructMethods = null;
        preDestroyMethods = null;
    }

    public InjectionProcessor(Class<? extends T> beanClass, List<Injection> injections, List<Method> postConstructMethods, List<Method> preDestroyMethods, Context context) {
        this.beanClass = beanClass;
        this.injections = injections;
        this.postConstructMethods = postConstructMethods;
        this.preDestroyMethods = preDestroyMethods;
        this.context = context;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public T createInstance() throws OpenEJBException {
        if (instance == null) {
            construct();
        }
        return instance;
    }

    public T getInstance() {
        return instance;
    }

    private void construct() throws OpenEJBException {
        if (instance != null) throw new IllegalStateException("Instance already constructed");

        Class<? extends T> clazz = beanClass;

        ObjectRecipe objectRecipe;
        if (suppliedInstance != null) {
            clazz = (Class<? extends T>) suppliedInstance.getClass();
            objectRecipe = PassthroughFactory.recipe(suppliedInstance);
        } else {
            objectRecipe = new ObjectRecipe(clazz);
        }
        
        objectRecipe.allow(Option.FIELD_INJECTION);
        objectRecipe.allow(Option.PRIVATE_PROPERTIES);
        objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);
        objectRecipe.allow(Option.NAMED_PARAMETERS);

        fillInjectionProperties(objectRecipe);

        for (Entry<String, Object> entry : properties.entrySet()) {
            objectRecipe.setProperty(entry.getKey(), entry.getValue());
        }

        Object object;
        try {
            object = objectRecipe.create(clazz.getClassLoader());
        } catch (Exception e) {
            throw new OpenEJBException("Error while creating bean " + clazz.getName(), e);
        }

        Map unsetProperties = objectRecipe.getUnsetProperties();
        if (unsetProperties.size() > 0) {
            for (Object property : unsetProperties.keySet()) {
                logger.warning("Injection: No such property '" + property + "' in class " + clazz.getName());
            }
        }
        instance = clazz.cast(object);
    }

    public void postConstruct() throws OpenEJBException {
        if (instance == null) throw new IllegalStateException("Instance has not been constructed");
        if (postConstructMethods == null) return;
        for (Method postConstruct : postConstructMethods) {
            try {
                postConstruct.invoke(instance);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException && e.getCause() instanceof Exception) {
                    e = (Exception) e.getCause();
                }
                throw new OpenEJBException("Error while calling post construct method", e);
            }
        }
    }

    public void preDestroy() {
        if (instance == null) return;
        if (preDestroyMethods == null) return;
        for (Method preDestroy : preDestroyMethods) {
            try {
                preDestroy.invoke(instance);
            } catch (Exception e) {
                if (e instanceof InvocationTargetException && e.getCause() instanceof Exception) {
                    e = (Exception) e.getCause();
                }
                logger.error("Error while calling pre destroy method", e);
            }
        }
    }

    private void fillInjectionProperties(ObjectRecipe objectRecipe) {
        if (injections == null) return;
        
        boolean usePrefix = true;
        try {
            if (beanClass != null) beanClass.getConstructor();
        } catch (NoSuchMethodException e) {
            // Using constructor injection
            // xbean can't handle the prefix yet
            usePrefix = false;
        }

        Class clazz = beanClass;

        if (suppliedInstance != null) clazz = suppliedInstance.getClass();

        for (Injection injection : injections) {
            if (!injection.getTarget().isAssignableFrom(clazz)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object value = context.lookup(jndiName);

                String prefix;
                if (usePrefix) {
                    prefix = injection.getTarget().getName() + "/";
                } else {
                    prefix = "";
                }

                objectRecipe.setProperty(prefix + injection.getName(), value);
            } catch (NamingException e) {
                logger.warning("Injection data not found in JNDI context: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget().getName() + "/" + injection.getName());
            }
        }
    }

    public static Context unwrap(Context context) {
        if (context == null) return null;
        try {
            context = (Context) context.lookup("comp/env/");
        } catch (NamingException notAnIssue) {
            //TODO figure out which clause should work and remove the other one.
            try {
                context = (Context) context.lookup("java:comp/env/");
            } catch (NamingException notAnIssue2) {
            }
        }
        
        return context;
    }

}

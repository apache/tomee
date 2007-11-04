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
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.StaticRecipe;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class InjectionProcessor<T> {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, InjectionProcessor.class);
    private final Class<? extends T> beanClass;
    private final List<Injection> injections;
    private final List<Method> postConstructMethods;
    private final List<Method> preDestroyMethods;
    private final Context context;
    private T instance;

    public InjectionProcessor(Class<? extends T> beanClass, List<Injection> injections, List<Method> postConstructMethods, List<Method> preDestroyMethods, Context context) {
        this.beanClass = beanClass;
        this.injections = injections;
        this.postConstructMethods = postConstructMethods;
        this.preDestroyMethods = preDestroyMethods;
        this.context = context;
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

        ObjectRecipe objectRecipe = new ObjectRecipe(beanClass);
        objectRecipe.allow(Option.FIELD_INJECTION);
        objectRecipe.allow(Option.PRIVATE_PROPERTIES);
        objectRecipe.allow(Option.IGNORE_MISSING_PROPERTIES);

        fillInjectionProperties(objectRecipe);

        Object object = null;
        try {
            object = objectRecipe.create(beanClass.getClassLoader());
        } catch (Exception e) {
            throw new OpenEJBException("Error while creating bean " + beanClass.getName(), e);
        }

        Map unsetProperties = objectRecipe.getUnsetProperties();
        if (unsetProperties.size() > 0) {
            for (Object property : unsetProperties.keySet()) {
                logger.warning("Injection: No such property '" + property + "' in class " + beanClass.getName());
            }
        }
        instance = beanClass.cast(object);
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
        
        for (Injection injection : injections) {
            if (!injection.getTarget().isAssignableFrom(beanClass)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object object = context.lookup("java:comp/env/" + jndiName);
                if (object instanceof String) {
                    String string = (String) object;
                    // Pass it in raw so it could be potentially converted to
                    // another data type by an xbean-reflect property editor
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), string);
                } else {
                    objectRecipe.setProperty(injection.getTarget().getName() + "/" + injection.getName(), new StaticRecipe(object));
                }
            } catch (NamingException e) {
                logger.warning("Injection data not found in JNDI context: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget() + "/" + injection.getName());
            }
        }
    }
}

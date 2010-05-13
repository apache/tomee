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
package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;
import org.apache.xbean.finder.ClassFinder;

import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorData {

    private Class clazz;

    private final List<Method> aroundInvoke = new ArrayList<Method>();

    private final List<Method> postConstruct = new ArrayList<Method>();
    private final List<Method> preDestroy = new ArrayList<Method>();

    private final List<Method> postActivate = new ArrayList<Method>();
    private final List<Method> prePassivate = new ArrayList<Method>();

    public InterceptorData(Class clazz) {
        this.clazz = clazz;
    }

    public Class getInterceptorClass() {
        return clazz;
    }

    public List<Method> getAroundInvoke() {
        return aroundInvoke;
    }

    public List<Method> getPostConstruct() {
        return postConstruct;
    }

    public List<Method> getPreDestroy() {
        return preDestroy;
    }

    public List<Method> getPostActivate() {
        return postActivate;
    }

    public List<Method> getPrePassivate() {
        return prePassivate;
    }

    public List<Method> getMethods(Operation operation) {
        switch(operation) {
            case BUSINESS: return getAroundInvoke();
            case BUSINESS_WS: return getAroundInvoke();
            case REMOVE: return getAroundInvoke();
            case POST_CONSTRUCT: return getPostConstruct();
            case PRE_DESTROY: return getPreDestroy();
            case ACTIVATE: return getPostActivate();
            case PASSIVATE: return getPrePassivate();
            case AFTER_BEGIN: return getAroundInvoke();
            case AFTER_COMPLETION: return getAroundInvoke();
            case BEFORE_COMPLETION: return getAroundInvoke();
        }
        return Collections.EMPTY_LIST;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final InterceptorData that = (InterceptorData) o;

        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;

        return true;
    }

    public int hashCode() {
        return (clazz != null ? clazz.hashCode() : 0);
    }

    public static InterceptorData scan(Class<?> clazz) {
        ClassFinder finder = new ClassFinder(clazz);

        InterceptorData data = new InterceptorData(clazz);

        data.aroundInvoke.addAll(finder.findAnnotatedMethods(AroundInvoke.class));
        data.postConstruct.addAll(finder.findAnnotatedMethods(PostConstruct.class));
        data.preDestroy.addAll(finder.findAnnotatedMethods(PreDestroy.class));
        data.postActivate.addAll(finder.findAnnotatedMethods(PostActivate.class));
        data.prePassivate.addAll(finder.findAnnotatedMethods(PrePassivate.class));

        return data;
    }
}

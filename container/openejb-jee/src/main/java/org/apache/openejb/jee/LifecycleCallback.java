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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * The lifecycle-callback type specifies a method on a
 * class to be called when a lifecycle event occurs.
 * Note that each class may have only one lifecycle callback
 * method for any given event and that the method may not
 * be overloaded.
 * <p/>
 * If the lifefycle-callback-class element is missing then
 * the class defining the callback is assumed to be the
 * component class in scope at the place in the descriptor
 * in which the callback definition appears.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lifecycle-callbackType", propOrder = {
        "lifecycleCallbackClass",
        "lifecycleCallbackMethod"
        })
public class LifecycleCallback implements CallbackMethod {

    @XmlElement(name = "lifecycle-callback-class")
    protected String lifecycleCallbackClass;
    @XmlElement(name = "lifecycle-callback-method", required = true)
    protected String lifecycleCallbackMethod;

    public LifecycleCallback() {
    }

    public LifecycleCallback(java.lang.reflect.Method method) {
        this.lifecycleCallbackClass = method.getDeclaringClass().getName();
        this.lifecycleCallbackMethod = method.getName();
    }

    public LifecycleCallback(String lifecycleCallbackClass, String lifecycleCallbackMethod) {
        this.lifecycleCallbackClass = lifecycleCallbackClass;
        this.lifecycleCallbackMethod = lifecycleCallbackMethod;
    }

    public String getLifecycleCallbackClass() {
        return lifecycleCallbackClass;
    }

    public void setLifecycleCallbackClass(String value) {
        this.lifecycleCallbackClass = value;
    }

    public String getLifecycleCallbackMethod() {
        return lifecycleCallbackMethod;
    }

    public void setLifecycleCallbackMethod(String value) {
        this.lifecycleCallbackMethod = value;
    }

    public String getClassName() {
        return getLifecycleCallbackClass();
    }

    public String getMethodName() {
        return getLifecycleCallbackMethod();
    }
}

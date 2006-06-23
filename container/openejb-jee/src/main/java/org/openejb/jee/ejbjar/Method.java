/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Method {
    private String id;
    private List<String> description = new ArrayList<String>();
    private String ejbName;
    private MethodIntfType methodIntf;
    private String methodName;
    private MethodParams methodParams = new MethodParams();

    public Method() {
    }

    public Method(java.lang.reflect.Method method){
        this.methodName = method.getName();
        Class[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            addMethodParam(parameterType.getName());
        }
    }
    
    public Method(String ejbName, String methodName) {
        this.ejbName = ejbName;
        this.methodName = methodName;
    }

    public Method(String ejbName, String methodName, MethodIntfType methodIntf) {
        this.ejbName = ejbName;
        this.methodIntf = methodIntf;
        this.methodName = methodName;
    }

    public Method(String ejbName, MethodIntfType methodIntf) {
        this(ejbName, "*", methodIntf);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public MethodIntfType getMethodIntf() {
        return methodIntf;
    }

    public void setMethodIntf(MethodIntfType methodIntf) {
        this.methodIntf = methodIntf;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getMethodParams() {
        return methodParams.getMethodParams();
    }

    public void setMethodParams(List<String> methodParams) {
        this.methodParams.setMethodParams(methodParams);
    }

    public String getMethodParam(int index) {
        return getMethodParams().get(index);
    }

    public void addMethodParam(String param) {
        getMethodParams().add(param);
    }
}

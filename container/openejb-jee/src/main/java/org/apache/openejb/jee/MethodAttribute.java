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
package org.apache.openejb.jee;

/**
 * @version $Rev$ $Date$
 */
public class MethodAttribute<A> {

    private final A attribute;
    private final String ejbName;
    private final String methodName;
    private final MethodParams methodParams;
    private final String className;

    public MethodAttribute(A attribute, Method method) {
        this.attribute = attribute;
        this.ejbName = method.getEjbName();
        this.methodName = method.getMethodName();
        this.methodParams = method.getMethodParams();
        this.className = method.getClassName();
    }

    public MethodAttribute(A attribute, String ejbName, NamedMethod method) {
        this.attribute = attribute;
        this.ejbName = ejbName;
        this.methodName = method.getMethodName();
        this.methodParams = method.getMethodParams();
        this.className = method.getClassName();
    }

    public A getAttribute() {
        return attribute;
    }

    public String getClassName() {
        return className;
    }

    public String getEjbName() {
        return ejbName;
    }

    public String getMethodName() {
        return methodName;
    }

    public MethodParams getMethodParams() {
        return methodParams;
    }
}

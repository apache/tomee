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
package org.apache.openejb.assembler.spring;

/**
 * @org.apache.xbean.XBean element="transaction"
 */
public class MethodTransactionInfo {
    public String transAttribute;
    public MethodInfo[] methods;

    public MethodTransactionInfo() {
    }

    public MethodTransactionInfo(org.apache.openejb.assembler.classic.MethodTransactionInfo info){
        this.transAttribute = info.transAttribute;
        this.methods = new MethodInfo[info.methods.length];
        for (int i = 0; i < methods.length; i++) {
            methods[i] = new MethodInfo(info.methods[i]);
        }
    }
    public String getTransAttribute() {
        return transAttribute;
    }

    public void setTransAttribute(String transAttribute) {
        this.transAttribute = transAttribute;
    }

    /**
     * @org.apache.xbean.FlatCollection childElement="method"
     */
    public MethodInfo[] getMethods() {
        return methods;
    }

    public void setMethods(MethodInfo[] methods) {
        this.methods = methods;
    }
}

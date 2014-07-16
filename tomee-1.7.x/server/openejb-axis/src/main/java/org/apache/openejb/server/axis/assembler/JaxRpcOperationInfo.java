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
package org.apache.openejb.server.axis.assembler;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JaxRpcOperationInfo {
    public String name;
    public BindingStyle bindingStyle;
    public boolean wrapped;
    public OperationStyle operationStyle;

    public String javaMethodName;

    public List<JaxRpcParameterInfo> parameters = new ArrayList<JaxRpcParameterInfo>();

    public QName returnQName;
    public QName returnXmlType;
    public String returnJavaType;

    public Collection<JaxRpcFaultInfo> faults = new ArrayList<JaxRpcFaultInfo>();

    public static enum OperationStyle {
        ONE_WAY, REQUEST_RESPONSE, SOLICIT_RESPONSE, NOTIFICATION
    }
}

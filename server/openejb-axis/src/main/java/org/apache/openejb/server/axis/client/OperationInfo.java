/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import net.sf.cglib.core.Signature;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.soap.SOAPConstants;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class OperationInfo {
    private final OperationDesc operationDesc;
    private final boolean useSOAPAction;
    private final String soapActionURI;
    private final SOAPConstants soapVersion;
    private final QName operationName;
    private final String methodName;
    private final String methodDesc;

    public OperationInfo(OperationDesc operationDesc, boolean useSOAPAction, String soapActionURI, SOAPConstants soapVersion, QName operationName, String methodName, String methodDesc) {
        this.operationDesc = operationDesc;
        this.useSOAPAction = useSOAPAction;
        this.soapActionURI = soapActionURI;
        this.soapVersion = soapVersion;
        this.operationName = operationName;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public Signature getSignature() {
        return new Signature(methodName, methodDesc);
    }

    public OperationDesc getOperationDesc() {
        return operationDesc;
    }

    public boolean isUseSOAPAction() {
        return useSOAPAction;
    }

    public String getSoapActionURI() {
        return soapActionURI;
    }

    public SOAPConstants getSoapVersion() {
        return soapVersion;
    }

    public QName getOperationName() {
        return operationName;
    }

    public void prepareCall(Call call) {
        call.setOperation(operationDesc);
        call.setUseSOAPAction(useSOAPAction);
        call.setSOAPActionURI(soapActionURI);
        call.setSOAPVersion(soapVersion);
        call.setOperationName(operationName);
        //GAH!!!
        call.setOperationStyle(operationDesc.getStyle());
        call.setOperationUse(operationDesc.getUse());
    }

    public Throwable unwrapFault(RemoteException re) {
        if (re instanceof AxisFault && re.getCause() != null) {
            Throwable t = re.getCause();
            if (operationDesc.getFaultByClass(t.getClass()) != null) {
                return t;
            }
        }
        return re;
    }
}

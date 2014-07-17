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

import org.apache.axis.NoEndPointException;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;

import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import java.util.Iterator;

public class GenericServiceEndpointWrapper implements Stub {
    private final GenericServiceEndpoint genericServiceEndpoint;

    public GenericServiceEndpointWrapper(final GenericServiceEndpoint genericServiceEndpoint) {
        this.genericServiceEndpoint = genericServiceEndpoint;
    }

    public void _setProperty(final String s, final Object o) {
        genericServiceEndpoint._setProperty(s, o);
    }

    public Object _getProperty(final String s) {
        return genericServiceEndpoint._getProperty(s);
    }

    public Object removeProperty(final String s) {
        return genericServiceEndpoint.removeProperty(s);
    }

    public Iterator _getPropertyNames() {
        return genericServiceEndpoint._getPropertyNames();
    }

    public void setUsername(final String s) {
        genericServiceEndpoint.setUsername(s);
    }

    public String getUsername() {
        return genericServiceEndpoint.getUsername();
    }

    public void setPassword(final String s) {
        genericServiceEndpoint.setPassword(s);
    }

    public String getPassword() {
        return genericServiceEndpoint.getPassword();
    }

    public int getTimeout() {
        return genericServiceEndpoint.getTimeout();
    }

    public void setTimeout(final int i) {
        genericServiceEndpoint.setTimeout(i);
    }

    public QName getPortName() {
        return genericServiceEndpoint.getPortName();
    }

    public void setPortName(final QName qName) {
        genericServiceEndpoint.setPortName(qName);
    }

    public void setPortName(final String s) {
        genericServiceEndpoint.setPortName(s);
    }

    public void setMaintainSession(final boolean b) {
        genericServiceEndpoint.setMaintainSession(b);
    }

    public void setHeader(final String s, final String s1, final Object o) {
        genericServiceEndpoint.setHeader(s, s1, o);
    }

    public void setHeader(final SOAPHeaderElement soapHeaderElement) {
        genericServiceEndpoint.setHeader(soapHeaderElement);
    }

    public void extractAttachments(final Call call) {
        genericServiceEndpoint.extractAttachments(call);
    }

    public void addAttachment(final Object o) {
        genericServiceEndpoint.addAttachment(o);
    }

    public SOAPHeaderElement getHeader(final String s, final String s1) {
        return genericServiceEndpoint.getHeader(s, s1);
    }

    public SOAPHeaderElement getResponseHeader(final String s, final String s1) {
        return genericServiceEndpoint.getResponseHeader(s, s1);
    }

    public SOAPHeaderElement[] getHeaders() {
        return genericServiceEndpoint.getHeaders();
    }

    public SOAPHeaderElement[] getResponseHeaders() {
        return genericServiceEndpoint.getResponseHeaders();
    }

    public Object[] getAttachments() {
        return genericServiceEndpoint.getAttachments();
    }

    public void clearHeaders() {
        genericServiceEndpoint.clearHeaders();
    }

    public void clearAttachments() {
        genericServiceEndpoint.clearAttachments();
    }

    void checkCachedEndpoint() throws NoEndPointException {
        genericServiceEndpoint.checkCachedEndpoint();
    }
}

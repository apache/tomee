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
package org.apache.openejb.server.cxf;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.ws.security.WSConstants;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * When using JAX-WS Handler, the {@link org.apache.openejb.server.cxf.ejb.EjbInterceptor}
 * adds the {@link org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor}. OpenEJB now supports
 * WS Security out of the box, so it must indicates WS Security headers have been treated. That is simply done
 * using that fake interceptor.
 * <p/>
 * $Id$
 */
public class WSSPassThroughInterceptor extends AbstractSoapInterceptor {
    private static final Set<QName> HEADERS = new HashSet<QName>();

    static {
        HEADERS.add(new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN));
        HEADERS.add(new QName(WSConstants.WSSE11_NS, WSConstants.WSSE_LN));
        HEADERS.add(new QName(WSConstants.ENC_NS, WSConstants.ENC_DATA_LN));
    }

    public WSSPassThroughInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    public WSSPassThroughInterceptor(String phase) {
        super(phase);
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        return HEADERS;
    }

    public void handleMessage(SoapMessage soapMessage) {
        // do nothing

        // this interceptor simply returns all WS-Security headers in its getUnderstoodHeaders()
        // method, so that CXF does not complain that they have not been "processed"
        // this is useful if you only need to look at the non-encrypted XML
    }

}

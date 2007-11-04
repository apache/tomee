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
package org.apache.openejb.server.webservices;

public interface WsConstants {
    /**
     * Used when this WebServiceContainer is servicing a POJO, in which case
     * the pojo instance is held by the enclosing servlet/invoker and passed in
     * the Request instance to the container.
     */
    public static final String POJO_INSTANCE = WsConstants.class.getName() + "@pojoInstance";

    /**
     * Used when this WebServiceContainer is servicing a POJO implementing the
     * ServiceLifecycle interface, in which case the WebServiceContainer is expected
     * to put the JAX-RPC MessageContext it creates in the Request instance.
     */
    public static final String MESSAGE_CONTEXT = WsConstants.class.getName() + "@MessageContext";

    /**
     * Used for JAX-WS MessageContext. MessageContext must expose HttpServletRequest.
     */
    public static final String SERVLET_REQUEST = WsConstants.class.getName() + "@ServletRequest";

    /**
     * Used for JAX-WS MessageContext. MessageContext must expose HttpServletResponse.
     */
    public static final String SERVLET_RESPONSE = WsConstants.class.getName() + "@ServletResponse";

    /**
     * Used for JAX-WS MessageContext. MessageContext must expose ServletContext.
     */
    public static final String SERVLET_CONTEXT = WsConstants.class.getName() + "@ServletContext";

    /**
     * Token inserted into wsdl where location should be replaced with the real location
     */
    public static final String LOCATION_REPLACEMENT_TOKEN = "LOCATIONREPLACEMENTTOKEN";
}

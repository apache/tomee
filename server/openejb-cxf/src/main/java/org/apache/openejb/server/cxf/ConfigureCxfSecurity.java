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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;

/**
 * Helper class to extract WSS4J properties from a set of properties. More over,
 * it configures In and Out interceptor to manage WS6Security.
 *
 */
public class ConfigureCxfSecurity {
    
    public static final void setupWSS4JChain(Endpoint endpoint, Properties inProps) {

	Map<String, Object> in = getPropsFromProperties(inProps, "wss4j.in.");
	Map<String, Object> out = getPropsFromProperties(inProps, "wss4j.out.");
	setupWSS4JChain(endpoint, in, out);
    }

    public static Map<String, Object> getPropsFromProperties(Properties inProps, String pattern) {
	String key, val;

	Map<String, Object> props = new HashMap<String, Object>();
	for (Map.Entry<Object, Object> entry : inProps.entrySet()) {
	    key = String.valueOf(entry.getKey());
	    val = String.valueOf(entry.getValue()).trim();
	    if (key.startsWith(pattern)) {
		props.put(key.substring(pattern.length()), val);
	    }
	}
	if (!props.isEmpty()) {
	    // WSHandler first look for a property PW_CALLBACK_CLASS
	    // if not found, it gets the PW_CALLBACK_REF
	    props.put(WSHandlerConstants.PW_CALLBACK_REF, new ServerPasswordHandler());
	}
	return props;
    }

    public static final void setupWSS4JChain(Endpoint endpoint, Map<String, Object> inProps, Map<String, Object> outProps) {

	if (null != inProps && !inProps.isEmpty()) {
	    endpoint.getInInterceptors().add(new SAAJInInterceptor());
	    endpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
	}

	if (null != outProps && !outProps.isEmpty()) {
	    endpoint.getOutInterceptors().add(new SAAJOutInterceptor());
	    endpoint.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));
	}

    }

    public static final void configure(Endpoint endpoint, Properties p) {
	setupWSS4JChain(endpoint, p);
    }

}

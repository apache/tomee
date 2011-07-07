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

import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Helper class to extract WSS4J properties from a set of properties. More over,
 * it configures In and Out interceptor to manage WS-Security.
 */
public class ConfigureCxfSecurity {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.CXF, ConfigureCxfSecurity.class);
    private static final Map<QName, Object> DEFAULT_VALIDATOR_MAP = new HashMap<QName, Object>() {{
        put(new QName(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "UsernameToken"),
            new OpenEJBLoginValidator());
    }};

    public static final void setupWSS4JChain(Endpoint endpoint, Properties inProps) {

        final Map<String, Object> in = getPropsFromProperties(inProps, "wss4j.in.");
        final Map<String, Object> out = getPropsFromProperties(inProps, "wss4j.out.");
        if (!in.containsKey(WSS4JInInterceptor.VALIDATOR_MAP)) {
            // default case, if user doesn't want it he should add: wss4j.in.validator.{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd}UsernameToken = DummyImpl
            in.put(WSS4JInInterceptor.VALIDATOR_MAP, DEFAULT_VALIDATOR_MAP);
        }
        setupWSS4JChain(endpoint, in, out);
    }

    public static Map<String, Object> getPropsFromProperties(Properties inProps, String pattern) {
        final String validatorPrefix = pattern + "validator";
        final String processorPrefix = pattern + "processor";
        final Map<QName, Object> validatorMap = new HashMap<QName, Object>();
        final Map<QName, Object> processorMap = new HashMap<QName, Object>();
        final Map<String, Object> props = new HashMap<String, Object>();

        String key, val;
        for (Map.Entry<Object, Object> entry : inProps.entrySet()) {
            key = String.valueOf(entry.getKey()).trim();
            val = String.valueOf(entry.getValue()).trim();
            if (key.startsWith(validatorPrefix)) {
                SplitInfo infos = new SplitInfo(key, val);
                try {
                    validatorMap.put(infos.qname, getValidator(infos.value));
                } catch (Exception e) {
                    LOGGER.warning("validator not found " + val, e);
                }
            } if (key.startsWith(processorPrefix)) {
                SplitInfo infos = new SplitInfo(key, val);
                processorMap.put(infos.qname, infos.value);
            } else if (key.startsWith(pattern)) {
                props.put(key.substring(pattern.length()), val);
            }
        }

        if (!validatorMap.isEmpty()) {
            props.put(WSS4JInInterceptor.VALIDATOR_MAP, validatorMap);
        }
        if (!processorMap.isEmpty()) {
            props.put(WSS4JInInterceptor.PROCESSOR_MAP, processorMap);
        }
        return props;
    }

    private static Object getValidator(String validator) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ConfigureCxfSecurity.class.getClassLoader();
        }
        return cl.loadClass(validator).newInstance();
    }

    public static final void setupWSS4JChain(Endpoint endpoint, Map<String, Object> inProps, Map<String, Object> outProps) {

        if (null != inProps && !inProps.isEmpty()) {
            endpoint.getInInterceptors().add(new SAAJInInterceptor());
            endpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));

            // if WS Security is used with a JAX-WS handler (See EjbInterceptor), we have to deal with mustUnderstand flag
            // in WS Security headers. So, let's add an interceptor
            endpoint.getInInterceptors().add(new WSSPassThroughInterceptor());
        }

        if (null != outProps && !outProps.isEmpty()) {
            endpoint.getOutInterceptors().add(new SAAJOutInterceptor());
            endpoint.getOutInterceptors().add(new WSS4JOutInterceptor(outProps));
        }

    }

    public static final void configure(Endpoint endpoint, Properties p) {
        setupWSS4JChain(endpoint, p);
    }

    /**
     * split {<namespace>}<local> = foo
     * useful because in the namespace there is at least a '.' which is a separator for Proeprties
     * and escaping doesn't always work.
     */
    private static class SplitInfo {
        public QName qname;
        public String value;

        public SplitInfo(final String key, final String val) {
            String k = key;
            int startIdx = k.indexOf('{');
            if (startIdx > 0) {
                k = k.substring(startIdx);
            }

            value = val;

            int idx = value.indexOf("=");
            if (idx > 0) {
                k = k + ':' + value.substring(0, idx);
                value = value.substring(idx + 1);
            }
            k = k.trim();
            value = value.trim();

            final int start = k.indexOf('{');
            final int end = k.indexOf('}');
            final String ns = k.substring(start + 1, end);
            final String local = k.substring(end + 1);

            qname = new QName(ns, local);
        }
    }
}

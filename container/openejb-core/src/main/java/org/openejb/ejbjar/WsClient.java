/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.openejb.ejbjar;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class WsClient {
    public static class PortComponentRef {
        String id;
        String serviceEndpointInterface;
        boolean enableMtom;
        String portComponentLink;
    }

    public static class ServiceRef extends Javaee.JndiEnvironmentRef {
        List<String> displayName;
        List<Javaee.Icon> icons;
        String serviceRefName;
        String serviceInterface;
        String serviceRefType;
        String wsdlFile;
        String jaxrpcMappingFile;
        String serviceQname;
        String portComponentRef;
        List<Handler> handlers;
        List<HandlerChain> handlerChains;
    }

    public static class HandlerChain {
        String id;
        Target target;
        List<Handler> handlers;
    }

    public static class Handler {
        String id;
        List<String> description;
        List<String> displayName;
        List<Javaee.Icon> icons;
        String handlerClass;
        List<InitParam> initParams;
        List<String> soapHeaders;
        List<String> soapRoles;
        List<String> portNames;
    }

    public static class InitParam extends Javaee.ParamValue {
    }

    public abstract static class Target {
        String id;
    }

    public static class QNamePattern extends Target {
        String pattern;
    }

    public static class ServiceNamePattern extends QNamePattern {
    }

    public static class PortNamePattern extends QNamePattern {
    }

    public static class ProtocolBindings extends Target {
        List<String> bindings;
    }
}

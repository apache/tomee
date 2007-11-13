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
package org.apache.openejb.core.webservices;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceProvider;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class JaxWsUtils {

    private static final Map<String, String> BINDING_MAP = new HashMap<String, String>();

    static {
        BINDING_MAP.put("##SOAP11_HTTP", "http://schemas.xmlsoap.org/wsdl/soap/http");
        BINDING_MAP.put("##SOAP12_HTTP", "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        BINDING_MAP.put("##SOAP11_HTTP_MTOM", "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true");
        BINDING_MAP.put("##SOAP12_HTTP_MTOM", "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true");
        BINDING_MAP.put("##XML_HTTP", "http://www.w3.org/2004/08/wsdl/http");
    }

    private JaxWsUtils() {
    }

    public static QName getPortType(Class<?> seiClass) {
        WebService webService = seiClass.getAnnotation(WebService.class);
        if (webService != null) {
            String localName = webService.name();
            if (localName == null || localName.length() == 0) {
                localName = seiClass.getName();
            }
            String namespace = webService.targetNamespace();
            return new QName(getNamespace(seiClass, namespace), localName);
        }
        return null;
    }

    public static String getBindingURI(String token) {
        if (token != null) {
            if (token.startsWith("##")) {
                String uri = BINDING_MAP.get(token);
                if (uri == null) {
                    throw new IllegalArgumentException("Unsupported binding token: " + token);
                }
                return uri;
            }
            return token;
        }
        return BINDING_MAP.get("##SOAP11_HTTP");
    }

    public static boolean isWebService(Class clazz) {
        return ((clazz.isAnnotationPresent(WebService.class) || clazz.isAnnotationPresent(WebServiceProvider.class)) && isProperWebService(clazz));
    }

    private static boolean isProperWebService(Class clazz) {
        int modifiers = clazz.getModifiers();
        return (Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isAbstract(modifiers));
    }

    public static String getServiceName(Class clazz) {
        return getServiceQName(clazz).getLocalPart();
    }

    private static String getServiceName(Class clazz, String name) {
        if (name == null || name.trim().length() == 0) {
            return clazz.getSimpleName() + "Service";
        } else {
            return name.trim();
        }
    }

    private static String getPortName(Class clazz, String name, String portName) {
        if (portName == null || portName.trim().length() == 0) {
            if (name == null || name.trim().length() == 0) {
                return clazz.getSimpleName() + "Port";
            } else {
                return name + "Port";
            }
        } else {
            return portName.trim();
        }
    }

    private static String getNamespace(Class clazz, String namespace) {
        if (namespace == null || namespace.trim().length() == 0) {
            Package pkg = clazz.getPackage();
            if (pkg == null) {
                return null;
            } else {
                return getNamespace(pkg.getName());
            }
        } else {
            return namespace.trim();
        }
    }

    private static String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i = tokenizer.countTokens() - 1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuffer namespace = new StringBuffer("http://");
        String dot = "";
        for (int i = 0; i < tokens.length; i++) {
            if (i == 1) {
                dot = ".";
            }
            namespace.append(dot).append(tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

    private static QName getServiceQName(Class clazz, String namespace, String name) {
        return new QName(getNamespace(clazz, namespace), getServiceName(clazz, name));
    }

    public static QName getServiceQName(Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            return getServiceQName(clazz, webService.targetNamespace(), webService.serviceName());
        }
        WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return getServiceQName(clazz, webServiceProvider.targetNamespace(), webServiceProvider.serviceName());
        }
        WebServiceClient webServiceClient = clazz.getAnnotation(WebServiceClient.class);
        if (webServiceClient != null) {
            return getServiceQName(clazz, webServiceClient.targetNamespace(), webServiceClient.name());
        }
        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    private static QName getPortQName(Class<?> clazz, String namespace, String name, String portName) {
        return new QName(getNamespace(clazz, namespace), getPortName(clazz, name, portName));
    }

    public static QName getPortQName(Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            return getPortQName(clazz, webService.targetNamespace(), webService.name(), webService.portName());
        }

        WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return getPortQName(clazz, webServiceProvider.targetNamespace(), null, webServiceProvider.portName());
        }

        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    public static String getName(Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            String sei = webService.endpointInterface();
            if (sei != null && sei.trim().length() != 0) {
                try {
                    Class seiClass = clazz.getClassLoader().loadClass(sei.trim());
                    return getNameFromInterface(seiClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to load SEI class: " + sei, e);
                }
            }
            return getName(clazz, webService.name());
        }

        WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return clazz.getName();
        }

        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    private static String getNameFromInterface(Class<?> intf) {
        WebService webService = intf.getAnnotation(WebService.class);
        if (webService != null) {
            return getName(intf, webService.name());
        }
        throw new IllegalArgumentException("The " + intf.getName() + " is not annotated");
    }

    private static String getName(Class clazz, String name) {
        if (name != null) {
            name = name.trim();
            if (name.length() > 0) {
                return name;
            }
        }
        return clazz.getSimpleName();
    }

    private static String getWsdlLocation(Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            String wsdlLocation = webService.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) wsdlLocation = null;
            return wsdlLocation;
        }

        WebServiceClient webServiceClient = clazz.getAnnotation(WebServiceClient.class);
        if (webServiceClient != null) {
            String wsdlLocation = webServiceClient.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) wsdlLocation = null;
            return wsdlLocation;
        }

        WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            String wsdlLocation = webServiceProvider.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) wsdlLocation = null;
            return wsdlLocation;
        }

        return null;
    }

    public static String getServiceInterface(Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null && webService.endpointInterface() != null) {
            String endpointInterface = webService.endpointInterface().trim();
            if (endpointInterface.length() == 0) endpointInterface = null;
            return endpointInterface;
        }

        // if the bean implements only one WebService class, that is the SEI
        String endpointInterface = null;
        for (Class intf : clazz.getInterfaces()) {
            webService = clazz.getAnnotation(WebService.class);
            if (webService != null) {
                if (endpointInterface == null) {
                    endpointInterface = intf.getName();
                } else {
                    // multiple endpoint interfaces
                    endpointInterface = null;
                    break;
                }
            }
        }

        if (endpointInterface != null) {
            return endpointInterface;
        }

        return null;
    }

    public static String getServiceWsdlLocation(Class<?> clazz, ClassLoader loader) {
        String wsdlLocation = getWsdlLocation(clazz);
        if (wsdlLocation != null && !wsdlLocation.equals("")) {
            return wsdlLocation;
        }

        String serviceInterfaceClassName = getServiceInterface(clazz);
        if (serviceInterfaceClassName != null && !serviceInterfaceClassName.equals("")) {
            try {
                Class serviceInterfaceClass = loader.loadClass(serviceInterfaceClassName);
                return getWsdlLocation(serviceInterfaceClass);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean containsWsdlLocation(Class<?> clazz, ClassLoader loader) {
        String wsdlLocSEIFromAnnotation = getServiceWsdlLocation(clazz, loader);
        return wsdlLocSEIFromAnnotation != null && !wsdlLocSEIFromAnnotation.equals("");
    }

    public static String getBindingUriFromAnn(Class<?> clazz) {
        BindingType bindingType = clazz.getAnnotation(BindingType.class);
        if (bindingType != null) {
            return bindingType.value();
        }
        return null;
    }
}

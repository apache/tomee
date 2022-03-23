/*
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

package org.apache.openejb.core.webservices;

import org.apache.openejb.OpenEJBRuntimeException;

import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceProvider;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class JaxWsUtils {

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

    public static QName getPortType(final Class<?> seiClass) {
        final WebService webService = seiClass.getAnnotation(WebService.class);
        if (webService != null) {
            String localName = webService.name();
            if (localName == null || localName.length() == 0) {
                localName = seiClass.getSimpleName();
            }
            final String namespace = webService.targetNamespace();
            return new QName(getNamespace(seiClass, namespace), localName);
        }
        return null;
    }

    public static String getBindingURI(final String token) {
        if (token != null) {
            if (token.startsWith("##")) {
                final String uri = BINDING_MAP.get(token);
                if (uri == null) {
                    throw new IllegalArgumentException("Unsupported binding token: " + token);
                }
                return uri;
            }
            return token;
        }
        return BINDING_MAP.get("##SOAP11_HTTP");
    }

    public static boolean isWebService(final Class clazz) {
        return (clazz.isAnnotationPresent(WebService.class) || clazz.isAnnotationPresent(WebServiceProvider.class)) && isProperWebService(clazz);
    }

    private static boolean isProperWebService(final Class clazz) {
        final int modifiers = clazz.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isAbstract(modifiers);
    }

    public static String getServiceName(final Class clazz) {
        return getServiceQName(clazz).getLocalPart();
    }

    private static String getServiceName(final Class clazz, final String name) {
        if (name == null || name.trim().length() == 0) {
            return clazz.getSimpleName() + "Service";
        } else {
            return name.trim();
        }
    }

    private static String getPortName(final Class clazz, final String name, final String portName) {
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

    private static String getNamespace(final Class clazz, final String namespace) {
        if (namespace == null || namespace.trim().length() == 0) {
            final Package pkg = clazz.getPackage();
            if (pkg == null) {
                return null;
            } else {
                return getNamespace(pkg.getName());
            }
        } else {
            return namespace.trim();
        }
    }

    private static String getNamespace(final String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        final StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        final String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i = tokenizer.countTokens() - 1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        final StringBuilder namespace = new StringBuilder("http://");
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

    private static QName getServiceQName(final Class clazz, final String namespace, final String name) {
        return new QName(getNamespace(clazz, namespace), getServiceName(clazz, name));
    }

    public static QName getServiceQName(final Class<?> clazz) {
        final WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            return getServiceQName(clazz, webService.targetNamespace(), webService.serviceName());
        }
        final WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return getServiceQName(clazz, webServiceProvider.targetNamespace(), webServiceProvider.serviceName());
        }
        final WebServiceClient webServiceClient = clazz.getAnnotation(WebServiceClient.class);
        if (webServiceClient != null) {
            return getServiceQName(clazz, webServiceClient.targetNamespace(), webServiceClient.name());
        }
        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    private static QName getPortQName(final Class<?> clazz, final String namespace, final String name, final String portName) {
        return new QName(getNamespace(clazz, namespace), getPortName(clazz, name, portName));
    }

    public static QName getPortQName(final Class<?> clazz) {
        final WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            return getPortQName(clazz, webService.targetNamespace(), webService.name(), webService.portName());
        }

        final WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return getPortQName(clazz, webServiceProvider.targetNamespace(), null, webServiceProvider.portName());
        }

        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    public static String getName(final Class<?> clazz) {
        final WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            final String sei = webService.endpointInterface();
            if (sei != null && sei.trim().length() != 0) {
                try {
                    final Class seiClass = clazz.getClassLoader().loadClass(sei.trim());
                    return getNameFromInterface(seiClass);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBRuntimeException("Unable to load SEI class: " + sei, e);
                }
            }
            return getName(clazz, webService.name());
        }

        final WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            return clazz.getName();
        }

        throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
    }

    private static String getNameFromInterface(final Class<?> intf) {
        final WebService webService = intf.getAnnotation(WebService.class);
        if (webService != null) {
            return getName(intf, webService.name());
        }
        throw new IllegalArgumentException("The " + intf.getName() + " is not annotated");
    }

    private static String getName(final Class clazz, String name) {
        if (name != null) {
            name = name.trim();
            if (name.length() > 0) {
                return name;
            }
        }
        return clazz.getSimpleName();
    }

    private static String getWsdlLocation(final Class<?> clazz) {
        final WebService webService = clazz.getAnnotation(WebService.class);
        if (webService != null) {
            String wsdlLocation = webService.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) {
                wsdlLocation = null;
            }
            return wsdlLocation;
        }

        final WebServiceClient webServiceClient = clazz.getAnnotation(WebServiceClient.class);
        if (webServiceClient != null) {
            String wsdlLocation = webServiceClient.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) {
                wsdlLocation = null;
            }
            return wsdlLocation;
        }

        final WebServiceProvider webServiceProvider = clazz.getAnnotation(WebServiceProvider.class);
        if (webServiceProvider != null) {
            String wsdlLocation = webServiceProvider.wsdlLocation().trim();
            if (wsdlLocation.length() == 0) {
                wsdlLocation = null;
            }
            return wsdlLocation;
        }

        return null;
    }

    public static String getServiceInterface(final Class<?> clazz) {
        WebService webService = clazz.getAnnotation(WebService.class);
        String endpointInterface = null;
        if (webService != null && webService.endpointInterface() != null) {
            endpointInterface = webService.endpointInterface().trim();
            if (endpointInterface.length() == 0) {
                endpointInterface = null;

            } else {
                return endpointInterface;
            }
        }

        // if the bean implements only one WebService class, that is the SEI
        for (final Class<?> intf : clazz.getInterfaces()) {
            // interface MUST also have a @WebService
            webService = intf.getAnnotation(WebService.class);
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

        return endpointInterface;
    }

    public static String getServiceWsdlLocation(final Class<?> clazz, final ClassLoader loader) {
        final String wsdlLocation = getWsdlLocation(clazz);
        if (wsdlLocation != null && !wsdlLocation.isEmpty()) {
            return wsdlLocation;
        }

        final String serviceInterfaceClassName = getServiceInterface(clazz);
        if (serviceInterfaceClassName != null && !serviceInterfaceClassName.isEmpty()) {
            try {
                final Class serviceInterfaceClass = loader.loadClass(serviceInterfaceClassName);
                return getWsdlLocation(serviceInterfaceClass);
            } catch (final Exception e) {
                // no-op
            }
        }
        return null;
    }

    public static boolean containsWsdlLocation(final Class<?> clazz, final ClassLoader loader) {
        final String wsdlLocSEIFromAnnotation = getServiceWsdlLocation(clazz, loader);
        return wsdlLocSEIFromAnnotation != null && !wsdlLocSEIFromAnnotation.isEmpty();
    }

    public static String getBindingUriFromAnn(final Class<?> clazz) {
        final BindingType bindingType = clazz.getAnnotation(BindingType.class);
        if (bindingType != null) {
            return bindingType.value();
        }
        return null;
    }
}

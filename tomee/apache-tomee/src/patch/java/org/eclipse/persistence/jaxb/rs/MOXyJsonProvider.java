/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Blaise Doughan - 2.4 - initial implementation
package org.eclipse.persistence.jaxb.rs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.activation.DataSource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.internal.core.helper.CoreClassConstants;
import org.eclipse.persistence.internal.helper.Helper;
import org.eclipse.persistence.internal.oxm.Constants;
import org.eclipse.persistence.internal.queries.CollectionContainerPolicy;
import org.eclipse.persistence.internal.queries.ContainerPolicy;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.oxm.JSONWithPadding;

/**
 * <p>This is an implementation of <i>MessageBodyReader</i>/<i>MessageBodyWriter
 * </i> that can be used to enable EclipseLink JAXB (MOXy) as the JSON
 * provider.</p>
 * <p>
 * <b>Supported Media Type Patterns</b>
 * <ul>
 * <li>*&#47;json (i.e. application/json and text/json)</li>
 * <li>*&#47;*+json</li>
 * </ul>
 *
 * <p>Below are some different usage options.</p>
 *
 * <b>Option #1 - <i>MOXyJsonProvider</i> Default Behavior</b>
 * <p>You can use the <i>Application</i> class to specify that
 * <i>MOXyJsonProvider</i> should be used with your JAX-RS application.</p>
 * <pre>
 * package org.example;

 * import java.util.*;
 * import javax.ws.rs.core.Application;
 * import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
 *
 * public class ExampleApplication  extends Application {
 *
 *     &#64;Override
 *     public Set&lt;Class&lt;?&gt;&gt; getClasses() {
 *         HashSet&lt;Class&lt;?&gt;&gt; set = new HashSet&lt;Class&lt;?&gt;&gt;(2);
 *         set.add(MOXyJsonProvider.class);
 *         set.add(ExampleService.class);
 *         return set;
 *     }
 *
 * }
 * </pre>
 *
 * <b>Option #2 - Customize <i>MOXyJsonProvider</i></b>
 * <p>You can use the <i>Application</i> class to specify a configured instance
 * of <i>MOXyJsonProvider</i> should be used with your JAX-RS application.</p>
 * <pre>
 * package org.example;
 *
 * import java.util.*;
 * import javax.ws.rs.core.Application;
 * import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
 *
 * public class CustomerApplication  extends Application {
 *
 *     &#64;Override
 *     public Set&lt;Class&lt;?&gt;&gt; getClasses() {
 *         HashSet&lt;Class&lt;?&gt;&gt; set = new HashSet&lt;Class&lt;?&gt;&gt;(1);
 *         set.add(ExampleService.class);
 *         return set;
 *     }

 *     &#64;Override
 *     public Set&lt;Object&gt; getSingletons() {
 *         moxyJsonProvider moxyJsonProvider = new MOXyJsonProvider();
 *         moxyJsonProvider.setFormattedOutput(true);
 *         moxyJsonProvider.setIncludeRoot(true);
 *
 *         HashSet&lt;Object&gt; set = new HashSet&lt;Object&gt;(2);
 *         set.add(moxyJsonProvider);
 *         return set;
 *     }
 *
 * }
 * </pre>
 * <b>Option #3 - Extend MOXyJsonProvider</b>
 * <p>You can use MOXyJsonProvider for creating your own
 * <i>MessageBodyReader</i>/<i>MessageBodyWriter</i>.</p>
 * <pre>
 * package org.example;
 *
 * import java.lang.annotation.Annotation;
 * import java.lang.reflect.Type;
 *
 * import javax.ws.rs.*;
 * import javax.ws.rs.core.*;
 * import javax.ws.rs.ext.Provider;
 * import javax.xml.bind.*;
 *
 * import org.eclipse.persistence.jaxb.MarshallerProperties;
 * import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
 *
 * &#64;Provider
 * &#64;Produces(MediaType.APPLICATION_JSON)
 * &#64;Consumes(MediaType.APPLICATION_JSON)
 * public class CustomerJSONProvider extends MOXyJsonProvider {

 *     &#64;Override
 *     public boolean isReadable(Class&lt;?&gt; type, Type genericType,
 *             Annotation[] annotations, MediaType mediaType) {
 *         return getDomainClass(genericType) == Customer.class;
 *     }
 *
 *     &#64;Override
 *     public boolean isWriteable(Class&lt;?&gt; type, Type genericType,
 *             Annotation[] annotations, MediaType mediaType) {
 *         return isReadable(type, genericType, annotations, mediaType);
 *     }
 *
 *     &#64;Override
 *     protected void preReadFrom(Class&lt;Object&gt; type, Type genericType,
 *             Annotation[] annotations, MediaType mediaType,
 *             MultivaluedMap&lt;String, String&gt; httpHeaders,
 *             Unmarshaller unmarshaller) throws JAXBException {
 *         unmarshaller.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, "$");
 *     }
 *
 *     &#64;Override
 *     protected void preWriteTo(Object object, Class&lt;?&gt; type, Type genericType,
 *             Annotation[] annotations, MediaType mediaType,
 *             MultivaluedMap&lt;String, Object&gt; httpHeaders, Marshaller marshaller)
 *             throws JAXBException {
 *         marshaller.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, "$");
 *     }
 *
 * }
 * </pre>
 * @since 2.4
 */
@Produces({MediaType.APPLICATION_JSON, MediaType.WILDCARD, "application/x-javascript"})
@Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
public class MOXyJsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>{

    private static final String APPLICATION_XJAVASCRIPT = "application/x-javascript";
    private static final String CHARSET = "charset";
    private static final QName EMPTY_STRING_QNAME = new QName("");
    private static final String JSON = "json";
    private static final String PLUS_JSON = "+json";

    @Context
    protected Providers providers;

    private String attributePrefix = null;
    private Map<Set<Class<?>>, JAXBContext> contextCache = new HashMap<Set<Class<?>>, JAXBContext>();
    private boolean formattedOutput = false;
    private boolean includeRoot = false;
    private boolean marshalEmptyCollections = true;
    private Map<String, String> namespacePrefixMapper;
    private char namespaceSeperator = Constants.DOT;
    private String valueWrapper;
    private boolean wrapperAsArrayName = false;

    /**
     * The value that will be prepended to all keys that are mapped to an XML
     * attribute.  By default there is no attribute prefix.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_ATTRIBUTE_PREFIX
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_ATTRIBUTE_PREFIX
     */
    public String getAttributePrefix() {
        return attributePrefix;
    }

    /**
     * A convenience method to get the domain class (i.e. <i>Customer</i> or <i>Foo, Bar</i>) from
     * the parameter/return type (i.e. <i>Customer</i>, <i>List&lt;Customer&gt;</i>,
     * <i>JAXBElement&lt;Customer&gt;</i>, <i>JAXBElement&lt;? extends Customer&gt;</i>,
     * <i>List&lt;JAXBElement&lt;Customer&gt;&gt;</i>, or
     * <i>List&lt;JAXBElement&lt;? extends Customer&gt;&gt;</i>
     * <i>List&lt;Foo&lt;Bar&gt;&gt;</i>).
     * @param genericType - The parameter/return type of the JAX-RS operation.
     * @return The corresponding domain classes.
     */
    protected Set<Class<?>> getDomainClasses(Type genericType) {
        if(null == genericType) {
            return asSet(Object.class);
        }
        if(genericType instanceof Class && genericType != JAXBElement.class) {
            Class<?> clazz = (Class<?>) genericType;
            if(clazz.isArray()) {
                return getDomainClasses(clazz.getComponentType());
            }
            return asSet(clazz);
        } else if(genericType instanceof ParameterizedType) {
            Set<Class<?>> result = new LinkedHashSet<Class<?>>();
            result.add((Class<?>)((ParameterizedType) genericType).getRawType());
            Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
            if(types.length > 0){
                for (Type upperType : types) {
                    result.addAll(getDomainClasses(upperType));
                }
            }
            return result;
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return getDomainClasses(genericArrayType.getGenericComponentType());
        } else if(genericType instanceof WildcardType) {
            Set<Class<?>> result = new LinkedHashSet<Class<?>>();
            Type[] upperTypes = ((WildcardType)genericType).getUpperBounds();
            if(upperTypes.length > 0){
                for (Type upperType : upperTypes) {
                    result.addAll(getDomainClasses(upperType));
                }
            } else {
                result.add(Object.class);
            }
            return result;
        } else {
            return asSet(Object.class);
        }
    }

    private Set<Class<?>> asSet(Class<?> clazz) {
        Set<Class<?>> result = new LinkedHashSet<>();
        result.add(clazz);
        return result;
    }

    /**
     * Return the <i>JAXBContext</i> that corresponds to the domain class.  This
     * method does the following:
     * <ol>
     * <li>If an EclipseLink JAXB (MOXy) <i>JAXBContext</i> is available from
     * a <i>ContextResolver</i> then use it.</li>
     * <li>If an existing <i>JAXBContext</i> was not found in step one, then
     * create a new one on the domain class.</li>
     * </ol>
     * @param domainClasses - The domain classes we need a <i>JAXBContext</i> for.
     * @param annotations - The annotations corresponding to domain object.
     * @param mediaType - The media type for the HTTP entity.
     * @param httpHeaders - HTTP headers associated with HTTP entity.
     * @return
     * @throws JAXBException
     */
    protected JAXBContext getJAXBContext(Set<Class<?>> domainClasses, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, ?> httpHeaders) throws JAXBException {

        JAXBContext jaxbContext = contextCache.get(domainClasses);
        if(null != jaxbContext) {
            return jaxbContext;
        }

        synchronized (contextCache) {
            jaxbContext = contextCache.get(domainClasses);
            if(null != jaxbContext) {
                return jaxbContext;
            }

            ContextResolver<JAXBContext> resolver = null;
            if(null != providers) {
                resolver = providers.getContextResolver(JAXBContext.class, mediaType);
            }

            if (null != resolver && domainClasses.size() == 1) {
                jaxbContext = resolver.getContext(domainClasses.iterator().next());
            }

            if(null == jaxbContext) {
                jaxbContext = JAXBContextFactory.createContext(domainClasses.toArray(new Class[0]), null);
                contextCache.put(domainClasses, jaxbContext);
                return jaxbContext;
            } else if (jaxbContext instanceof org.eclipse.persistence.jaxb.JAXBContext) {
                return jaxbContext;
            } else {
                jaxbContext = JAXBContextFactory.createContext(domainClasses.toArray(new Class[0]), null);
                contextCache.put(domainClasses, jaxbContext);
                return jaxbContext;
            }
        }
    }

    private JAXBContext getJAXBContext(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if(null == genericType) {
            genericType = type;
        }

        try {
            Set<Class<?>> domainClasses = getDomainClasses(genericType);
            return getJAXBContext(domainClasses, annotations, mediaType, null);
        } catch(JAXBException e) {
            AbstractSessionLog.getLog().logThrowable(SessionLog.WARNING, SessionLog.MOXY, e);
            return null;
        }
    }

    /**
     * By default the JSON-binding will ignore namespace qualification. If this
     * property is set the portion of the key before the namespace separator
     * will be used to determine the namespace URI.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#NAMESPACE_PREFIX_MAPPER
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_NAMESPACE_PREFIX_MAPPER
     */
    public Map<String, String> getNamespacePrefixMapper() {
        return namespacePrefixMapper;
    }

    /**
     * This character (default is '.') separates the prefix from the key name.
     * It is only used if namespace qualification has been enabled be setting a
     * namespace prefix mapper.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_NAMESPACE_SEPARATOR
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_NAMESPACE_SEPARATOR
     */
    public char getNamespaceSeparator() {
        return this.namespaceSeperator;
    }

    /*
     * @return -1 since the size of the JSON message is not known.
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * The key that will correspond to the property mapped with @XmlValue.  This
     * key will only be used if there are other mapped properties.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_VALUE_WRAPPER
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_VALUE_WRAPPER
     */
    public String getValueWrapper() {
        return valueWrapper;
    }

    /**
     * @return true if the JSON output should be formatted (default is false).
     */
    public boolean isFormattedOutput() {
        return formattedOutput;
    }

    /**
     * @return true if the root node is included in the JSON message (default is
     * false).
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_INCLUDE_ROOT
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_INCLUDE_ROOT
     */
    public boolean isIncludeRoot() {
        return includeRoot;
    }

    /**
     * If true empty collections will be marshalled as empty arrays, else the
     * collection will not be marshalled to JSON (default is true).
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_MARSHAL_EMPTY_COLLECTIONS
     */
    public boolean isMarshalEmptyCollections() {
        return marshalEmptyCollections;
    }

    /**
     * @return true indicating that <i>MOXyJsonProvider</i> will
     * be used for the JSON binding if the media type is of the following
     * patterns *&#47;json or *&#47;*+json, and the type is not assignable from
     * any of (or a Collection or JAXBElement of) the following:
     * <ul>
     * <li>byte[]</li>
     * <li>java.io.File</li>
     * <li>java.io.InputStream</li>
     * <li>java.io.Reader</li>
     * <li>java.lang.Object</li>
     * <li>java.lang.String</li>
     * <li>javax.activation.DataSource</li>
     * </ul>
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if(!supportsMediaType(mediaType)) {
            return false;
        } else if(CoreClassConstants.APBYTE == type || CoreClassConstants.STRING == type) {
            return false;
        } else if(Map.class.isAssignableFrom(type)) {
            return false;
        } else if(File.class.isAssignableFrom(type)) {
            return false;
        } else if(DataSource.class.isAssignableFrom(type)) {
            return false;
        } else if(InputStream.class.isAssignableFrom(type)) {
            return false;
        } else if(Reader.class.isAssignableFrom(type)) {
            return false;
        } else if(Object.class == type) {
            return false;
        } else if(type.isPrimitive()) {
            return false;
        } else if(type.isArray() && (type.getComponentType().isArray() || type.getComponentType().isPrimitive() || type.getComponentType().getPackage().getName().startsWith("java."))) {
            return false;
        } else if(JAXBElement.class.isAssignableFrom(type)) {
            Set<Class<?>> domainClasses = getDomainClasses(genericType);
            for (Class<?> domainClass : domainClasses) {
                if (isReadable(domainClass, null, annotations, mediaType) || String.class == domainClass) {
                    return true;
                }
            }
            return false;
        } else if(Collection.class.isAssignableFrom(type)) {
            Set<Class<?>> domainClasses = getDomainClasses(genericType);
            for (Class<?> domainClass : domainClasses) {
                if (isReadable(domainClass, null, annotations, mediaType) || String.class == domainClass) {
                    return true;
                }
            }
            return false;
        } else {
            return null != getJAXBContext(type, genericType, annotations, mediaType);
        }
    }

    /**
     * If true the grouping element will be used as the JSON key.
     *
     * <p><b>Example</b></p>
     * <p>Given the following class:</p>
     * <pre>
     * &#64;XmlAccessorType(XmlAccessType.FIELD)
     * public class Customer {
     *
     *     &#64;XmlElementWrapper(name="phone-numbers")
     *     &#64;XmlElement(name="phone-number")
     *     private {@literal List<PhoneNumber>} phoneNumbers;
     *
     * }
     * </pre>
     * <p>If the property is set to false (the default) the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : {
     *         "phone-number" : [ {
     *             ...
     *         }, {
     *             ...
     *         }]
     *     }
     * }
     * </pre>
     * <p>And if the property is set to true, then the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : [ {
     *         ...
     *     }, {
     *         ...
     *     }]
     * }
     * </pre>
     * @since 2.4.2
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#JSON_WRAPPER_AS_ARRAY_NAME
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_WRAPPER_AS_ARRAY_NAME
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_WRAPPER_AS_ARRAY_NAME
     */
    public boolean isWrapperAsArrayName() {
        return wrapperAsArrayName;
    }

    /**
     * @return true indicating that <i>MOXyJsonProvider</i> will
     * be used for the JSON binding if the media type is of the following
     * patterns *&#47;json or *&#47;*+json, and the type is not assignable from
     * any of (or a Collection or JAXBElement of) the following:
     * <ul>
     * <li>byte[]</li>
     * <li>java.io.File</li>
     * <li>java.lang.Object</li>
     * <li>java.lang.String</li>
     * <li>jakarta.activation.DataSource</li>
     * <li>jakarta.ws.rs.core.StreamingOutput</li>
     * </ul>
     */
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if(type == JSONWithPadding.class && APPLICATION_XJAVASCRIPT.equals(mediaType.toString())) {
            return true;
        }
        if(!supportsMediaType(mediaType)) {
            return false;
        } else if(CoreClassConstants.APBYTE == type || CoreClassConstants.STRING == type || type.isPrimitive()) {
            return false;
        } else if(Map.class.isAssignableFrom(type)) {
            return false;
        } else if(File.class.isAssignableFrom(type)) {
            return false;
        } else if(DataSource.class.isAssignableFrom(type)) {
            return false;
        } else if(StreamingOutput.class.isAssignableFrom(type)) {
            return false;
        } else if(Object.class == type) {
            return false;
        } else if(type.isPrimitive()) {
            return false;
        } else if(type.isArray() && (String.class.equals(type.getComponentType()) || type.getComponentType().isPrimitive() || Helper.isPrimitiveWrapper(type.getComponentType()))) {
                return true;
        } else if(type.isArray() && (type.getComponentType().isArray() || type.getComponentType().isPrimitive() || type.getComponentType().getPackage().getName().startsWith("java."))) {
            return false;
        } else if(JAXBElement.class.isAssignableFrom(type)) {
            Set<Class<?>> domainClasses = getDomainClasses(genericType);

            for (Class<?> domainClass : domainClasses) {
                if (isWriteable(domainClass, null, annotations, mediaType) || domainClass == String.class) {
                    return true;
                }
            }

            return false;
        } else if(Collection.class.isAssignableFrom(type)) {
            Set<Class<?>> domainClasses = getDomainClasses(genericType);

            //special case for List<JAXBElement<String>>
            //this is quick fix, MOXyJsonProvider should be refactored as stated in issue #459541
            if (domainClasses.size() == 3) {
                Class[] domainArray = domainClasses.toArray(new Class[domainClasses.size()]);
                if (JAXBElement.class.isAssignableFrom(domainArray[1]) && String.class == domainArray[2]) {
                    return true;
                }
            }

            for (Class<?> domainClass : domainClasses) {

                if (String.class.equals(domainClass) || domainClass.isPrimitive() || Helper.isPrimitiveWrapper(domainClass)) {
                    return true;
                }

                    String packageName = domainClass.getPackage().getName();
                if(null == packageName || !packageName.startsWith("java.")) {
                    if (isWriteable(domainClass, null, annotations, mediaType)) {
                        return true;
                    }
                }
            }
            return false;
         } else {
             return null != getJAXBContext(type, genericType, annotations, mediaType);
        }
    }

    /**
     * Subclasses of <i>MOXyJsonProvider</i> can override this method to
     * customize the instance of <i>Unmarshaller</i> that will be used to
     * unmarshal the JSON message in the readFrom call.
     * @param type - The Class to be unmarshalled (i.e. <i>Customer</i> or
     * <i>List</i>)
     * @param genericType - The type of object to be unmarshalled (i.e
     * <i>Customer</i> or <i>List&lt;Customer&gt;</i>).
     * @param annotations - The annotations corresponding to domain object.
     * @param mediaType - The media type for the HTTP entity.
     * @param httpHeaders - HTTP headers associated with HTTP entity.
     * @param unmarshaller - The instance of <i>Unmarshaller</i> that will be
     * used to unmarshal the JSON message.
     * @throws JAXBException
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties
     */
    protected void preReadFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, Unmarshaller unmarshaller) throws JAXBException {
    }

    /**
     * Subclasses of <i>MOXyJsonProvider</i> can override this method to
     * customize the instance of <i>Marshaller</i> that will be used to marshal
     * the domain objects to JSON in the writeTo call.
     * @param object - The domain object that will be marshalled to JSON.
     * @param type - The Class to be marshalled (i.e. <i>Customer</i> or
     * <i>List</i>)
     * @param genericType - The type of object to be marshalled (i.e
     * <i>Customer</i> or <i>List&lt;Customer&gt;</i>).
     * @param annotations - The annotations corresponding to domain object.
     * @param mediaType - The media type for the HTTP entity.
     * @param httpHeaders - HTTP headers associated with HTTP entity.
     * @param marshaller - The instance of <i>Marshaller</i> that will be used
     * to marshal the domain object to JSON.
     * @throws JAXBException
     * @see org.eclipse.persistence.jaxb.MarshallerProperties
     */
    protected void preWriteTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, Marshaller marshaller) throws JAXBException {
    }

    /*
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            if(null == genericType) {
                genericType = type;
            }

            Set<Class<?>> domainClasses = getDomainClasses(genericType);
            JAXBContext jaxbContext = getJAXBContext(domainClasses, annotations, mediaType, httpHeaders);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, attributePrefix);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, includeRoot);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_SEPARATOR, namespaceSeperator);
            if(null != valueWrapper) {
                unmarshaller.setProperty(UnmarshallerProperties.JSON_VALUE_WRAPPER, valueWrapper);
            }
            unmarshaller.setProperty(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, wrapperAsArrayName);
            preReadFrom(type, genericType, annotations, mediaType, httpHeaders, unmarshaller);

            StreamSource jsonSource;
            Map<String, String> mediaTypeParameters = null;
            if(null != mediaType) {
                mediaTypeParameters = mediaType.getParameters();
            }
            if(null != mediaTypeParameters && mediaTypeParameters.containsKey(CHARSET)) {
                String charSet = mediaTypeParameters.get(CHARSET);
                Reader entityReader = new InputStreamReader(entityStream, charSet);
                jsonSource = new StreamSource(entityReader);
            } else {
                jsonSource = new StreamSource(entityStream);
            }

            Class<?> domainClass = getDomainClass(domainClasses);
            JAXBElement<?> jaxbElement = unmarshaller.unmarshal(jsonSource, domainClass);
            if(type.isAssignableFrom(JAXBElement.class)) {
                return jaxbElement;
            } else {
                Object value = jaxbElement.getValue();
                if(value instanceof ArrayList) {
                    if(type.isArray()) {
                        ArrayList<Object> arrayList = (ArrayList<Object>) value;
                        int arrayListSize = arrayList.size();
                        boolean wrapItemInJAXBElement = wrapItemInJAXBElement(genericType);
                        Object array;
                        if(wrapItemInJAXBElement) {
                            array = Array.newInstance(JAXBElement.class, arrayListSize);
                        } else {
                            array = Array.newInstance(domainClass, arrayListSize);
                        }
                        for(int x=0; x<arrayListSize; x++) {
                            Object element = handleJAXBElement(arrayList.get(x), domainClass, wrapItemInJAXBElement);
                            Array.set(array, x, element);
                        }
                        return array;
                    } else {
                        ContainerPolicy containerPolicy;
                        if(type.isAssignableFrom(List.class) || type.isAssignableFrom(ArrayList.class) || type.isAssignableFrom(Collection.class)) {
                            containerPolicy = new CollectionContainerPolicy(ArrayList.class);
                        } else if(type.isAssignableFrom(Set.class)) {
                            containerPolicy = new CollectionContainerPolicy(HashSet.class);
                        } else if(type.isAssignableFrom(Deque.class) || type.isAssignableFrom(Queue.class)) {
                            containerPolicy = new CollectionContainerPolicy(LinkedList.class);
                        } else if(type.isAssignableFrom(NavigableSet.class) || type.isAssignableFrom(SortedSet.class)) {
                            containerPolicy = new CollectionContainerPolicy(TreeSet.class);
                        } else {
                            containerPolicy = new CollectionContainerPolicy(type);
                        }
                        Object container = containerPolicy.containerInstance();
                        boolean wrapItemInJAXBElement = wrapItemInJAXBElement(genericType);
                        for(Object element : (Collection<Object>) value) {
                            element = handleJAXBElement(element, domainClass, wrapItemInJAXBElement);
                            containerPolicy.addInto(element, container, null);
                        }
                        return container;
                    }
                } else {
                    return value;
                }
            }
        } catch(UnmarshalException unmarshalException) {
            ResponseBuilder builder = Response.status(Status.BAD_REQUEST);
            throw new WebApplicationException(unmarshalException, builder.build());
        } catch(JAXBException jaxbException) {
            throw new WebApplicationException(jaxbException);
        }
    }

    /**
     * Get first non java class if exists.
     *
     * @param domainClasses
     * @return first domain class or first generic class or just the first class from the list
     */
    public Class<?> getDomainClass(Set<Class<?>> domainClasses) {

        if (domainClasses.size() == 1) {
            return domainClasses.iterator().next();
        }

        boolean isStringPresent = false;

        for (Class<?> clazz : domainClasses) {
            if (!clazz.getName().startsWith("java.")
                    && !clazz.getName().startsWith("javax.")
                    && !clazz.getName().startsWith("jakarta.")
                    && !java.util.List.class.isAssignableFrom(clazz)) {
                return clazz;
            } else if (clazz == String.class) {
                isStringPresent = true;
            }
        }

        if (isStringPresent) {
            return String.class;
        }

        //handle simple generic case
        if (domainClasses.size() >= 2) {
            Iterator<Class<?>> it = domainClasses.iterator();
            it.next();
            return it.next();
        }

        return domainClasses.iterator().next();
    }

    private boolean wrapItemInJAXBElement(Type genericType) {
        if(genericType == JAXBElement.class) {
            return true;
        } else if(genericType instanceof GenericArrayType) {
            return wrapItemInJAXBElement(((GenericArrayType) genericType).getGenericComponentType());
        } else if(genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            return wrapItemInJAXBElement(parameterizedType.getOwnerType()) || wrapItemInJAXBElement(parameterizedType.getRawType()) || wrapItemInJAXBElement(actualType);
        } else {
            return false;
        }
    }

    private Object handleJAXBElement(Object element, Class domainClass, boolean wrapItemInJAXBElement) {
        if(wrapItemInJAXBElement) {
            if(element instanceof JAXBElement) {
                return element;
            } else {
                return new JAXBElement(EMPTY_STRING_QNAME, domainClass, element);
            }
        } else {
            return JAXBIntrospector.getValue(element);
        }
    }

    /**
     * Specify a value that will be prepended to all keys that are mapped to an
     * XML attribute.  By default there is no attribute prefix.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_ATTRIBUTE_PREFIX
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_ATTRIBUTE_PREFIX
     */
    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    /**
     * Specify if the JSON output should be formatted (default is false).
     * @param formattedOutput - true if the output should be formatted, else
     * false.
     */
    public void setFormattedOutput(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
    }

    /**
     * Specify if the root node should be included in the JSON message (default
     * is false).
     * @param includeRoot - true if the message includes the root node, else
     * false.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_INCLUDE_ROOT
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_INCLUDE_ROOT
     */
    public void setIncludeRoot(boolean includeRoot) {
        this.includeRoot = includeRoot;
    }

    /**
     * If true empty collections will be marshalled as empty arrays, else the
     * collection will not be marshalled to JSON (default is true).
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_MARSHAL_EMPTY_COLLECTIONS
     */
    public void setMarshalEmptyCollections(boolean marshalEmptyCollections) {
        this.marshalEmptyCollections = marshalEmptyCollections;
    }

   /**
     * By default the JSON-binding will ignore namespace qualification. If this
     * property is set then a prefix corresponding to the namespace URI and a
     * namespace separator will be prefixed to the key.
     * include it you can specify a Map of namespace URI to prefix.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#NAMESPACE_PREFIX_MAPPER
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_NAMESPACE_PREFIX_MAPPER
     */
    public void setNamespacePrefixMapper(Map<String, String> namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
    }

    /**
     * This character (default is '.') separates the prefix from the key name.
     * It is only used if namespace qualification has been enabled be setting a
     * namespace prefix mapper.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_NAMESPACE_SEPARATOR
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_NAMESPACE_SEPARATOR
     */
    public void setNamespaceSeparator(char namespaceSeparator) {
        this.namespaceSeperator = namespaceSeparator;
    }

    /**
     * If true the grouping element will be used as the JSON key.
     *
     * <p><b>Example</b></p>
     * <p>Given the following class:</p>
     * <pre>
     * &#64;XmlAccessorType(XmlAccessType.FIELD)
     * public class Customer {
     *
     *     &#64;XmlElementWrapper(name="phone-numbers")
     *     &#64;XmlElement(name="phone-number")
     *     private {@literal List<PhoneNumber>} phoneNumbers;
     *
     * }
     * </pre>
     * <p>If the property is set to false (the default) the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : {
     *         "phone-number" : [ {
     *             ...
     *         }, {
     *             ...
     *         }]
     *     }
     * }
     * </pre>
     * <p>And if the property is set to true, then the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : [ {
     *         ...
     *     }, {
     *         ...
     *     }]
     * }
     * </pre>
     * @since 2.4.2
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#JSON_WRAPPER_AS_ARRAY_NAME
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_WRAPPER_AS_ARRAY_NAME
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_WRAPPER_AS_ARRAY_NAME
     */
    public void setWrapperAsArrayName(boolean wrapperAsArrayName) {
        this.wrapperAsArrayName = wrapperAsArrayName;
    }

    /**
     * Specify the key that will correspond to the property mapped with
     * {@literal @XmlValue}.  This key will only be used if there are other mapped
     * properties.
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#JSON_VALUE_WRAPPER
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties#JSON_VALUE_WRAPPER
     */
    public void setValueWrapper(String valueWrapper) {
        this.valueWrapper = valueWrapper;
    }

    /**
     * @return true for all media types of the pattern *&#47;json and
     * *&#47;*+json.
     */
    protected boolean supportsMediaType(MediaType mediaType) {
        if(null == mediaType) {
            return true;
        }
        String subtype = mediaType.getSubtype();
        return subtype.equals(JSON) || subtype.endsWith(PLUS_JSON);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            if(null == genericType) {
                genericType = type;
            }

            Set<Class<?>> domainClasses = getDomainClasses(genericType);
            JAXBContext jaxbContext = getJAXBContext(domainClasses, annotations, mediaType, httpHeaders);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, attributePrefix);
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, includeRoot);
            marshaller.setProperty(MarshallerProperties.JSON_MARSHAL_EMPTY_COLLECTIONS, marshalEmptyCollections);
            marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, namespaceSeperator);
            if(null != valueWrapper) {
                marshaller.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, valueWrapper);
            }
            marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, wrapperAsArrayName);
            marshaller.setProperty(MarshallerProperties.NAMESPACE_PREFIX_MAPPER, namespacePrefixMapper);

            Map<String, String> mediaTypeParameters = null;
            if(null != mediaType) {
                mediaTypeParameters = mediaType.getParameters();
            }
            if(null != mediaTypeParameters && mediaTypeParameters.containsKey(CHARSET)) {
                String charSet = mediaTypeParameters.get(CHARSET);
                marshaller.setProperty(Marshaller.JAXB_ENCODING, charSet);
            }

            preWriteTo(object, type, genericType, annotations, mediaType, httpHeaders, marshaller);
            if (domainClasses.size() == 1) {
                Class<?> domainClass = domainClasses.iterator().next();
                if(!(List.class.isAssignableFrom(type) ||  type.isArray()) && domainClass.getPackage().getName().startsWith("java.")) {
                    object = new JAXBElement(new QName((String) marshaller.getProperty(MarshallerProperties.JSON_VALUE_WRAPPER)), domainClass, object);
                }
            }

            marshaller.marshal(object, entityStream);
        } catch(JAXBException jaxbException) {
            throw new WebApplicationException(jaxbException);
        }
    }

}
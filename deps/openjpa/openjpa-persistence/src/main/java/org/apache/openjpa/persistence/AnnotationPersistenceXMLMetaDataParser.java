/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.DelegatingMetaDataFactory;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.XMLFieldMetaData;
import org.apache.openjpa.meta.XMLMetaData;

/**
 * JAXB xml annotation metadata parser.
 *
 * @author Catalina Wei
 * @since 1.0.0
 * @nojavadoc
 */
public class AnnotationPersistenceXMLMetaDataParser {

    private static final Localizer _loc = Localizer.forPackage
        (AnnotationPersistenceXMLMetaDataParser.class);

    private final OpenJPAConfiguration _conf;
    private final Log _log;
    private MetaDataRepository _repos = null;
    
    // cache the JAXB Xml... classes if they are present so we do not
    // have a hard-wired dependency on JAXB here
    private Class xmlTypeClass = null;
    private Class xmlRootElementClass = null;
    private Class xmlAccessorTypeClass = null;
    private Class xmlAttributeClass = null;
    private Class xmlElementClass = null;
    private Method xmlTypeName = null;
    private Method xmlTypeNamespace = null;
    private Method xmlRootName = null;
    private Method xmlRootNamespace = null;
    private Method xmlAttributeName = null;
    private Method xmlAttributeNamespace = null;
    private Method xmlElementName = null;
    private Method xmlElementNamespace = null;
    private Method xmlAccessorValue = null;

    /**
     * Constructor; supply configuration.
     */
    public AnnotationPersistenceXMLMetaDataParser(OpenJPAConfiguration conf) {
        _conf = conf;
        _log = conf.getLog(OpenJPAConfiguration.LOG_METADATA);
        try {
            xmlTypeClass = Class.forName(
                "javax.xml.bind.annotation.XmlType");
            xmlTypeName = xmlTypeClass.getMethod("name", null);
            xmlTypeNamespace = xmlTypeClass.getMethod("namespace", null);
            xmlRootElementClass = Class.forName(
                "javax.xml.bind.annotation.XmlRootElement");
            xmlRootName = xmlRootElementClass.getMethod("name", null);
            xmlRootNamespace = xmlRootElementClass.getMethod("namespace", null);
            xmlAccessorTypeClass = Class.forName(
                "javax.xml.bind.annotation.XmlAccessorType");
            xmlAccessorValue = xmlAccessorTypeClass.getMethod("value", null);
            xmlAttributeClass = Class.forName(
                "javax.xml.bind.annotation.XmlAttribute");
            xmlAttributeName = xmlAttributeClass.getMethod("name", null);
            xmlAttributeNamespace = xmlAttributeClass.getMethod("namespace"
                , null);
            xmlElementClass = Class.forName(
                "javax.xml.bind.annotation.XmlElement");
            xmlElementName = xmlElementClass.getMethod("name", null);
            xmlElementNamespace = xmlElementClass.getMethod("namespace", null);
        } catch (Exception e) {
        }
    }

    /**
     * Configuration supplied on construction.
     */
    public OpenJPAConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * Metadata log.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * Returns the repository for this parser. If none has been set,
     * create a new repository and sets it.
     */
    public MetaDataRepository getRepository() {
        if (_repos == null) {
            MetaDataRepository repos = _conf.newMetaDataRepositoryInstance();
            MetaDataFactory mdf = repos.getMetaDataFactory();
            if (mdf instanceof DelegatingMetaDataFactory)
                mdf = ((DelegatingMetaDataFactory) mdf).getInnermostDelegate();
            if (mdf instanceof PersistenceMetaDataFactory)
                ((PersistenceMetaDataFactory) mdf).setXMLAnnotationParser(this);
            _repos = repos;
        }
        return _repos;
    }

    /**
     * Set the metadata repository for this parser.
     */
    public void setRepository(MetaDataRepository repos) {
        _repos = repos;
    }

    /**
     * Clear caches.
     */
    public void clear() {
    }

    /**
     * Parse persistence metadata for the given field metadata. This parser/class is NOT threadsafe! The caller of 
     * this method needs to insure that the MetaData(/Mapping)Repository is locked prior to calling this method.
     */
    public synchronized void parse(Class<?> cls) {
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("parse-class", cls.getName()));
        parseXMLClassAnnotations(cls);
    }

    /**
     * Read annotations for the current type.
     */
    private XMLMetaData parseXMLClassAnnotations(Class<?> cls) {
        // check immediately whether the class has JAXB XML annotations
        if (cls == null || xmlTypeClass == null
            || !((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, xmlTypeClass))).booleanValue()
                && (AccessController
                .doPrivileged(J2DoPrivHelper.isAnnotationPresentAction(cls,
                    xmlRootElementClass))).booleanValue()))
            return null;

        // find / create metadata
        XMLMetaData meta = getXMLMetaData(cls);
        
        return meta;
    }

    /**
     * Find or create xml metadata for the current type. 
     */
    private XMLMetaData getXMLMetaData(Class<?> cls) {
        XMLMetaData meta = getRepository().getCachedXMLMetaData(cls);
        if (meta == null) {
            // if not in cache, create metadata
            meta = getRepository().addXMLClassMetaData(cls);
            parseXmlRootElement(cls, meta);
            populateFromReflection(cls, meta);
        }
        return meta;
    }
    
    private void parseXmlRootElement(Class type, XMLMetaData meta) {
        try {
            if (type.getAnnotation(xmlRootElementClass) != null) {
                meta.setXmlRootElement(true);
                meta.setXmlname((String) xmlRootName.invoke(type.getAnnotation
                    (xmlRootElementClass), new Object[]{}));
                meta.setXmlnamespace((String) xmlRootNamespace.invoke(type
                    .getAnnotation(xmlRootElementClass), new Object[]{}));
            }
            else {
                meta.setXmlname((String) xmlTypeName.invoke(type.getAnnotation
                    (xmlTypeClass), new Object[]{}));
                meta.setXmlnamespace((String) xmlTypeNamespace.invoke(type
                    .getAnnotation(xmlTypeClass), new Object[]{}));           
            }
        } catch (Exception e) {            
        }
    }

    private void populateFromReflection(Class cls, XMLMetaData meta) {
        Member[] members;
        
        Class superclass = cls.getSuperclass();

        // handle inheritance at sub-element level
        if ((AccessController.doPrivileged(J2DoPrivHelper
            .isAnnotationPresentAction(superclass, xmlTypeClass)))
            .booleanValue())
            populateFromReflection(superclass, meta);

        try {
            if (StringUtils.equals(xmlAccessorValue.invoke(cls.getAnnotation(
                xmlAccessorTypeClass), new Object[]{}).toString(), "FIELD"))
                members = cls.getDeclaredFields();
            else
                members = cls.getDeclaredMethods();

            for (int i = 0; i < members.length; i++) {
                Member member = members[i];
                AnnotatedElement el = (AnnotatedElement) member;
                XMLMetaData field = null;
                if (el.getAnnotation(xmlElementClass) != null) {
                    String xmlname = (String) xmlElementName.invoke(el
                        .getAnnotation(xmlElementClass), new Object[]{});
                    // avoid JAXB XML bind default name
                    if (StringUtils.equals(XMLMetaData.defaultName, xmlname))
                        xmlname = member.getName();
                    if ((AccessController.doPrivileged(J2DoPrivHelper
                        .isAnnotationPresentAction(((Field) member).getType(),
                            xmlTypeClass))).booleanValue()) {
                        field = _repos.addXMLClassMetaData(((Field) member).getType());
                        parseXmlRootElement(((Field) member).getType(), field);
                        populateFromReflection(((Field) member).getType()
                            , field);
                        field.setXmltype(XMLMetaData.XMLTYPE);
                        field.setXmlname(xmlname);
                    }
                    else {
                        field = _repos.newXMLFieldMetaData(((Field) member)
                            .getType(), member.getName());
                        field.setXmltype(XMLMetaData.ELEMENT);
                        field.setXmlname(xmlname);
                        field.setXmlnamespace((String) xmlElementNamespace
                            .invoke(el.getAnnotation(xmlElementClass)
                            , new Object[]{}));                    
                    }
                }
                else if (el.getAnnotation(xmlAttributeClass) != null) {
                    field = _repos.newXMLFieldMetaData(((Field) member)
                        .getType(), member.getName());
                    field.setXmltype(XMLFieldMetaData.ATTRIBUTE);
                    String xmlname = (String) xmlAttributeName.invoke(
                        el.getAnnotation(xmlAttributeClass), new Object[]{});
                    // avoid JAXB XML bind default name
                    if (StringUtils.equals(XMLMetaData.defaultName, xmlname))
                        xmlname = member.getName();
                    field.setXmlname("@"+xmlname);
                    field.setXmlnamespace((String) xmlAttributeNamespace.invoke(
                        el.getAnnotation(xmlAttributeClass), new Object[]{}));
                }
                if (field != null)
                    meta.addField(member.getName(), field);
            }
        } catch(Exception e) {
        }
    }
}

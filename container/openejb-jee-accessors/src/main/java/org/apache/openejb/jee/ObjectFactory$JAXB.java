/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.JAXBObjectFactory;

public class ObjectFactory$JAXB
    extends JAXBObjectFactory<ObjectFactory>
{

    public static final ObjectFactory$JAXB INSTANCE = new ObjectFactory$JAXB();
    private final Map<QName, Class<? extends JAXBObject>> rootElements = new HashMap<>();

    public ObjectFactory$JAXB() {
        super(ObjectFactory.class, EjbRelationshipRole$JAXB.class, Text$JAXB.class, Application$JAXB.class, ApplicationClient$JAXB.class, EjbJar$JAXB.class, WebApp$JAXB.class, TldTaglib$JAXB.class, Connector$JAXB.class, Webservices$JAXB.class, JavaWsdlMapping$JAXB.class, FacesConfig$JAXB.class, WebFragment$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-relation-name".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-relationship-role".intern()), EjbRelationshipRole$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "description".intern()), Text$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "http-method".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "application".intern()), Application$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "application-client".intern()), ApplicationClient$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "ejb-jar".intern()), EjbJar$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "web-app".intern()), WebApp$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "taglib".intern()), TldTaglib$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/j2ee".intern(), "connector".intern()), Connector$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "webservices".intern()), Webservices$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/j2ee".intern(), "java-wsdl-mapping".intern()), JavaWsdlMapping$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config".intern()), FacesConfig$JAXB.class);
        rootElements.put(new QName("http://java.sun.com/xml/ns/javaee".intern(), "web-fragment".intern()), WebFragment$JAXB.class);
    }

    public Map<QName, Class<? extends JAXBObject>> getRootElements() {
        return rootElements;
    }

}

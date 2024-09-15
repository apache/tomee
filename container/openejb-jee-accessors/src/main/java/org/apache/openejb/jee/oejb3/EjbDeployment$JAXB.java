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

package org.apache.openejb.jee.oejb3;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.oejb3.EjbLink$JAXB.readEjbLink;
import static org.apache.openejb.jee.oejb3.EjbLink$JAXB.writeEjbLink;
import static org.apache.openejb.jee.oejb3.Jndi$JAXB.readJndi;
import static org.apache.openejb.jee.oejb3.Jndi$JAXB.writeJndi;
import static org.apache.openejb.jee.oejb3.Query$JAXB.readQuery;
import static org.apache.openejb.jee.oejb3.Query$JAXB.writeQuery;
import static org.apache.openejb.jee.oejb3.ResourceLink$JAXB.readResourceLink;
import static org.apache.openejb.jee.oejb3.ResourceLink$JAXB.writeResourceLink;
import static org.apache.openejb.jee.oejb3.RoleMapping$JAXB.readRoleMapping;
import static org.apache.openejb.jee.oejb3.RoleMapping$JAXB.writeRoleMapping;

@SuppressWarnings({
    "StringEquality"
})
public class EjbDeployment$JAXB
    extends JAXBObject<EjbDeployment>
{


    public EjbDeployment$JAXB() {
        super(EjbDeployment.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "ejb-deployment".intern()), new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "ejbDeployment".intern()), Jndi$JAXB.class, EjbLink$JAXB.class, ResourceLink$JAXB.class, Query$JAXB.class, RoleMapping$JAXB.class);
    }

    public static EjbDeployment readEjbDeployment(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeEjbDeployment(XoXMLStreamWriter writer, EjbDeployment ejbDeployment, RuntimeContext context)
        throws Exception
    {
        _write(writer, ejbDeployment, context);
    }

    public void write(XoXMLStreamWriter writer, EjbDeployment ejbDeployment, RuntimeContext context)
        throws Exception
    {
        _write(writer, ejbDeployment, context);
    }

    public static final EjbDeployment _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        EjbDeployment ejbDeployment = new EjbDeployment();
        context.beforeUnmarshal(ejbDeployment, LifecycleCallback.NONE);

        List<Jndi> jndi = null;
        List<EjbLink> ejbLink = null;
        List<ResourceLink> resourceLink = null;
        List<Query> query = null;
        List<RoleMapping> roleMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("ejbDeployment"!= xsiType.getLocalPart())||("http://www.openejb.org/openejb-jar/1.1"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, EjbDeployment.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("container-id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: containerId
                ejbDeployment.containerId = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("deployment-id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: deploymentId
                String deploymentId = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, deploymentId, ejbDeployment);
                ejbDeployment.deploymentId = deploymentId;
            } else if (("ejb-name" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: ejbName
                ejbDeployment.ejbName = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "container-id"), new QName("", "deployment-id"), new QName("", "ejb-name"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("jndi" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: jndi
                Jndi jndiItem = readJndi(elementReader, context);
                if (jndi == null) {
                    jndi = ejbDeployment.jndi;
                    if (jndi!= null) {
                        jndi.clear();
                    } else {
                        jndi = new ArrayList<>();
                    }
                }
                jndi.add(jndiItem);
            } else if (("ejb-link" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: ejbLink
                EjbLink ejbLinkItem = readEjbLink(elementReader, context);
                if (ejbLink == null) {
                    ejbLink = ejbDeployment.ejbLink;
                    if (ejbLink!= null) {
                        ejbLink.clear();
                    } else {
                        ejbLink = new ArrayList<>();
                    }
                }
                ejbLink.add(ejbLinkItem);
            } else if (("resource-link" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: resourceLink
                ResourceLink resourceLinkItem = readResourceLink(elementReader, context);
                if (resourceLink == null) {
                    resourceLink = ejbDeployment.resourceLink;
                    if (resourceLink!= null) {
                        resourceLink.clear();
                    } else {
                        resourceLink = new ArrayList<>();
                    }
                }
                resourceLink.add(resourceLinkItem);
            } else if (("query" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: query
                Query queryItem = readQuery(elementReader, context);
                if (query == null) {
                    query = ejbDeployment.query;
                    if (query!= null) {
                        query.clear();
                    } else {
                        query = new ArrayList<>();
                    }
                }
                query.add(queryItem);
            } else if (("role-mapping" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: roleMapping
                RoleMapping roleMappingItem = readRoleMapping(elementReader, context);
                if (roleMapping == null) {
                    roleMapping = ejbDeployment.roleMapping;
                    if (roleMapping!= null) {
                        roleMapping.clear();
                    } else {
                        roleMapping = new ArrayList<>();
                    }
                }
                roleMapping.add(roleMappingItem);
            } else if (("properties" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: properties
                String propertiesRaw = elementReader.getElementText();

                Properties properties;
                try {
                    properties = Adapters.propertiesAdapterAdapter.unmarshal(propertiesRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, PropertiesAdapter.class, Properties.class, Properties.class, e);
                    continue;
                }

                ejbDeployment.properties = properties;
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "jndi"), new QName("http://www.openejb.org/openejb-jar/1.1", "ejb-link"), new QName("http://www.openejb.org/openejb-jar/1.1", "resource-link"), new QName("http://www.openejb.org/openejb-jar/1.1", "query"), new QName("http://www.openejb.org/openejb-jar/1.1", "role-mapping"), new QName("http://www.openejb.org/openejb-jar/1.1", "properties"));
            }
        }
        if (jndi!= null) {
            ejbDeployment.jndi = jndi;
        }
        if (ejbLink!= null) {
            ejbDeployment.ejbLink = ejbLink;
        }
        if (resourceLink!= null) {
            ejbDeployment.resourceLink = resourceLink;
        }
        if (query!= null) {
            ejbDeployment.query = query;
        }
        if (roleMapping!= null) {
            ejbDeployment.roleMapping = roleMapping;
        }

        context.afterUnmarshal(ejbDeployment, LifecycleCallback.NONE);

        return ejbDeployment;
    }

    public final EjbDeployment read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, EjbDeployment ejbDeployment, RuntimeContext context)
        throws Exception
    {
        if (ejbDeployment == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://www.openejb.org/openejb-jar/1.1");
        if (EjbDeployment.class!= ejbDeployment.getClass()) {
            context.unexpectedSubclass(writer, ejbDeployment, EjbDeployment.class);
            return ;
        }

        context.beforeMarshal(ejbDeployment, LifecycleCallback.NONE);


        // ATTRIBUTE: containerId
        String containerIdRaw = ejbDeployment.containerId;
        if (containerIdRaw!= null) {
            String containerId = null;
            try {
                containerId = Adapters.collapsedStringAdapterAdapter.marshal(containerIdRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbDeployment, "containerId", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "container-id", containerId);
        }

        // ATTRIBUTE: deploymentId
        String deploymentIdRaw = ejbDeployment.deploymentId;
        if (deploymentIdRaw!= null) {
            String deploymentId = null;
            try {
                deploymentId = Adapters.collapsedStringAdapterAdapter.marshal(deploymentIdRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbDeployment, "deploymentId", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "deployment-id", deploymentId);
        }

        // ATTRIBUTE: ejbName
        String ejbNameRaw = ejbDeployment.ejbName;
        if (ejbNameRaw!= null) {
            String ejbName = null;
            try {
                ejbName = Adapters.collapsedStringAdapterAdapter.marshal(ejbNameRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbDeployment, "ejbName", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "ejb-name", ejbName);
        }

        // ELEMENT: jndi
        List<Jndi> jndi = ejbDeployment.jndi;
        if (jndi!= null) {
            for (Jndi jndiItem: jndi) {
                if (jndiItem!= null) {
                    writer.writeStartElement(prefix, "jndi", "http://www.openejb.org/openejb-jar/1.1");
                    writeJndi(writer, jndiItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbDeployment, "jndi");
                }
            }
        }

        // ELEMENT: ejbLink
        List<EjbLink> ejbLink = ejbDeployment.ejbLink;
        if (ejbLink!= null) {
            for (EjbLink ejbLinkItem: ejbLink) {
                if (ejbLinkItem!= null) {
                    writer.writeStartElement(prefix, "ejb-link", "http://www.openejb.org/openejb-jar/1.1");
                    writeEjbLink(writer, ejbLinkItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbDeployment, "ejbLink");
                }
            }
        }

        // ELEMENT: resourceLink
        List<ResourceLink> resourceLink = ejbDeployment.resourceLink;
        if (resourceLink!= null) {
            for (ResourceLink resourceLinkItem: resourceLink) {
                if (resourceLinkItem!= null) {
                    writer.writeStartElement(prefix, "resource-link", "http://www.openejb.org/openejb-jar/1.1");
                    writeResourceLink(writer, resourceLinkItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbDeployment, "resourceLink");
                }
            }
        }

        // ELEMENT: query
        List<Query> query = ejbDeployment.query;
        if (query!= null) {
            for (Query queryItem: query) {
                if (queryItem!= null) {
                    writer.writeStartElement(prefix, "query", "http://www.openejb.org/openejb-jar/1.1");
                    writeQuery(writer, queryItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(ejbDeployment, "query");
                }
            }
        }

        // ELEMENT: roleMapping
        List<RoleMapping> roleMapping = ejbDeployment.roleMapping;
        if (roleMapping!= null) {
            for (RoleMapping roleMappingItem: roleMapping) {
                if (roleMappingItem!= null) {
                    writer.writeStartElement(prefix, "role-mapping", "http://www.openejb.org/openejb-jar/1.1");
                    writeRoleMapping(writer, roleMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: properties
        Properties propertiesRaw = ejbDeployment.properties;
        String properties = null;
        try {
            properties = Adapters.propertiesAdapterAdapter.marshal(propertiesRaw);
        } catch (Exception e) {
            context.xmlAdapterError(ejbDeployment, "properties", PropertiesAdapter.class, Properties.class, Properties.class, e);
        }
        if (properties!= null) {
            writer.writeStartElement(prefix, "properties", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(properties);
            writer.writeEndElement();
        }

        context.afterMarshal(ejbDeployment, LifecycleCallback.NONE);
    }

}

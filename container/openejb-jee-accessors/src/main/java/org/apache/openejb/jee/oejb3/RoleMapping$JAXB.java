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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

@SuppressWarnings({
    "StringEquality"
})
public class RoleMapping$JAXB
    extends JAXBObject<RoleMapping>
{


    public RoleMapping$JAXB() {
        super(RoleMapping.class, null, null);
    }

    public static RoleMapping readRoleMapping(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeRoleMapping(XoXMLStreamWriter writer, RoleMapping roleMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, roleMapping, context);
    }

    public void write(XoXMLStreamWriter writer, RoleMapping roleMapping, RuntimeContext context)
        throws Exception
    {
        _write(writer, roleMapping, context);
    }

    public static final RoleMapping _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        RoleMapping roleMapping = new RoleMapping();
        context.beforeUnmarshal(roleMapping, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, RoleMapping.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("role-name" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: roleName
                String roleNameRaw = elementReader.getElementText();

                String roleName;
                try {
                    roleName = Adapters.collapsedStringAdapterAdapter.unmarshal(roleNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                roleMapping.roleName = roleName;
            } else if (("principal-name" == elementReader.getLocalName())&&("http://www.openejb.org/openejb-jar/1.1" == elementReader.getNamespaceURI())) {
                // ELEMENT: principalName
                String principalNameRaw = elementReader.getElementText();

                String principalName;
                try {
                    principalName = Adapters.collapsedStringAdapterAdapter.unmarshal(principalNameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                roleMapping.principalName = principalName;
            } else {
                context.unexpectedElement(elementReader, new QName("http://www.openejb.org/openejb-jar/1.1", "role-name"), new QName("http://www.openejb.org/openejb-jar/1.1", "principal-name"));
            }
        }

        context.afterUnmarshal(roleMapping, LifecycleCallback.NONE);

        return roleMapping;
    }

    public final RoleMapping read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, RoleMapping roleMapping, RuntimeContext context)
        throws Exception
    {
        if (roleMapping == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://www.openejb.org/openejb-jar/1.1");
        if (RoleMapping.class!= roleMapping.getClass()) {
            context.unexpectedSubclass(writer, roleMapping, RoleMapping.class);
            return ;
        }

        context.beforeMarshal(roleMapping, LifecycleCallback.NONE);


        // ELEMENT: roleName
        String roleNameRaw = roleMapping.roleName;
        String roleName = null;
        try {
            roleName = Adapters.collapsedStringAdapterAdapter.marshal(roleNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(roleMapping, "roleName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (roleName!= null) {
            writer.writeStartElement(prefix, "role-name", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(roleName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(roleMapping, "roleName");
        }

        // ELEMENT: principalName
        String principalNameRaw = roleMapping.principalName;
        String principalName = null;
        try {
            principalName = Adapters.collapsedStringAdapterAdapter.marshal(principalNameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(roleMapping, "principalName", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (principalName!= null) {
            writer.writeStartElement(prefix, "principal-name", "http://www.openejb.org/openejb-jar/1.1");
            writer.writeCharacters(principalName);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(roleMapping, "principalName");
        }

        context.afterMarshal(roleMapping, LifecycleCallback.NONE);
    }

}

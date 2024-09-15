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
public class EjbLink$JAXB
    extends JAXBObject<EjbLink>
{


    public EjbLink$JAXB() {
        super(EjbLink.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "ejb-link".intern()), null);
    }

    public static EjbLink readEjbLink(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeEjbLink(XoXMLStreamWriter writer, EjbLink ejbLink, RuntimeContext context)
        throws Exception
    {
        _write(writer, ejbLink, context);
    }

    public void write(XoXMLStreamWriter writer, EjbLink ejbLink, RuntimeContext context)
        throws Exception
    {
        _write(writer, ejbLink, context);
    }

    public static final EjbLink _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        EjbLink ejbLink = new EjbLink();
        context.beforeUnmarshal(ejbLink, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, EjbLink.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("deployment-id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: deployentId
                ejbLink.deployentId = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("ejb-ref-name" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: ejbRefName
                ejbLink.ejbRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "deployment-id"), new QName("", "ejb-ref-name"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(ejbLink, LifecycleCallback.NONE);

        return ejbLink;
    }

    public final EjbLink read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, EjbLink ejbLink, RuntimeContext context)
        throws Exception
    {
        if (ejbLink == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (EjbLink.class!= ejbLink.getClass()) {
            context.unexpectedSubclass(writer, ejbLink, EjbLink.class);
            return ;
        }

        context.beforeMarshal(ejbLink, LifecycleCallback.NONE);


        // ATTRIBUTE: deployentId
        String deployentIdRaw = ejbLink.deployentId;
        if (deployentIdRaw!= null) {
            String deployentId = null;
            try {
                deployentId = Adapters.collapsedStringAdapterAdapter.marshal(deployentIdRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbLink, "deployentId", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "deployment-id", deployentId);
        }

        // ATTRIBUTE: ejbRefName
        String ejbRefNameRaw = ejbLink.ejbRefName;
        if (ejbRefNameRaw!= null) {
            String ejbRefName = null;
            try {
                ejbRefName = Adapters.collapsedStringAdapterAdapter.marshal(ejbRefNameRaw);
            } catch (Exception e) {
                context.xmlAdapterError(ejbLink, "ejbRefName", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "ejb-ref-name", ejbRefName);
        }

        context.afterMarshal(ejbLink, LifecycleCallback.NONE);
    }

}

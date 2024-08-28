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
public class ResourceLink$JAXB
    extends JAXBObject<ResourceLink>
{


    public ResourceLink$JAXB() {
        super(ResourceLink.class, new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "resource-link".intern()), null);
    }

    public static ResourceLink readResourceLink(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeResourceLink(XoXMLStreamWriter writer, ResourceLink resourceLink, RuntimeContext context)
        throws Exception
    {
        _write(writer, resourceLink, context);
    }

    public void write(XoXMLStreamWriter writer, ResourceLink resourceLink, RuntimeContext context)
        throws Exception
    {
        _write(writer, resourceLink, context);
    }

    public static final ResourceLink _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        ResourceLink resourceLink = new ResourceLink();
        context.beforeUnmarshal(resourceLink, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            return context.unexpectedXsiType(reader, ResourceLink.class);
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("res-id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: resId
                resourceLink.resId = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (("res-ref-name" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: resRefName
                resourceLink.resRefName = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "res-id"), new QName("", "res-ref-name"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            context.unexpectedElement(elementReader);
        }

        context.afterUnmarshal(resourceLink, LifecycleCallback.NONE);

        return resourceLink;
    }

    public final ResourceLink read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, ResourceLink resourceLink, RuntimeContext context)
        throws Exception
    {
        if (resourceLink == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (ResourceLink.class!= resourceLink.getClass()) {
            context.unexpectedSubclass(writer, resourceLink, ResourceLink.class);
            return ;
        }

        context.beforeMarshal(resourceLink, LifecycleCallback.NONE);


        // ATTRIBUTE: resId
        String resIdRaw = resourceLink.resId;
        if (resIdRaw!= null) {
            String resId = null;
            try {
                resId = Adapters.collapsedStringAdapterAdapter.marshal(resIdRaw);
            } catch (Exception e) {
                context.xmlAdapterError(resourceLink, "resId", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "res-id", resId);
        }

        // ATTRIBUTE: resRefName
        String resRefNameRaw = resourceLink.resRefName;
        if (resRefNameRaw!= null) {
            String resRefName = null;
            try {
                resRefName = Adapters.collapsedStringAdapterAdapter.marshal(resRefNameRaw);
            } catch (Exception e) {
                context.xmlAdapterError(resourceLink, "resRefName", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "res-ref-name", resRefName);
        }

        context.afterMarshal(resourceLink, LifecycleCallback.NONE);
    }

}

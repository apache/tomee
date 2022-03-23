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
package org.apache.openejb.jee;

import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.FacesRedirectViewParam$JAXB.readFacesRedirectViewParam;
import static org.apache.openejb.jee.FacesRedirectViewParam$JAXB.writeFacesRedirectViewParam;

@SuppressWarnings({
    "StringEquality"
})
public class FacesRedirect$JAXB
    extends JAXBObject<FacesRedirect> {


    public FacesRedirect$JAXB() {
        super(FacesRedirect.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-redirectType".intern()), FacesRedirectViewParam$JAXB.class);
    }

    public static FacesRedirect readFacesRedirect(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesRedirect(final XoXMLStreamWriter writer, final FacesRedirect facesRedirect, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRedirect, context);
    }

    public void write(final XoXMLStreamWriter writer, final FacesRedirect facesRedirect, final RuntimeContext context)
        throws Exception {
        _write(writer, facesRedirect, context);
    }

    public final static FacesRedirect _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final FacesRedirect facesRedirect = new FacesRedirect();
        context.beforeUnmarshal(facesRedirect, LifecycleCallback.NONE);

        List<FacesRedirectViewParam> viewParam = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-redirectType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesRedirect.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesRedirect);
                facesRedirect.id = id;
            } else if (("include-view-params" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: includeViewParams
                final Boolean includeViewParams = ("1".equals(attribute.getValue()) || "true".equals(attribute.getValue()));
                facesRedirect.includeViewParams = includeViewParams;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"), new QName("", "include-view-params"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("view-param" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: viewParam
                final FacesRedirectViewParam viewParamItem = readFacesRedirectViewParam(elementReader, context);
                if (viewParam == null) {
                    viewParam = facesRedirect.viewParam;
                    if (viewParam != null) {
                        viewParam.clear();
                    } else {
                        viewParam = new ArrayList<FacesRedirectViewParam>();
                    }
                }
                viewParam.add(viewParamItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "view-param"));
            }
        }
        if (viewParam != null) {
            facesRedirect.viewParam = viewParam;
        }

        context.afterUnmarshal(facesRedirect, LifecycleCallback.NONE);

        return facesRedirect;
    }

    public final FacesRedirect read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final FacesRedirect facesRedirect, RuntimeContext context)
        throws Exception {
        if (facesRedirect == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesRedirect.class != facesRedirect.getClass()) {
            context.unexpectedSubclass(writer, facesRedirect, FacesRedirect.class);
            return;
        }

        context.beforeMarshal(facesRedirect, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = facesRedirect.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(facesRedirect, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ATTRIBUTE: includeViewParams
        final Boolean includeViewParams = facesRedirect.includeViewParams;
        if (includeViewParams != null) {
            writer.writeAttribute("", "", "include-view-params", Boolean.toString(includeViewParams));
        }

        // ELEMENT: viewParam
        final List<FacesRedirectViewParam> viewParam = facesRedirect.viewParam;
        if (viewParam != null) {
            for (final FacesRedirectViewParam viewParamItem : viewParam) {
                if (viewParamItem != null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "view-param");
                    writeFacesRedirectViewParam(writer, viewParamItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesRedirect, LifecycleCallback.NONE);
    }

}

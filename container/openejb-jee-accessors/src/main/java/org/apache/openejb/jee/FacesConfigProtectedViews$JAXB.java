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

import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.UrlPattern$JAXB.readUrlPattern;
import static org.apache.openejb.jee.UrlPattern$JAXB.writeUrlPattern;

@SuppressWarnings({
    "StringEquality"
})
public class FacesConfigProtectedViews$JAXB
    extends JAXBObject<FacesConfigProtectedViews>
{


    public FacesConfigProtectedViews$JAXB() {
        super(FacesConfigProtectedViews.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-protected-viewsType".intern()), UrlPattern$JAXB.class);
    }

    public static FacesConfigProtectedViews readFacesConfigProtectedViews(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeFacesConfigProtectedViews(XoXMLStreamWriter writer, FacesConfigProtectedViews facesConfigProtectedViews, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigProtectedViews, context);
    }

    public void write(XoXMLStreamWriter writer, FacesConfigProtectedViews facesConfigProtectedViews, RuntimeContext context)
        throws Exception
    {
        _write(writer, facesConfigProtectedViews, context);
    }

    public static final FacesConfigProtectedViews _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesConfigProtectedViews facesConfigProtectedViews = new FacesConfigProtectedViews();
        context.beforeUnmarshal(facesConfigProtectedViews, LifecycleCallback.NONE);

        List<UrlPattern> urlPattern = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("faces-config-protected-viewsType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesConfigProtectedViews.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute);
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("url-pattern" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: urlPattern
                UrlPattern urlPatternItem = readUrlPattern(elementReader, context);
                if (urlPattern == null) {
                    urlPattern = facesConfigProtectedViews.urlPattern;
                    if (urlPattern!= null) {
                        urlPattern.clear();
                    } else {
                        urlPattern = new ArrayList<>();
                    }
                }
                urlPattern.add(urlPatternItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "url-pattern"));
            }
        }
        if (urlPattern!= null) {
            facesConfigProtectedViews.urlPattern = urlPattern;
        }

        context.afterUnmarshal(facesConfigProtectedViews, LifecycleCallback.NONE);

        return facesConfigProtectedViews;
    }

    public final FacesConfigProtectedViews read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, FacesConfigProtectedViews facesConfigProtectedViews, RuntimeContext context)
        throws Exception
    {
        if (facesConfigProtectedViews == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        if (FacesConfigProtectedViews.class!= facesConfigProtectedViews.getClass()) {
            context.unexpectedSubclass(writer, facesConfigProtectedViews, FacesConfigProtectedViews.class);
            return ;
        }

        context.beforeMarshal(facesConfigProtectedViews, LifecycleCallback.NONE);


        // ELEMENT: urlPattern
        List<UrlPattern> urlPattern = facesConfigProtectedViews.urlPattern;
        if (urlPattern!= null) {
            for (UrlPattern urlPatternItem: urlPattern) {
                if (urlPatternItem!= null) {
                    writer.writeStartElementWithAutoPrefix("http://java.sun.com/xml/ns/javaee", "url-pattern");
                    writeUrlPattern(writer, urlPatternItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesConfigProtectedViews, "urlPattern");
                }
            }
        }

        context.afterMarshal(facesConfigProtectedViews, LifecycleCallback.NONE);
    }

}

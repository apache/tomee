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

import static org.apache.openejb.jee.CookieConfig$JAXB.readCookieConfig;
import static org.apache.openejb.jee.CookieConfig$JAXB.writeCookieConfig;
import static org.apache.openejb.jee.TrackingMode$JAXB.parseTrackingMode;
import static org.apache.openejb.jee.TrackingMode$JAXB.toStringTrackingMode;

@SuppressWarnings({
    "StringEquality"
})
public class SessionConfig$JAXB
    extends JAXBObject<SessionConfig> {


    public SessionConfig$JAXB() {
        super(SessionConfig.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "session-configType".intern()), CookieConfig$JAXB.class, TrackingMode$JAXB.class);
    }

    public static SessionConfig readSessionConfig(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public static void writeSessionConfig(final XoXMLStreamWriter writer, final SessionConfig sessionConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, sessionConfig, context);
    }

    public void write(final XoXMLStreamWriter writer, final SessionConfig sessionConfig, final RuntimeContext context)
        throws Exception {
        _write(writer, sessionConfig, context);
    }

    public final static SessionConfig _read(final XoXMLStreamReader reader, RuntimeContext context)
        throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final SessionConfig sessionConfig = new SessionConfig();
        context.beforeUnmarshal(sessionConfig, LifecycleCallback.NONE);

        List<TrackingMode> trackingMode = null;

        // Check xsi:type
        final QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("session-configType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, SessionConfig.class);
            }
        }

        // Read attributes
        for (final Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                final String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, sessionConfig);
                sessionConfig.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (final XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("session-timeout" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: sessionTimeout
                final Integer sessionTimeout = Integer.valueOf(elementReader.getElementAsString());
                sessionConfig.sessionTimeout = sessionTimeout;
            } else if (("cookie-config" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: cookieConfig
                final CookieConfig cookieConfig = readCookieConfig(elementReader, context);
                sessionConfig.cookieConfig = cookieConfig;
            } else if (("tracking-mode" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: trackingMode
                final TrackingMode trackingModeItem = parseTrackingMode(elementReader, context, elementReader.getElementAsString());
                if (trackingMode == null) {
                    trackingMode = sessionConfig.trackingMode;
                    if (trackingMode != null) {
                        trackingMode.clear();
                    } else {
                        trackingMode = new ArrayList<TrackingMode>();
                    }
                }
                trackingMode.add(trackingModeItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "session-timeout"), new QName("http://java.sun.com/xml/ns/javaee", "cookie-config"), new QName("http://java.sun.com/xml/ns/javaee", "tracking-mode"));
            }
        }
        if (trackingMode != null) {
            sessionConfig.trackingMode = trackingMode;
        }

        context.afterUnmarshal(sessionConfig, LifecycleCallback.NONE);

        return sessionConfig;
    }

    public final SessionConfig read(final XoXMLStreamReader reader, final RuntimeContext context)
        throws Exception {
        return _read(reader, context);
    }

    public final static void _write(final XoXMLStreamWriter writer, final SessionConfig sessionConfig, RuntimeContext context)
        throws Exception {
        if (sessionConfig == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        final String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (SessionConfig.class != sessionConfig.getClass()) {
            context.unexpectedSubclass(writer, sessionConfig, SessionConfig.class);
            return;
        }

        context.beforeMarshal(sessionConfig, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        final String idRaw = sessionConfig.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (final Exception e) {
                context.xmlAdapterError(sessionConfig, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: sessionTimeout
        final Integer sessionTimeout = sessionConfig.sessionTimeout;
        if (sessionTimeout != null) {
            writer.writeStartElement(prefix, "session-timeout", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Integer.toString(sessionTimeout));
            writer.writeEndElement();
        }

        // ELEMENT: cookieConfig
        final CookieConfig cookieConfig = sessionConfig.cookieConfig;
        if (cookieConfig != null) {
            writer.writeStartElement(prefix, "cookie-config", "http://java.sun.com/xml/ns/javaee");
            writeCookieConfig(writer, cookieConfig, context);
            writer.writeEndElement();
        }

        // ELEMENT: trackingMode
        final List<TrackingMode> trackingMode = sessionConfig.trackingMode;
        if (trackingMode != null) {
            for (final TrackingMode trackingModeItem : trackingMode) {
                if (trackingModeItem != null) {
                    writer.writeStartElement(prefix, "tracking-mode", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(toStringTrackingMode(sessionConfig, null, context, trackingModeItem));
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(sessionConfig, LifecycleCallback.NONE);
    }

}

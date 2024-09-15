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
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.LifecycleCallback;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.Attribute;
import org.metatype.sxc.util.XoXMLStreamReader;
import org.metatype.sxc.util.XoXMLStreamWriter;


import static org.apache.openejb.jee.NamedMethod$JAXB.readNamedMethod;
import static org.apache.openejb.jee.NamedMethod$JAXB.writeNamedMethod;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;
import static org.apache.openejb.jee.TimerSchedule$JAXB.readTimerSchedule;
import static org.apache.openejb.jee.TimerSchedule$JAXB.writeTimerSchedule;

@SuppressWarnings({
    "StringEquality"
})
public class Timer$JAXB
    extends JAXBObject<Timer>
{

    private static final DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Unable to initialize DatatypeFactory", e);
        }
    }

    public Timer$JAXB() {
        super(Timer.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "timerType".intern()), Text$JAXB.class, TimerSchedule$JAXB.class, NamedMethod$JAXB.class);
    }

    public static Timer readTimer(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static void writeTimer(XoXMLStreamWriter writer, Timer timer, RuntimeContext context)
        throws Exception
    {
        _write(writer, timer, context);
    }

    public void write(XoXMLStreamWriter writer, Timer timer, RuntimeContext context)
        throws Exception
    {
        _write(writer, timer, context);
    }

    public static final Timer _read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        Timer timer = new Timer();
        context.beforeUnmarshal(timer, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType!= null) {
            if (("timerType"!= xsiType.getLocalPart())||("http://java.sun.com/xml/ns/javaee"!= xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, Timer.class);
            }
        }

        // Read attributes
        for (Attribute attribute: reader.getAttributes()) {
            if (("id" == attribute.getLocalName())&&(("" == attribute.getNamespace())||(attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, timer);
                timer.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI!= attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader: reader.getChildElements()) {
            if (("description" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<>();
                }
                descriptions.add(descriptionsItem);
            } else if (("schedule" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: schedule
                TimerSchedule schedule = readTimerSchedule(elementReader, context);
                timer.schedule = schedule;
            } else if (("start" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: start
                XMLGregorianCalendar start = datatypeFactory.newXMLGregorianCalendar(elementReader.getElementText());
                timer.start = start;
            } else if (("end" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: end
                XMLGregorianCalendar end = datatypeFactory.newXMLGregorianCalendar(elementReader.getElementText());
                timer.end = end;
            } else if (("timeout-method" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timeoutMethod
                NamedMethod timeoutMethod = readNamedMethod(elementReader, context);
                timer.timeoutMethod = timeoutMethod;
            } else if (("persistent" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: persistent
                Boolean persistent = ("1".equals(elementReader.getElementText())||"true".equals(elementReader.getElementText()));
                timer.persistent = persistent;
            } else if (("timezone" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: timezone
                String timezoneRaw = elementReader.getElementText();

                String timezone;
                try {
                    timezone = Adapters.collapsedStringAdapterAdapter.unmarshal(timezoneRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timer.timezone = timezone;
            } else if (("info" == elementReader.getLocalName())&&("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: info
                String infoRaw = elementReader.getElementText();

                String info;
                try {
                    info = Adapters.collapsedStringAdapterAdapter.unmarshal(infoRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timer.info = info;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "schedule"), new QName("http://java.sun.com/xml/ns/javaee", "start"), new QName("http://java.sun.com/xml/ns/javaee", "end"), new QName("http://java.sun.com/xml/ns/javaee", "timeout-method"), new QName("http://java.sun.com/xml/ns/javaee", "persistent"), new QName("http://java.sun.com/xml/ns/javaee", "timezone"), new QName("http://java.sun.com/xml/ns/javaee", "info"));
            }
        }
        if (descriptions!= null) {
            try {
                timer.setDescriptions(descriptions.toArray(new Text[descriptions.size()] ));
            } catch (Exception e) {
                context.setterError(reader, Timer.class, "setDescriptions", Text[].class, e);
            }
        }

        context.afterUnmarshal(timer, LifecycleCallback.NONE);

        return timer;
    }

    public final Timer read(XoXMLStreamReader reader, RuntimeContext context)
        throws Exception
    {
        return _read(reader, context);
    }

    public static final void _write(XoXMLStreamWriter writer, Timer timer, RuntimeContext context)
        throws Exception
    {
        if (timer == null) {
            writer.writeXsiNil();
            return ;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (Timer.class!= timer.getClass()) {
            context.unexpectedSubclass(writer, timer, Timer.class);
            return ;
        }

        context.beforeMarshal(timer, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = timer.id;
        if (idRaw!= null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(timer, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = timer.getDescriptions();
        } catch (Exception e) {
            context.getterError(timer, "descriptions", Timer.class, "getDescriptions", e);
        }
        if (descriptions!= null) {
            for (Text descriptionsItem: descriptions) {
                if (descriptionsItem!= null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(timer, "descriptions");
                }
            }
        }

        // ELEMENT: schedule
        TimerSchedule schedule = timer.schedule;
        if (schedule!= null) {
            writer.writeStartElement(prefix, "schedule", "http://java.sun.com/xml/ns/javaee");
            writeTimerSchedule(writer, schedule, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(timer, "schedule");
        }

        // ELEMENT: start
        XMLGregorianCalendar start = timer.start;
        if (start!= null) {
            writer.writeStartElement(prefix, "start", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(start.toXMLFormat());
            writer.writeEndElement();
        }

        // ELEMENT: end
        XMLGregorianCalendar end = timer.end;
        if (end!= null) {
            writer.writeStartElement(prefix, "end", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(end.toXMLFormat());
            writer.writeEndElement();
        }

        // ELEMENT: timeoutMethod
        NamedMethod timeoutMethod = timer.timeoutMethod;
        if (timeoutMethod!= null) {
            writer.writeStartElement(prefix, "timeout-method", "http://java.sun.com/xml/ns/javaee");
            writeNamedMethod(writer, timeoutMethod, context);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(timer, "timeoutMethod");
        }

        // ELEMENT: persistent
        Boolean persistent = timer.persistent;
        if (persistent!= null) {
            writer.writeStartElement(prefix, "persistent", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(Boolean.toString(persistent));
            writer.writeEndElement();
        }

        // ELEMENT: timezone
        String timezoneRaw = timer.timezone;
        String timezone = null;
        try {
            timezone = Adapters.collapsedStringAdapterAdapter.marshal(timezoneRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timer, "timezone", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (timezone!= null) {
            writer.writeStartElement(prefix, "timezone", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(timezone);
            writer.writeEndElement();
        }

        // ELEMENT: info
        String infoRaw = timer.info;
        String info = null;
        try {
            info = Adapters.collapsedStringAdapterAdapter.marshal(infoRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timer, "info", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (info!= null) {
            writer.writeStartElement(prefix, "info", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(info);
            writer.writeEndElement();
        }

        context.afterMarshal(timer, LifecycleCallback.NONE);
    }

}

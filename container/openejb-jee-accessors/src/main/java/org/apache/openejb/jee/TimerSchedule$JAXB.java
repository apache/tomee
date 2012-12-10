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

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;

@SuppressWarnings({
        "StringEquality"
})
public class TimerSchedule$JAXB
        extends JAXBObject<TimerSchedule> {


    public TimerSchedule$JAXB() {
        super(TimerSchedule.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "timer-scheduleType".intern()));
    }

    public static TimerSchedule readTimerSchedule(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeTimerSchedule(XoXMLStreamWriter writer, TimerSchedule timerSchedule, RuntimeContext context)
            throws Exception {
        _write(writer, timerSchedule, context);
    }

    public void write(XoXMLStreamWriter writer, TimerSchedule timerSchedule, RuntimeContext context)
            throws Exception {
        _write(writer, timerSchedule, context);
    }

    public final static TimerSchedule _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        TimerSchedule timerSchedule = new TimerSchedule();
        context.beforeUnmarshal(timerSchedule, LifecycleCallback.NONE);


        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("timer-scheduleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, TimerSchedule.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, timerSchedule);
                timerSchedule.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("second" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: second
                String secondRaw = elementReader.getElementAsString();

                String second;
                try {
                    second = Adapters.collapsedStringAdapterAdapter.unmarshal(secondRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.second = second;
            } else if (("minute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: minute
                String minuteRaw = elementReader.getElementAsString();

                String minute;
                try {
                    minute = Adapters.collapsedStringAdapterAdapter.unmarshal(minuteRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.minute = minute;
            } else if (("hour" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: hour
                String hourRaw = elementReader.getElementAsString();

                String hour;
                try {
                    hour = Adapters.collapsedStringAdapterAdapter.unmarshal(hourRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.hour = hour;
            } else if (("day-of-month" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dayOfMonth
                String dayOfMonthRaw = elementReader.getElementAsString();

                String dayOfMonth;
                try {
                    dayOfMonth = Adapters.collapsedStringAdapterAdapter.unmarshal(dayOfMonthRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.dayOfMonth = dayOfMonth;
            } else if (("month" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: month
                String monthRaw = elementReader.getElementAsString();

                String month;
                try {
                    month = Adapters.collapsedStringAdapterAdapter.unmarshal(monthRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.month = month;
            } else if (("day-of-week" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: dayOfWeek
                String dayOfWeekRaw = elementReader.getElementAsString();

                String dayOfWeek;
                try {
                    dayOfWeek = Adapters.collapsedStringAdapterAdapter.unmarshal(dayOfWeekRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.dayOfWeek = dayOfWeek;
            } else if (("year" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: year
                String yearRaw = elementReader.getElementAsString();

                String year;
                try {
                    year = Adapters.collapsedStringAdapterAdapter.unmarshal(yearRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                timerSchedule.year = year;
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "second"), new QName("http://java.sun.com/xml/ns/javaee", "minute"), new QName("http://java.sun.com/xml/ns/javaee", "hour"), new QName("http://java.sun.com/xml/ns/javaee", "day-of-month"), new QName("http://java.sun.com/xml/ns/javaee", "month"), new QName("http://java.sun.com/xml/ns/javaee", "day-of-week"), new QName("http://java.sun.com/xml/ns/javaee", "year"));
            }
        }

        context.afterUnmarshal(timerSchedule, LifecycleCallback.NONE);

        return timerSchedule;
    }

    public final TimerSchedule read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, TimerSchedule timerSchedule, RuntimeContext context)
            throws Exception {
        if (timerSchedule == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (TimerSchedule.class != timerSchedule.getClass()) {
            context.unexpectedSubclass(writer, timerSchedule, TimerSchedule.class);
            return;
        }

        context.beforeMarshal(timerSchedule, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = timerSchedule.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(timerSchedule, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: second
        String secondRaw = timerSchedule.second;
        String second = null;
        try {
            second = Adapters.collapsedStringAdapterAdapter.marshal(secondRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "second", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (second != null) {
            writer.writeStartElement(prefix, "second", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(second);
            writer.writeEndElement();
        }

        // ELEMENT: minute
        String minuteRaw = timerSchedule.minute;
        String minute = null;
        try {
            minute = Adapters.collapsedStringAdapterAdapter.marshal(minuteRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "minute", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (minute != null) {
            writer.writeStartElement(prefix, "minute", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(minute);
            writer.writeEndElement();
        }

        // ELEMENT: hour
        String hourRaw = timerSchedule.hour;
        String hour = null;
        try {
            hour = Adapters.collapsedStringAdapterAdapter.marshal(hourRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "hour", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (hour != null) {
            writer.writeStartElement(prefix, "hour", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(hour);
            writer.writeEndElement();
        }

        // ELEMENT: dayOfMonth
        String dayOfMonthRaw = timerSchedule.dayOfMonth;
        String dayOfMonth = null;
        try {
            dayOfMonth = Adapters.collapsedStringAdapterAdapter.marshal(dayOfMonthRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "dayOfMonth", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (dayOfMonth != null) {
            writer.writeStartElement(prefix, "day-of-month", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(dayOfMonth);
            writer.writeEndElement();
        }

        // ELEMENT: month
        String monthRaw = timerSchedule.month;
        String month = null;
        try {
            month = Adapters.collapsedStringAdapterAdapter.marshal(monthRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "month", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (month != null) {
            writer.writeStartElement(prefix, "month", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(month);
            writer.writeEndElement();
        }

        // ELEMENT: dayOfWeek
        String dayOfWeekRaw = timerSchedule.dayOfWeek;
        String dayOfWeek = null;
        try {
            dayOfWeek = Adapters.collapsedStringAdapterAdapter.marshal(dayOfWeekRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "dayOfWeek", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (dayOfWeek != null) {
            writer.writeStartElement(prefix, "day-of-week", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(dayOfWeek);
            writer.writeEndElement();
        }

        // ELEMENT: year
        String yearRaw = timerSchedule.year;
        String year = null;
        try {
            year = Adapters.collapsedStringAdapterAdapter.marshal(yearRaw);
        } catch (Exception e) {
            context.xmlAdapterError(timerSchedule, "year", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (year != null) {
            writer.writeStartElement(prefix, "year", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(year);
            writer.writeEndElement();
        }

        context.afterMarshal(timerSchedule, LifecycleCallback.NONE);
    }

}

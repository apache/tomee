/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.jee;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;

/**
 * @version $Rev$ $Date$
 */
public class StaxCompare {

    public static void compare(String a, String b) throws Exception {
        StringBuilder message = new StringBuilder();
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader rA = factory.createXMLEventReader(new StringReader(a));
        XMLEventReader rB = factory.createXMLEventReader(new StringReader(b));
        if (!compare(rA, rB, message)) {
            throw new Exception(message.toString());
        }
    }

    public static boolean compare(XMLEventReader a, XMLEventReader b, StringBuilder message) {
        XMLEvent eventA;
        XMLEvent eventB;
        int eventType;
        try {
        while ((eventA = nextInterestingEvent(a)) != null & (eventB = nextInterestingEvent(b)) != null) {
                if ((eventType = eventA.getEventType()) != eventB.getEventType()) {
                    message.append("events of different types: ").append(eventA).append(", ").append(eventB);
                    return false;
                }

                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    StartElement startA = eventA.asStartElement();
                    StartElement startB = eventB.asStartElement();
                    if (!startA.getName().getLocalPart().equals(startB.getName().getLocalPart())) {
                        message.append("Different elements ").append(startA.getName()).append(", ").append(startB.getName()).append(" at location ").append(eventA.getLocation());
                        return false;
                    }
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    EndElement endA = eventA.asEndElement();
                    EndElement endB = eventB.asEndElement();
                    if (!endA.getName().getLocalPart().equals(endB.getName().getLocalPart())) {
                        message.append("Different elements ").append(endA.getName()).append(", ").append(endB.getName()).append(" at location ").append(eventA.getLocation());
                        return false;
                    }
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    Characters endA = eventA.asCharacters();
                    Characters endB = eventB.asCharacters();
                    if (!endA.getData().equals(endB.getData())) {
                        message.append("Different content ").append(endA.getData()).append(", ").append(endB.getData()).append(" at location ").append(eventA.getLocation());
                        return false;
                    }
                }

        }
            if (eventA != null) {
                message.append("A is longer: ").append(eventA.getLocation());
                return false;
            }
            if (eventB != null) {
                message.append("B is longer: ").append(eventB.getLocation());
                return false;
            }
        } catch (XMLStreamException e) {
            message.append("Exception processing ").append(e.getMessage());
            return false;
        }
        return true;
    }

    private static XMLEvent nextInterestingEvent(XMLEventReader r) throws XMLStreamException {
        do {
            XMLEvent e = r.nextEvent();
            int t = e.getEventType();
            if (t == XMLStreamConstants.START_ELEMENT || t == XMLStreamConstants.END_ELEMENT || t == XMLStreamConstants.ATTRIBUTE) {
                return e;
            }
        } while (r.hasNext());
        return null;
    }
}

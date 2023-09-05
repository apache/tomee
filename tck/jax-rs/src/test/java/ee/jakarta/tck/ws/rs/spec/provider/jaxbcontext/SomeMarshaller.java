/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.spec.provider.jaxbcontext;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.attachment.AttachmentMarshaller;

@SuppressWarnings("rawtypes")
public class SomeMarshaller implements Marshaller {

  @Override
  public <A extends XmlAdapter<?, ?>> A getAdapter(Class<A> arg0) {
    return null;
  }

  @Override
  public AttachmentMarshaller getAttachmentMarshaller() {
    return null;
  }

  @Override
  public ValidationEventHandler getEventHandler() throws JAXBException {
    return null;
  }

  @Override
  public Listener getListener() {
    return null;
  }

  @Override
  public Node getNode(Object arg0) throws JAXBException {
    return null;
  }

  @Override
  public Object getProperty(String arg0) throws PropertyException {
    return null;
  }

  @Override
  public Schema getSchema() {
    return null;
  }

  @Override
  public void marshal(Object arg0, Result arg1) throws JAXBException {
    String value = objectToString(arg0);
    arg1.setSystemId(value + getClass().getSimpleName());
  }

  @Override
  public void marshal(Object arg0, OutputStream arg1) throws JAXBException {
    try {
      String value = objectToString(arg0);
      arg1.write("M1".getBytes());
      arg1.write(value.getBytes());
      arg1.write(getClass().getSimpleName().getBytes());
      arg1.flush();
    } catch (IOException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public void marshal(Object arg0, File arg1) throws JAXBException {
  }

  @Override
  public void marshal(Object jaxbElement, Writer writer) throws JAXBException {
    try {
      writer.write("M2");
      String value = objectToString(jaxbElement);
      writer.write(value);
      writer.write(getClass().getSimpleName());
    } catch (IOException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public void marshal(Object jaxbElement, ContentHandler handler)
      throws JAXBException {
    String value = objectToString(jaxbElement);
    try {
      handler.startDocument();
      Attributes attributes = new AttributesImpl();
      handler.startElement(value, value, getClass().getSimpleName(),
          attributes);
      handler.endElement(value, value, getClass().getSimpleName());
      handler.endDocument();
    } catch (SAXException se) {
      throw new JAXBException(se);
    }
  }

  @Override
  public void marshal(Object jaxbElement, Node node) throws JAXBException {
    String value = objectToString(jaxbElement);
    node.setNodeValue(value + getClass().getSimpleName());
  }

  @Override
  public void marshal(Object jaxbElement, XMLStreamWriter writer)
      throws JAXBException {
    try {
      String elementValue = objectToString(jaxbElement);
      writer.writeStartDocument();
      writer.writeStartElement(elementValue);
      writer.writeAttribute(elementValue, getClass().getSimpleName());
      writer.writeEndElement();
      writer.writeEndDocument();
      writer.flush();
    } catch (XMLStreamException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public void marshal(Object jaxbElement, XMLEventWriter writer)
      throws JAXBException {
    try {
      String value = objectToString(jaxbElement);
      writer.setPrefix(value, getClass().getSimpleName());
      writer.flush();
    } catch (XMLStreamException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public <A extends XmlAdapter<?, ?>> void setAdapter( A adapter ) {
  }

  @Override
  public <A extends XmlAdapter<?, ?>> void setAdapter( Class<A> type, A adapter ) {
  }

  @Override
  public void setAttachmentMarshaller(AttachmentMarshaller am) {
  }

  @Override
  public void setEventHandler(ValidationEventHandler handler)
      throws JAXBException {
  }

  @Override
  public void setListener(Listener listener) {
  }

  @Override
  public void setProperty(String name, Object value) throws PropertyException {
  }

  @Override
  public void setSchema(Schema schema) {
  }

  private static String objectToString(Object object) {
    String objectValue;
    if (object instanceof JAXBElement)
      objectValue = ((JAXBElement) object).getValue().toString();
    else
      objectValue = object.toString();
    return objectValue;
  }

}

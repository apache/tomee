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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.UnmarshallerHandler;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.attachment.AttachmentUnmarshaller;

@SuppressWarnings("rawtypes")
public class SomeUnmarshaller implements Unmarshaller {

  @Override
  public <A extends XmlAdapter<?, ?>> A getAdapter(Class<A> type) {
    return null;
  }

  @Override
  public AttachmentUnmarshaller getAttachmentUnmarshaller() {
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
  public Object getProperty(String name) throws PropertyException {
    return null;
  }

  @Override
  public Schema getSchema() {
    return null;
  }

  @Override
  public UnmarshallerHandler getUnmarshallerHandler() {
    return null;
  }

  @Override
  public void setAdapter(XmlAdapter adapter) {
  }

  @Override
  public <A extends XmlAdapter<?, ?>> void setAdapter(Class<A> type, A adapter) {
  }

  @Override
  public void setAttachmentUnmarshaller(AttachmentUnmarshaller au) {
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

  @Override
  public Object unmarshal(File f) throws JAXBException {
    try {
      FileReader fr = new FileReader(f);
      return unmarshal(fr);
    } catch (FileNotFoundException e) {
      throw new JAXBException(e);
    }

  }

  @Override
  public Object unmarshal(InputStream is) throws JAXBException {
    InputStreamReader isr = new InputStreamReader(is);
    return unmarshal(isr);
  }

  @Override
  public Object unmarshal(Reader reader) throws JAXBException {
    BufferedReader bf = new BufferedReader(reader);
    try {
      return bf.readLine();
    } catch (IOException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public Object unmarshal(URL url) throws JAXBException {
    try {
      return unmarshal(url.openStream());
    } catch (IOException e) {
      throw new JAXBException(e);
    }
  }

  @Override
  public Object unmarshal(InputSource source) throws JAXBException {
    return unmarshal((Source) null, String.class);
  }

  @Override
  public Object unmarshal(Node node) throws JAXBException {
    return node.toString();
  }

  @Override
  public Object unmarshal(Source source) throws JAXBException {
    return unmarshal((Source) null, String.class);
  }

  @Override
  public Object unmarshal(XMLStreamReader reader) throws JAXBException {
    return getClass().getSimpleName();
  }

  @Override
  public Object unmarshal(XMLEventReader reader) throws JAXBException {
    return getClass().getSimpleName();
  }

  @Override
  public <T> JAXBElement<T> unmarshal(Node node, Class<T> declaredType)
      throws JAXBException {
    return unmarshal((Source) null, declaredType);
  }

  @Override
  public <T> JAXBElement<T> unmarshal(Source source, Class<T> declaredType)
      throws JAXBException {
    String name = getClass().getSimpleName();
    @SuppressWarnings("unchecked")
    JAXBElement<T> el = new JAXBElement<T>(new QName(name), declaredType,
        (T) name);
    return el;
  }

  @Override
  public <T> JAXBElement<T> unmarshal(XMLStreamReader reader,
      Class<T> declaredType) throws JAXBException {
    return unmarshal((Source) null, declaredType);
  }

  @Override
  public <T> JAXBElement<T> unmarshal(XMLEventReader reader,
      Class<T> declaredType) throws JAXBException {
    return unmarshal((Source) null, declaredType);
  }

}

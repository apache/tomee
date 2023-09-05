/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.core.responsebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DateClientReaderWriter
    implements MessageBodyReader<Date>, MessageBodyWriter<Date> {
  private StringBuilder atom;

  public DateClientReaderWriter(StringBuilder atom) {
    super();
    this.atom = atom;
  }

  @Override
  public long getSize(Date arg0, Class<?> arg1, Type arg2, Annotation[] arg3,
      MediaType arg4) {
    return String.valueOf(Long.MAX_VALUE).length()
        + DateContainerReaderWriter.SPLITTER.length();
  }

  @Override
  public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return arg0 == Date.class;
  }

  @Override
  public void writeTo(Date date, Class<?> arg1, Type arg2, Annotation[] arg3,
      MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream stream)
      throws IOException, WebApplicationException {
    stream.write(dateToString(date).getBytes());
  }

  @Override
  public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3) {
    return isWriteable(arg0, arg1, arg2, arg3);
  }

  @Override
  public Date readFrom(Class<Date> arg0, Type arg1, Annotation[] arg2,
      MediaType arg3, MultivaluedMap<String, String> arg4, InputStream arg5)
      throws IOException, WebApplicationException {
    InputStreamReader reader = new InputStreamReader(arg5);
    BufferedReader br = new BufferedReader(reader);
    String data = br.readLine();
    String[] split = data == null ? new String[] { "0" }
        : data.split(DateContainerReaderWriter.SPLITTER);
    long date = Long.parseLong(split[0]);
    atom.append(split[1]);
    return new Date(date);
  }

  public static final String dateToString(Date date) {
    return String.valueOf(date.getTime());
  }
}

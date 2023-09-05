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

package ee.jakarta.tck.ws.rs.spec.provider.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

public abstract class AbstractReader
    implements MessageBodyReader<EntityForReader> {

  static StringBuilder readerSet = new StringBuilder();

  public static final MediaType NO_PROVIDER_MEDIATYPE = new MediaType("abc",
      "def");

  public static void resetSet() {
    readerSet = new StringBuilder();
  }

  public static void addClass(String string) {
    readerSet.append(string).append(";");
  }

  @Override
  public EntityForReader readFrom(Class<EntityForReader> type, Type genericType,
      Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    InputStreamReader isr = new InputStreamReader(entityStream);
    BufferedReader br = new BufferedReader(isr);
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName().toUpperCase()).append(";");
    if (mediaType != null)
      sb.append(mediaType.toString()).append(";");
    sb.append(br.readLine());
    br.close();
    sb.append(";").append(readerSet);
    return new EntityForReader(sb.toString());
  }
}

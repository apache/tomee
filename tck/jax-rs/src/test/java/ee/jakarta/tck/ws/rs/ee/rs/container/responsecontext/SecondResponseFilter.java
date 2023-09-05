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

package ee.jakarta.tck.ws.rs.ee.rs.container.responsecontext;

import java.lang.annotation.Annotation;

import jakarta.annotation.Priority;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(100)
// reverse order, should be second
public class SecondResponseFilter extends TemplateFilter {
  @Override
  protected void operationMethodNotFound(String operation) {
    // the check is to apply on ResponseFilter only
    // here, it is usually not found.
  }

  public void getHeadersIsMutable() {
    MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    if (assertTrue(headers.containsKey(HEADER), HEADER,
        "header is not in header map"))
      return;
    setEntity(HEADER + " found as expected");
  }

  public void setEntity() {
    MediaType type = responseContext.getMediaType();
    if (assertTrue(MediaType.APPLICATION_SVG_XML_TYPE.equals(type),
        "Unexpected mediatype", type))
      return;

    Annotation[] annotations = responseContext.getEntityAnnotations();
    for (Annotation annotation : annotations) {
      Class<?> clazz = annotation.annotationType();
      if (assertTrue(clazz == Provider.class || clazz == Priority.class,
          "Annotation", clazz, "was unexpected"))
        return;
    }
  }
}

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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.reader.readerinterceptorcontext;

import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import ee.jakarta.tck.ws.rs.api.rs.ext.interceptor.TemplateInterceptorBody;
import ee.jakarta.tck.ws.rs.common.provider.StringBean;

public class ExceptionThrowingStringBean extends StringBean {

  public ExceptionThrowingStringBean(String header) {
    super(header);
    if (!header.equals(TemplateInterceptorBody.WAE))
      throw new WebApplicationException(new IOException(header));
  }

  public ExceptionThrowingStringBean(String header, boolean b) {
    super(header);
  }
}

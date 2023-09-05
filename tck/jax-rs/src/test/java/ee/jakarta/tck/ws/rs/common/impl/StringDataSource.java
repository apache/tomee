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

package ee.jakarta.tck.ws.rs.common.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import jakarta.activation.DataSource;
import jakarta.ws.rs.core.MediaType;

public class StringDataSource implements DataSource {

  private String value;

  private MediaType mediaType;

  public StringDataSource(String value, MediaType mediaType) {
    super();
    this.value = value;
    this.mediaType = mediaType;
  }

  @Override
  public String getContentType() {
    return mediaType.getType() + "/" + mediaType.getSubtype();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(value.getBytes());
    return bais;
  }

  @Override
  public String getName() {
    return value;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(value.length());
    OutputStreamWriter osw = new OutputStreamWriter(baos);
    osw.write(value);
    return baos;
  }

}

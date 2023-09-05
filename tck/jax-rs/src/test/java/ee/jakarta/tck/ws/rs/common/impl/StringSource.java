/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.transform.stream.StreamSource;

public class StringSource extends StreamSource {

  private String source;

  @Override
  public String getSystemId() {
    return source;
  }

  public StringSource(String source) {
    super();
    this.source = "<stringsource>" + source + "</stringsource>";
  }

  @Override
  public void setSystemId(String arg0) {
    this.source = arg0;
  }

  @Override
  public InputStream getInputStream() {
    ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes());
    return bais;
  }

  @Override
  public Reader getReader() {
    InputStreamReader reader = new InputStreamReader(getInputStream());
    return reader;
  }

  @Override
  public String getPublicId() {
    return getSystemId();
  }

}

/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.ext.messagebodyreaderwriter;

/**
 * This class is used by
 * ee.jakarta.tck.ws.rs.ee.rs.ext.providers.ProvidersServlet
 */
public class ReadableWritableEntity {
  private String entity;

  public static final String NAME = "READABLEWRITEABLE";

  private static final String PREFIX = "<" + NAME + ">";

  private static final String SUFFIX = "</" + NAME + ">";

  public ReadableWritableEntity(String entity) {
    this.entity = entity;
  }

  public String toXmlString() {
    StringBuilder sb = new StringBuilder();
    sb.append(PREFIX).append(entity).append(SUFFIX);
    return sb.toString();
  }

  @Override
  public String toString() {
    return entity;
  }

  public static ReadableWritableEntity fromString(String stream) {
    String entity = stream.replaceAll(PREFIX, "").replaceAll(SUFFIX, "");
    return new ReadableWritableEntity(entity);
  }
}

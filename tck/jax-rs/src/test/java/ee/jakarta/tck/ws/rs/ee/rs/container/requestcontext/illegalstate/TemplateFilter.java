/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext.illegalstate;

import java.util.Collection;

import jakarta.ws.rs.container.ContainerRequestContext;

public class TemplateFilter {

  public static final String NOEXCEPTION = "No exception has been thrown";

  public static final String ISEXCEPTION = "IllegalSteateException has been thrown";

  public static final String URI = "http://xx.yy:888/base/resource/sub";

  public static final String OPERATION = "OPERATION";

  protected String entity = null;

  protected ContainerRequestContext requestContext;

  protected static <T> String collectionToString(Collection<T> collection) {
    StringBuilder sb = new StringBuilder();
    for (T item : collection) {
      String replace = item.toString().toLowerCase().replace("_", "-")
          .replace(" ", "");
      sb.append(replace).append(" ");
    }
    return sb.toString();
  }

  protected void setEntity(String entity) {
    this.entity = entity;
  }

}

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

package ee.jakarta.tck.ws.rs.spec.provider.standardhaspriority;

public abstract class AbstractProvider {
  public long getLength() {
    String name = getClass().getSimpleName().replace("Provider", "");
    long size = "writer".length() + name.length();
    return 2 * size;
  }

  public String getWriterName() {
    String name = getClass().getSimpleName().replace("Provider", "Writer");
    return name;
  }

  public String getReaderName() {
    String name = getClass().getSimpleName().replace("Provider", "Reader");
    return name;
  }

}

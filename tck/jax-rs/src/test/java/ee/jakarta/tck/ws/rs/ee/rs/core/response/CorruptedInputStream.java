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

package ee.jakarta.tck.ws.rs.ee.rs.core.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class CorruptedInputStream extends ByteArrayInputStream {

  AtomicInteger atomic;

  private boolean corrupted = false;

  public static final int CLOSEVALUE = 999;

  public static final String IOETEXT = "CorruptedInputStream tck test IOException";

  public CorruptedInputStream(byte[] buf, AtomicInteger atomic) {
    super(buf);
    this.atomic = atomic;
  }

  @Override
  public void close() throws IOException {
    if (corrupted) {
      atomic.set(CLOSEVALUE);
      throw new IOException(IOETEXT);
    }
    super.close();
  }

  public boolean isCorrupted() {
    return corrupted;
  }

  public void setCorrupted(boolean corrupted) {
    this.corrupted = corrupted;
  }

}

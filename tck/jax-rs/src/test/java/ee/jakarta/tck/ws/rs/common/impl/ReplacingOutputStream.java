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

import java.io.IOException;
import java.io.OutputStream;

public class ReplacingOutputStream extends OutputStream {
  protected OutputStream wrappedStream;

  protected char what;

  protected char replace;

  protected String newMessage;

  public ReplacingOutputStream(OutputStream wrappedStream, char what,
      char replace) {
    super();
    this.wrappedStream = wrappedStream;
    this.what = what;
    this.replace = replace;
    this.newMessage = null;
  }

  public ReplacingOutputStream(OutputStream wrappedStream, String newMessage) {
    super();
    this.wrappedStream = wrappedStream;
    this.what = 0;
    this.replace = 0;
    this.newMessage = newMessage;
  }

  @Override
  public void write(int b) throws IOException {
    write(intToByteArray(b));
  }

  @Override
  public void write(byte[] b) throws IOException {
    String old = new String(b);
    String nw = null;
    if (what != 0 && replace != 0)
      nw = old.replace(what, replace);
    if (newMessage != null)
      nw = newMessage;
    wrappedStream.write(nw.getBytes());
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    write(b);
  }

  /**
   * This is a hack for ascii characters only. The correct function should
   * convert other bytes as well when appropriate.
   */
  public static final byte[] intToByteArray(int value) {
    return new byte[] { (byte) (value & 0xff) };
  }

  @Override
  public void close() throws IOException {
    if (wrappedStream != null) {
      wrappedStream.close();
    }
  }

  @Override
  public void flush() throws IOException {
    if (wrappedStream != null) {
      wrappedStream.flush();
    }
  }
}

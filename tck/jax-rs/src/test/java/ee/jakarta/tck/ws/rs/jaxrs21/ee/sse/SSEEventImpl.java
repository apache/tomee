/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.jaxrs21.ee.sse;

import jakarta.ws.rs.sse.SseEvent;

public abstract class SSEEventImpl implements SseEvent {

  public SSEEventImpl(String comment, String id, String name, int delay) {
    super();
    this.comment = comment;
    this.id = id;
    this.name = name;
    this.delay = delay;
  }

  public SSEEventImpl() {
    super();
  }

  public int getDelay() {
    return delay;
  }

  public SSEEventImpl setDelay(int delay) {
    this.delay = delay;
    return this;
  }

  public SSEEventImpl setId(String id) {
    this.id = id;
    return this;
  }

  public SSEEventImpl setName(String name) {
    this.name = name;
    return this;
  }

  public SSEEventImpl setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public String id;

  public String name;

  public int delay = 0;

  public String comment;

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getReconnectDelay() {
    return delay;
  }
}

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

import java.lang.reflect.Type;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;

public class OutboundSSEEventImpl extends SSEEventImpl
    implements OutboundSseEvent {
  public Class<?> type = String.class;

  public Type genericType = new GenericType<String>(type) {
  }.getType();

  public Object data = null;

  public MediaType media = MediaType.TEXT_PLAIN_TYPE;

  public OutboundSSEEventImpl(Object data) {
    setData(data);
  }

  public OutboundSSEEventImpl(Object data, String comment, String id,
      String name, int delay) {
    super(comment, id, name, delay);
    setData(data);
  }

  @Override
  public boolean isReconnectDelaySet() {
    return delay != 0;
  }

  @Override
  public Class<?> getType() {
    return type;
  }

  @Override
  public Type getGenericType() {
    return type;
  }

  @Override
  public MediaType getMediaType() {
    return media;
  }

  @Override
  public Object getData() {
    return data;
  }

  public OutboundSSEEventImpl setType(Class<?> type) {
    this.type = type;
    return this;
  }

  public OutboundSSEEventImpl setGenericType(Type genericType) {
    this.genericType = genericType;
    return this;
  }

  public OutboundSSEEventImpl setData(Object data) {
    this.data = data;
    return this;
  }

  public OutboundSSEEventImpl setData(Object data, MediaType type) {
    this.data = data;
    return this;
  }
}

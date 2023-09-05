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

package ee.jakarta.tck.ws.rs.spec.provider.writer;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Link.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

public class OkResponse extends Response {

  Response r;

  public OkResponse(String entity) {
    r = Response.ok(new EntityForWriter(entity)).build();
  }

  @Override
  public Object getEntity() {
    return r.getEntity();
  }

  @Override
  public int getStatus() {
    return r.getStatus();
  }

  @Override
  public MultivaluedMap<String, Object> getMetadata() {
    return r.getMetadata();
  }

  @Override
  public boolean hasEntity() {
    return r.hasEntity();
  }

  @Override
  public Set<String> getAllowedMethods() {
    return r.getAllowedMethods();
  }

  @Override
  public Map<String, NewCookie> getCookies() {
    return r.getCookies();
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public EntityTag getEntityTag() {
    return r.getEntityTag();
  }

  @Override
  public String getHeaderString(String arg0) {
    return r.getHeaderString(arg0);
  }

  @Override
  public Locale getLanguage() {
    return r.getLanguage();
  }

  @Override
  public Date getLastModified() {
    return r.getDate();
  }

  @Override
  public int getLength() {
    return r.getLength();
  }

  @Override
  public Link getLink(String arg0) {
    return r.getLink(arg0);
  }

  @Override
  public Builder getLinkBuilder(String arg0) {
    return r.getLinkBuilder(arg0);
  }

  @Override
  public Set<Link> getLinks() {
    return r.getLinks();
  }

  @Override
  public URI getLocation() {
    return r.getLocation();
  }

  @Override
  public MediaType getMediaType() {
    return r.getMediaType();
  }

  @Override
  public StatusType getStatusInfo() {
    return r.getStatusInfo();
  }

  @Override
  public boolean hasLink(String arg0) {
    return r.hasLink(arg0);
  }

  @Override
  public boolean bufferEntity() {
    return r.bufferEntity();
  }

  @Override
  public void close() {
    r.close();
  }

  @Override
  public <T> T readEntity(Class<T> arg0) throws IllegalStateException {
    return r.readEntity(arg0);
  }

  @Override
  public <T> T readEntity(GenericType<T> arg0) throws IllegalStateException {
    return r.readEntity(arg0);
  }

  @Override
  public <T> T readEntity(Class<T> arg0, Annotation[] arg1)
      throws IllegalStateException {
    return r.readEntity(arg0, arg1);
  }

  @Override
  public <T> T readEntity(GenericType<T> arg0, Annotation[] arg1)
      throws IllegalStateException {
    return r.readEntity(arg0, arg1);
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders() {
    return r.getStringHeaders();
  }

}

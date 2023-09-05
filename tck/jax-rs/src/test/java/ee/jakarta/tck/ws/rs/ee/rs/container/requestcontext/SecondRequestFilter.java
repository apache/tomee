/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.rs.container.requestcontext;

import java.security.Principal;
import java.util.Collection;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

/**
 * The methods are called here by reflection from the superclass This is a
 * second filter, the lower priority filter, only a few requests will pass here,
 * rest is handled in higher priority filter.
 */
@Provider
@Priority(500)
@PreMatching
public class SecondRequestFilter extends TemplateFilter {

  public void abortWith() {
    throw new RuntimeException("The filter chain has not been broken");
  }

  public void getHeadersIsMutable() {
    String key = "KEY";
    MultivaluedMap<String, String> headers = requestContext.getHeaders();
    if (assertTrue(headers.containsKey(key), "Key", key, "not found"))
      return;
    abortWithEntity("#getHeaders() is mutable as expected");
  }

  public void setProperty() {
    Object o = requestContext.getProperty(PROPERTYNAME);
    // If the name specified
    // is already used for a property, this method will replace
    // the value of the property with the new value.
    if (assertTrue(o.equals(PROPERTYNAME + PROPERTYNAME), o, "is unexpected"))
      return;
    requestContext.setProperty(PROPERTYNAME, PROPERTYNAME);
    o = requestContext.getProperty(PROPERTYNAME);
    abortWithEntity(o == null ? "NULL" : o.toString());
  }

  public void setPropertyContext() {
    // servletRequest injection not working here
  }

  public void getPropertyNames() {
    Collection<String> names = requestContext.getPropertyNames();
    if (assertTrue(names.size() >= 5,
        "#getPropertyNames has unexpected number of elements", names.size(),
        "{", collectionToString(names), "}"))
      return;
    abortWithEntity(collectionToString(names));
  }

  public void setSecurityContext() {
    SecurityContext secCtx = requestContext.getSecurityContext();
    Principal principal = secCtx.getUserPrincipal();
    if (assertTrue(principal != null, "injected principal is null"))
      return;
    abortWithEntity(principal.getName());
  }

  public void setMethod() {
    // void, just do not return Operation SETMETHOD not implemented
  }

  public void setRequestUri1() {
    // void, just do not return Operation SETREQUESTURI1 not implemented
  }

}

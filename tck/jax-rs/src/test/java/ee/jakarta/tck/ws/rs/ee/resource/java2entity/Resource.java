/*
 * Copyright (c) 2012, 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.ee.resource.java2entity;

import java.lang.reflect.Method;
import java.util.LinkedList;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

@Path("resource")
public class Resource {

  @Path("linkedlist")
  @GET
  public LinkedList<String> checkDirect() {
    LinkedList<String> list = new LinkedList<String>();
    list.add("linked");
    list.add("list");
    return list;
  }

  @Path("response/linkedlist")
  @GET
  public Response checkResponseDirect() {
    LinkedList<String> list = new LinkedList<String>();
    list.add("linked");
    list.add("list");
    return Response.ok(list).build();
  }

  @Path("response/genericentity/linkedlist")
  @GET
  public Response checkResponseGeneric() {
    GenericEntity<LinkedList<String>> gells = checkGeneric();
    return Response.ok(gells).build();
  }

  @Path("genericentity/linkedlist")
  @GET
  public GenericEntity<LinkedList<String>> checkGeneric() {
    LinkedList<String> list = new LinkedList<String>();
    list.add("linked");
    list.add("list");
    GenericEntity<LinkedList<String>> gells;
    gells = new GenericEntity<LinkedList<String>>(list,
        getMethodByName("checkDirect").getGenericReturnType());
    return gells;
  }

  private Method getMethodByName(String name) {
    try {
      Method method = getClass().getMethod(name);
      return method;
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }
}
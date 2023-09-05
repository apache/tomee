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

package ee.jakarta.tck.ws.rs.api.rs.ext.interceptor;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.ws.rs.ext.InterceptorContext;
import ee.jakarta.tck.ws.rs.common.util.JaxrsUtil;

/**
 * Body for both reader and writer interceptor body The body is injected into
 * Template<Reader/Writer>Interceptor
 * 
 * @param <CONTEXT>
 *          The context the body methods will use.
 */
public abstract class TemplateInterceptorBody<CONTEXT extends InterceptorContext> {
  public static final String OPERATION = "OPERATION";

  public static final String ENTITY = "Entity";

  public static final String ENTITY2 = "Other Entity";

  public static final String PROPERTY = "Property";

  public static final String NULL = "None";

  public static final String NPE = "NullPointerException has been thrown as expected";

  public static final String IOE = "IOException has been thrown as expected";

  public static final String WAE = "WebApplicationException has been thrown as expected";

  protected CONTEXT context;

  protected InterceptorCallbackMethods callback;

  public Object executeMethod(CONTEXT context,
      InterceptorCallbackMethods callback) throws IOException {
    this.context = context;
    this.callback = callback;
    String operation = getHeaderString();
    Method[] methods = getClass().getMethods();
    for (Method method : methods)
      if (operation.equalsIgnoreCase(method.getName())) {
        try {
          Object ret = method.invoke(this);
          if (ret == null)
            return proceed();
          else
            return ret; // the method ensures the call of proceed();
        } catch (Exception e) {
          if (IOException.class.isInstance(e.getCause()))
            throw (IOException) e.getCause();
          else if (e.getCause() instanceof RuntimeException)
            throw (RuntimeException) e.getCause();
          else
            throwIOEAndLog(operation, e);
        }
      }
    return operationMethodNotFound(operation);
  }

  private static void throwIOEAndLog(String operation, Throwable e)
      throws IOException {
    System.out.println("Error while invoking method " + operation);
    e.printStackTrace();
    throw new IOException(e);
  }

  protected Object operationMethodNotFound(String operation)
      throws IOException {
    throw new IOException("Operation " + operation + " not implemented");
  }

  // ////////////////////////////////////////////////////////////////////
  protected void assertTrue(boolean conditionTrue, Object... msg) {
    if (conditionTrue)
      return;
    StringBuilder sb = new StringBuilder();
    if (msg != null)
      for (Object str : msg)
        sb.append(str).append(" ");
    throw new RuntimeException(new IOException(sb.toString()));
  }

  protected <T> void setEntity(T... entity) {
    setSeparatedEntity(" ", entity);
  }

  protected <T> void setSeparatedEntity(String separator, T... entity) {
    String complete = JaxrsUtil.iterableToString(separator, entity);
    writeEntity(complete);
  }

  protected void writeEntity(String entity) {
    callback.writeEntity(entity);
  }

  protected Object proceed() throws IOException {
    return callback.proceed();
  }

  protected String getHeaderString() {
    return callback.getHeaderString();
  }

}

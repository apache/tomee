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

package ee.jakarta.tck.ws.rs.spec.client.webtarget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient.Fault;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/**
 * A RandomAccess collection of objects upon each a certain method is called
 * creating object of the same type. Created objects are put into the collection
 * after the method is called on each item in the collection
 */
public class IteratedList<T> extends ArrayList<T> {
  private static final long serialVersionUID = 6628004256042593971L;

  protected Class<T> clazz;

  private List<T> newTs = new LinkedList<T>();

  public IteratedList(Class<T> clazz) {
    super();
    this.clazz = clazz;
  }

  /**
   * For each member of the collection call given method with arguments Add
   * newly created items into a collection afterwards
   */
  public void doWithAll(String methodName, Object... args) throws Fault {
    doLog(methodName, args);
    for (T t : this)
      callMethodWithArgs(t, methodName, args);
    addAll(newTs);
    newTs.clear();
  }

  protected void doLog(String methodName, Object... args) {
    StringBuilder sb = new StringBuilder();
    for (Object arg : args)
      sb.append(arg);
    TestUtil.logMsg("Testing method " + methodName + "(" + sb.toString() + ")");
  }

  protected void callMethodWithArgs(T t, String methodName, Object... args)
      throws Fault {
    Method m = findTargetMethodByNameAndArgs(methodName, args);
    try {
      Object o = m.invoke(t, args);
      if (o != null) {
        T newt = clazz.cast(o);
        newTs.add(newt);
      }
    } catch (Exception e) {
      throw new Fault(e);
    }
  }

  protected Method findTargetMethodByNameAndArgs(String name, Object... args)
      throws Fault {
    Method[] ms = clazz.getMethods();
    for (Method m : ms)
      if (m.getName().equals(name))
        if (args.length == m.getParameterTypes().length)
          if (isMethodOfArguments(m, args))
            return m;
    throw new Fault("Method " + name + " not found");
  }

  protected boolean isMethodOfArguments(Method m, Object... args) {
    boolean is = true;
    Class<?>[] types = m.getParameterTypes();
    for (int i = 0; i != args.length; i++)
      if (!types[i].isAssignableFrom(args[i].getClass()))
        is = false;
    return is;
  }

}

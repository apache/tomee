/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved.
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

package ee.jakarta.tck.ws.rs.common.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Hold multiple instances of TYPE in a {@link LinkedList} structure, last one
 * accessible by {@link #get()}.
 * 
 * @param <TYPE>
 */
public class LinkedHolder<TYPE> extends Holder<TYPE> implements Iterable<TYPE> {

  private LinkedList<TYPE> list = new LinkedList<TYPE>();

  public LinkedHolder(TYPE type) {
    add(type);
  }

  public LinkedHolder() {
  }

  public void add(TYPE value) {
    list.add(value);
    super.set(value);
  }

  /**
   * Replace the last item in the list
   */
  @Override
  public void set(TYPE value) {
    if (list.size() != 0) {
      list.set(list.size() - 1, value);
      super.set(value);
    }
    if (value == null) {
      super.set(null);
    } else {
      add(value);
    }
  }

  public TYPE get(int index) {
    if (index >= list.size())
      return null;
    return list.get(index);
  }

  public int size() {
    return list.size();
  }

  public void clear() {
    super.set(null);
    list.clear();
  }

  @Override
  public Iterator<TYPE> iterator() {
    return list.iterator();
  }

  public LinkedList<TYPE> asList() {
    return list;
  }

}

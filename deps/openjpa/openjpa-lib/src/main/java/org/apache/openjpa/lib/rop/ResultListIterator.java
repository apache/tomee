/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.rop;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Wrapper iterator that will return false for <code>hasNext()</code> if
 * the owning ResultList has been closed.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class ResultListIterator extends AbstractListIterator {

    private static final Localizer _loc = Localizer.forPackage(
        ResultListIterator.class);

    private final ListIterator _li;
    private final ResultList _rl;

    public ResultListIterator(ListIterator li, ResultList rl) {
        _li = li;
        _rl = rl;
    }

    public ResultList getResultList() {
        return _rl;
    }

    public boolean hasNext() {
        if (_rl.isClosed())
            return false;
        return _li.hasNext();
    }

    public boolean hasPrevious() {
        return _li.hasPrevious();
    }

    public Object next() {
        if (_rl.isClosed())
            throw new NoSuchElementException(_loc.get("closed").getMessage());
        return _li.next();
    }

    public int nextIndex() {
        return _li.nextIndex();
    }

    public Object previous() {
        return _li.previous();
    }

    public int previousIndex() {
        return _li.previousIndex();
    }
}


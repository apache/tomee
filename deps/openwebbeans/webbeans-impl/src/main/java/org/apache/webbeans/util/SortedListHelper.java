/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.util;

import java.util.List;
import java.util.Comparator;

public class SortedListHelper<E>
{

    private List<E> list;
    private Comparator<E> comparator;

    public SortedListHelper(List<E>list, Comparator<E> comparator) 
    {
        this.list = list;
        this.comparator = comparator;
    }

    public List<E> getList() 
    {
        return list;
    }

    public boolean add(E object)
    {
        if (list.isEmpty())
        {
            list.add(object);
            return true;
        }
        for(int i=0; i<list.size(); i++)
        {
            E obj = list.get(i);
            if (comparator.compare(object, obj) < 0) 
            {
                list.add(i, object);
                return true;
            }
        }
        return list.add(object);
    }

    public void clear() 
    {
        list.clear();
    }
}

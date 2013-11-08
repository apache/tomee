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
package org.apache.openjpa.persistence.generationtype;

import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;

public class CustomSeq implements Seq {

    private int i = 1;

    public void setType(int type) {
        if (type == Seq.TYPE_TRANSACTIONAL)
            throw new UnsupportedOperationException();
    }

    public Object next(StoreContext ctx, ClassMetaData cls) {
        return i++;
    }

    public Object current(StoreContext ctx, ClassMetaData cls) {
        return i;
    }

    public void allocate(int additional, StoreContext ctx, ClassMetaData cls) {
    }

    public void close() {
    }
}

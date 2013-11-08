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
package org.apache.openjpa.kernel.exps;

import java.util.Date;

import org.apache.openjpa.kernel.StoreContext;

/**
 * Represents the current date.
 *
 * @author Marc Prud'hommeaux
 */
class CurrentDate
    extends Val {
    private final Class<? extends Date> _type;
    
    public CurrentDate(Class<? extends Date> type) {
        _type = type;
    }
    
    public Class getType() {
        return _type;
    }

    public void setImplicitType(Class type) {
    }

    protected Object eval(Object candidate, Object orig, StoreContext ctx, Object[] params) {
        try {
            _type.getConstructor(long.class).newInstance(System.currentTimeMillis());
        } catch (Exception e) {
            return new Date();
        }
        return null;
    }
}

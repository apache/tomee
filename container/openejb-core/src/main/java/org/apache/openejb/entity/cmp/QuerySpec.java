/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;

import java.io.Serializable;

import org.apache.openejb.dispatch.MethodSignature;


/**
 * @version $Revision$ $Date$
 */
public class QuerySpec implements Serializable {
    private static final long serialVersionUID = 1267809494367656787L;
    private final MethodSignature methodSignature;
    private String ejbQl;
    private boolean local;
    private boolean flushCacheBeforeQuery;
    private String prefetchGroup;

    public QuerySpec(MethodSignature methodSignature) {
        this.methodSignature = methodSignature;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    public String getEjbQl() {
        return ejbQl;
    }

    public void setEjbQl(String ejbQl) {
        this.ejbQl = ejbQl;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public boolean isFlushCacheBeforeQuery() {
        return flushCacheBeforeQuery;
    }

    public void setFlushCacheBeforeQuery(boolean flushCacheBeforeQuery) {
        this.flushCacheBeforeQuery = flushCacheBeforeQuery;
    }

    public String getPrefetchGroup() {
        return prefetchGroup;
    }

    public void setPrefetchGroup(String prefetchGroup) {
        this.prefetchGroup = prefetchGroup;
    }

    public boolean equals(Object obj) {
        if (obj instanceof QuerySpec) {
            QuerySpec cmpSchema = (QuerySpec) obj;
            return methodSignature.equals(cmpSchema.methodSignature);
        }
        return false;
    }

    public String toString() {
        return methodSignature.toString();
    }
}
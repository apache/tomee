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

import java.util.List;


/**
 * @version $Revision$ $Date$
 */
public class TranqlQuery implements Query {
    private final org.tranql.dynamicquery.Query query;
    
    public TranqlQuery(org.tranql.dynamicquery.Query query) {
        this.query = query;
    }

    public List getResultList() {
        return query.getResultList();
    }

    public Object getSingleResult() {
        return query.getSingleResult();
    }

    public Query setParameter(int index, Object value) {
        query.setParameter(index, value);
        return this;
    }

    public Query setParameters(Object[] values) {
        query.setParameters(values);
        return this;
    }
}

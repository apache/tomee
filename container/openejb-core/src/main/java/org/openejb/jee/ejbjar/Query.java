/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Revision$ $Date$
 */
public class Query {
    private String id;
    private List<String> description = new ArrayList<String>();
    private QueryMethod queryMethod;
    private ResultTypeMapping resultTypeMapping;
    private String ejbQl;

    public Query() {
    }

    public Query(QueryMethod queryMethod, ResultTypeMapping resultTypeMapping, String ejbQl) {
        this.queryMethod = queryMethod;
        this.resultTypeMapping = resultTypeMapping;
        this.ejbQl = ejbQl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    public void setQueryMethod(QueryMethod queryMethod) {
        this.queryMethod = queryMethod;
    }

    public ResultTypeMapping getResultTypeMapping() {
        return resultTypeMapping;
    }

    public void setResultTypeMapping(ResultTypeMapping resultTypeMapping) {
        this.resultTypeMapping = resultTypeMapping;
    }

    public String getEjbQl() {
        return ejbQl;
    }

    public void setEjbQl(String ejbQl) {
        this.ejbQl = ejbQl;
    }
}

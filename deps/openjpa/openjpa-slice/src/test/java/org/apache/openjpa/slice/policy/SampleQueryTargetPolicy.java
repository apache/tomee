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
package org.apache.openjpa.slice.policy;

import java.util.List;
import java.util.Map;

import org.apache.openjpa.slice.QueryTargetPolicy;
import org.apache.openjpa.slice.TestQueryTargetPolicy;

/**
 * A sample query target policy used to {@linkplain TestQueryTargetPolicy}.
 * 
 * @author Pinaki Poddar
 *
 */
public class SampleQueryTargetPolicy implements QueryTargetPolicy {
    public String[] getTargets(String query, Map<Object, Object> params, 
            String language, List<String> slices,
            Object context) {
        if (TestQueryTargetPolicy.QueryPersonByName.equals(query)) {
            if ("Even".equals(params.get("name")))
                    return new String[]{"Even"};
            if ("Odd".equals(params.get("name")))
                return new String[]{"Odd"};
        }
        if (TestQueryTargetPolicy.QueryPersonByNameSwap.equals(query)) {
            if ("Even".equals(params.get("name")))
                    return new String[]{"Odd"};
            if ("Odd".equals(params.get("name")))
                return new String[]{"Even"};
        }
        return slices.toArray(new String[slices.size()]);
    }

}

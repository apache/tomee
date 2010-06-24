/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.jee;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @version $Rev$ $Date$
 */
public class LoadOnStartupAdapter extends XmlAdapter<String, Integer> {
    @Override
    public Integer unmarshal(String value) throws Exception {
        if (value.equalsIgnoreCase("true")) {
            return 1;
        }
        if (value.equalsIgnoreCase("false")) {
            return null;
        }
        return Integer.valueOf(value);
    }

    @Override
    public String marshal(Integer v) throws Exception {
        return v.toString();
    }
}

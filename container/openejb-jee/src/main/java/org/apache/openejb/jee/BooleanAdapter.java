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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @version $Rev$ $Date$
 */
public class BooleanAdapter extends XmlAdapter<String, Boolean> {

    public Boolean unmarshal(String value) throws Exception {
        value = value.trim();
        if (value.equals("1") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) {
            return Boolean.TRUE;
        } else if (value.equals("0") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("no")) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("String \"" + value + "\" is not a valid boolean value");
        }
    }

    public String marshal(Boolean value) throws Exception {
        if (value == null) {
            return null;
        }
        return (value.booleanValue()) ? "true" : "false";
    }
}

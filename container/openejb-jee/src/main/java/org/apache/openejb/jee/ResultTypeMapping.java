/**
 *
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

import javax.xml.bind.annotation.XmlEnumValue;


/**
 * The result-type-mappingType is used in the query element to
 * specify whether an abstract schema type returned by a query
 * for a select method is to be mapped to an EJBLocalObject or
 * EJBObject type.
 * <p/>
 * The value must be one of the following:
 * <p/>
 * Local
 * Remote
 */
public enum ResultTypeMapping {
    @XmlEnumValue("Local") LOCAL,
    @XmlEnumValue("Remote") REMOTE;
}

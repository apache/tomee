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
package org.apache.openejb.server.axis.assembler;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class XmlTypeInfo {
    public QName qname;
    public boolean anonymous;
    public QName elementQName;

    public QName xmlType;
    public boolean simpleType;
    public QName baseType;

    public boolean enumType;
    public boolean listType;
    public boolean restriction;
    public QName arrayComponentType;

    public final Map<String, QName> attributeTypes = new HashMap<String,QName>();
    public final Map<QName, XmlNestedElementInfo> nestedElements = new HashMap<QName, XmlNestedElementInfo>();
}

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
import java.util.ArrayList;
import java.util.Collection;

public class JaxRpcTypeInfo {
    //
    // QName is either the real QName or the generated anonymous name
    //
    public QName qname;

    // for simple type, the base xml type qname
    public QName simpleBaseType;
    public String javaType;

    public SerializerType serializerType;
    public QName componentType;

    public final Collection<JaxRpcFieldInfo> fields = new ArrayList<JaxRpcFieldInfo>();

    public static enum SerializerType {
        ARRAY, ENUM, LIST, OTHER
    }
}
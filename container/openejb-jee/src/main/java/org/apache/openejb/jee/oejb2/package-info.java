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
@javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(value=javax.xml.bind.annotation.adapters.CollapsedStringAdapter.class,type=String.class)
@javax.xml.bind.annotation.XmlSchema(
        namespace = "http://openejb.apache.org/xml/ns/openejb-jar-2.2", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
        xmlns = {
        @javax.xml.bind.annotation.XmlNs(prefix = "o", namespaceURI = "http://openejb.apache.org/xml/ns/openejb-jar-2.2"),
        @javax.xml.bind.annotation.XmlNs(prefix = "sys", namespaceURI = "http://geronimo.apache.org/xml/ns/deployment-1.2"),
        @javax.xml.bind.annotation.XmlNs(prefix = "pkgen", namespaceURI = "http://openejb.apache.org/xml/ns/pkgen-2.1"),
        @javax.xml.bind.annotation.XmlNs(prefix = "naming", namespaceURI = "http://geronimo.apache.org/xml/ns/naming-1.2"),
        @javax.xml.bind.annotation.XmlNs(prefix = "app", namespaceURI = "http://geronimo.apache.org/xml/ns/j2ee/application-1.2")
                }
) package org.apache.openejb.jee.oejb2;

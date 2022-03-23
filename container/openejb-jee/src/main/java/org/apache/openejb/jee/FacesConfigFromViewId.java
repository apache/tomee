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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p><span class="changed_modified_2_2">The</span>
 * value of from-view-id must contain one of the following
 * values:</p>
 *
 * <ul>
 *
 * <li><p>The exact match for a view identifier that is recognized
 * by the the ViewHandler implementation being used (such as
 * "/index.jsp" if you are using the default ViewHandler).</p></li>
 *
 * <li><p class="changed_added_2_2">The exact match of a flow node id
 * in the current flow, or a flow id of another flow.</p></li>
 *
 * <li><p> A proper prefix of a view identifier, plus a trailing
 * "*" character.  This pattern indicates that all view
 * identifiers that match the portion of the pattern up to the
 * asterisk will match the surrounding rule.  When more than one
 * match exists, the match with the longest pattern is selected.
 * </p></li>
 *
 * <li><p>An "*" character, which means that this pattern applies
 * to all view identifiers.  </p></li>
 *
 * </ul>
 *
 *
 *
 * <p>Java class for faces-config-from-view-idType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="faces-config-from-view-idType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;restriction base="&lt;http://xmlns.jcp.org/xml/ns/javaee&gt;string"&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "faces-config-from-view-idType")
public class FacesConfigFromViewId extends XmlString {


}

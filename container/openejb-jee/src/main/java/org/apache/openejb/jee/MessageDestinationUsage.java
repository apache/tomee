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
 * The message-destination-usageType specifies the use of the
 * message destination indicated by the reference.  The value
 * indicates whether messages are consumed from the message
 * destination, produced for the destination, or both.  The
 * Assembler makes use of this information in linking producers
 * of a destination with its consumers.
 * <p/>
 * The value of the message-destination-usage element must be
 * one of the following:
 * Consumes
 * Produces
 * ConsumesProduces
 */
public enum MessageDestinationUsage {
    @XmlEnumValue("Consumes") CONSUMES,
    @XmlEnumValue("Produces") PRODUCES,
    @XmlEnumValue("ConsumesProduces") CONSUMES_PRODUCES,
}

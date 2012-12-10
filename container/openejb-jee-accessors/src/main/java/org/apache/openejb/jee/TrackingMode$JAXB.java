/*
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
package org.apache.openejb.jee;

import com.envoisolutions.sxc.jaxb.JAXBEnum;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.XoXMLStreamReader;

import javax.xml.namespace.QName;

public class TrackingMode$JAXB
        extends JAXBEnum<TrackingMode> {


    public TrackingMode$JAXB() {
        super(TrackingMode.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "tracking-modeType".intern()));
    }

    public TrackingMode parse(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        return parseTrackingMode(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, TrackingMode trackingMode)
            throws Exception {
        return toStringTrackingMode(bean, parameterName, context, trackingMode);
    }

    public static TrackingMode parseTrackingMode(XoXMLStreamReader reader, RuntimeContext context, String value)
            throws Exception {
        if ("COOKIE".equals(value)) {
            return TrackingMode.COOKIE;
        } else if ("URL".equals(value)) {
            return TrackingMode.URL;
        } else if ("SSL".equals(value)) {
            return TrackingMode.SSL;
        } else {
            context.unexpectedEnumValue(reader, TrackingMode.class, value, "COOKIE", "URL", "SSL");
            return null;
        }
    }

    public static String toStringTrackingMode(Object bean, String parameterName, RuntimeContext context, TrackingMode trackingMode)
            throws Exception {
        if (TrackingMode.COOKIE == trackingMode) {
            return "COOKIE";
        } else if (TrackingMode.URL == trackingMode) {
            return "URL";
        } else if (TrackingMode.SSL == trackingMode) {
            return "SSL";
        } else {
            context.unexpectedEnumConst(bean, parameterName, trackingMode, TrackingMode.COOKIE, TrackingMode.URL, TrackingMode.SSL);
            return null;
        }
    }

}

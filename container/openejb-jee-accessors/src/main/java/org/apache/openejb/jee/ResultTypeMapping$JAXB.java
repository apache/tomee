/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.jee;

import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBEnum;
import org.metatype.sxc.jaxb.RuntimeContext;
import org.metatype.sxc.util.XoXMLStreamReader;

public class ResultTypeMapping$JAXB
    extends JAXBEnum<ResultTypeMapping>
{


    public ResultTypeMapping$JAXB() {
        super(ResultTypeMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "resultTypeMapping".intern()));
    }

    public ResultTypeMapping parse(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        return parseResultTypeMapping(reader, context, value);
    }

    public String toString(Object bean, String parameterName, RuntimeContext context, ResultTypeMapping resultTypeMapping)
        throws Exception
    {
        return toStringResultTypeMapping(bean, parameterName, context, resultTypeMapping);
    }

    public static ResultTypeMapping parseResultTypeMapping(XoXMLStreamReader reader, RuntimeContext context, String value)
        throws Exception
    {
        if ("Local".equals(value)) {
            return ResultTypeMapping.LOCAL;
        } else if ("Remote".equals(value)) {
            return ResultTypeMapping.REMOTE;
        } else {
            context.unexpectedEnumValue(reader, ResultTypeMapping.class, value, "Local", "Remote");
            return null;
        }
    }

    public static String toStringResultTypeMapping(Object bean, String parameterName, RuntimeContext context, ResultTypeMapping resultTypeMapping)
        throws Exception
    {
        if (ResultTypeMapping.LOCAL == resultTypeMapping) {
            return "Local";
        } else if (ResultTypeMapping.REMOTE == resultTypeMapping) {
            return "Remote";
        } else {
            context.unexpectedEnumConst(bean, parameterName, resultTypeMapping, ResultTypeMapping.LOCAL, ResultTypeMapping.REMOTE);
            return null;
        }
    }

}

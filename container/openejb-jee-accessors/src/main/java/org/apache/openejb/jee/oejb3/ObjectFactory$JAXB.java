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

package org.apache.openejb.jee.oejb3;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.metatype.sxc.jaxb.JAXBObject;
import org.metatype.sxc.jaxb.JAXBObjectFactory;

public class ObjectFactory$JAXB
    extends JAXBObjectFactory<ObjectFactory>
{

    public static final ObjectFactory$JAXB INSTANCE = new ObjectFactory$JAXB();
    private final Map<QName, Class<? extends JAXBObject>> rootElements = new HashMap<>();

    public ObjectFactory$JAXB() {
        super(ObjectFactory.class, EjbDeployment$JAXB.class, OpenejbJar$JAXB.class, EjbLink$JAXB.class, ResourceLink$JAXB.class, MethodParams$JAXB.class, QueryMethod$JAXB.class, Query$JAXB.class);
        rootElements.put(new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "method-param".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
        rootElements.put(new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "method-name".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
        rootElements.put(new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "object-ql".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
        rootElements.put(new QName("http://www.openejb.org/openejb-jar/1.1".intern(), "description".intern()), org.metatype.sxc.jaxb.StandardJAXBObjects.StringJAXB.class);
    }

    public Map<QName, Class<? extends JAXBObject>> getRootElements() {
        return rootElements;
    }

}

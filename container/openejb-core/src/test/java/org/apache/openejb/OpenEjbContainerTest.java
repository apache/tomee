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
package org.apache.openejb;

import junit.framework.TestCase;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;

import javax.ejb.EJB;
import javax.ejb.embeddable.EJBContainer;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbContainerTest extends TestCase {

    @EJB
    private Widget widget;

    public void testInject() throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();


        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(Widget.class));
        map.put(EJBContainer.MODULES, ejbJar);

        OpenEjbContainer openEjbContainer = (OpenEjbContainer) EJBContainer.createEJBContainer(map);

        openEjbContainer.inject(this);

        assertNotNull(widget);

        widget = null;

        openEjbContainer.getContext().bind("inject", this);
    }

    public static class Widget {

    }
}

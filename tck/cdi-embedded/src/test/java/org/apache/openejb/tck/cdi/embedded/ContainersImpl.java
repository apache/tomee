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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.SetAccessible;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import javax.ejb.embeddable.EJBContainer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ContainersImpl implements Containers {

    private static String stuck;

    private Exception exception;
    private EJBContainer container;

    @Override
    public boolean deploy(InputStream archive, String name) {
        if (!OpenEJB.isInitialized()) stuck = name;
        else System.out.println("STUCK " + stuck);

        exception = null;

        final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);

        ThreadSingletonServiceImpl.exit(null);
        if (assembler != null) {
            assembler.destroy();
        }
        try {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put(EJBContainer.MODULES, writeToFile(archive, name));
            map.put(EJBContainer.APP_NAME, name);

            container = EJBContainer.createEJBContainer(map);

            final WebBeansContext webBeansContext = ThreadSingletonServiceImpl.get();
            dump(webBeansContext.getBeanManagerImpl());

        } catch (Exception e) {
            exception = e;
            return false;
        }

        return true;
    }

    private void dump(Object o)
    {
        try
        {
            final Class<? extends Object> clazz = o.getClass();

            for (Field field : clazz.getDeclaredFields())
            {
                SetAccessible.on(field);

                if (Collection.class.isAssignableFrom(field.getType())) {
                    final Collection collection = (Collection) field.get(o);
                    System.out.println(field.getName() + "\t= " + collection.size());
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    private File writeToFile(InputStream archive, String name) throws IOException {
        final File file = File.createTempFile("deploy", "-" + name);
        file.deleteOnExit();

        final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

        try {
            int i = -1;
            while ((i = archive.read()) != -1) out.write(i);
        } finally {
            out.close();
        }

        return file;
    }

    @Override
    public DeploymentException getDeploymentException() {
        return new DeploymentException(exception.getLocalizedMessage(), exception);
    }

    @Override
    public void undeploy(String name) {
        if (container != null) container.close();
    }

    @Override
    public void setup() throws IOException {
    }

    @Override
    public void cleanup() throws IOException {
    }
}

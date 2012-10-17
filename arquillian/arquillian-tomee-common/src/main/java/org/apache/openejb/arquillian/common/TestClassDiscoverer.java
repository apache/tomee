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
package org.apache.openejb.arquillian.common;

import org.apache.openejb.config.AdditionalBeanDiscoverer;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestClassDiscoverer implements AdditionalBeanDiscoverer {
    @Override
    public AppModule discover(final AppModule module) {
        final String name = findTestName(module.getFile(), module.getClassLoader());
        if (name == null) {
            return module;
        }

        try {
            final Class<?> clazz = module.getClassLoader().loadClass(name);

            // call some reflection methods to make it fail if some dep are missing...
            Class<?> current = clazz;
            while (current != null) {
                current.getDeclaredFields();
                current.getDeclaredMethods();
                current.getCanonicalName();
                current = current.getSuperclass();
            }
        } catch (ClassNotFoundException e) {
            return module;
        } catch (NoClassDefFoundError ncdfe) {
            return module;
        }

        final EjbJar ejbJar = new EjbJar();
        final OpenejbJar openejbJar = new OpenejbJar();
        final ManagedBean bean = ejbJar.addEnterpriseBean(new ManagedBean(name, name, true));
        bean.setTransactionType(TransactionType.BEAN);
        final EjbDeployment ejbDeployment = openejbJar.addEjbDeployment(bean);
        ejbDeployment.setDeploymentId(name);
        module.getEjbModules().add(new EjbModule(ejbJar, openejbJar));
        return module;
    }

    private String findTestName(final File folder, final ClassLoader classLoader) {
        InputStream is = null;

        File dir = folder;

        if (dir != null && (dir.getName().endsWith(".war") || dir.getName().endsWith(".ear"))) {
            final File unpacked = new File(dir.getParentFile(), dir.getName().substring(0, dir.getName().length() - 4));
            if (unpacked.exists()) {
                dir = unpacked;
            }
        }

        if (dir != null && dir.isDirectory()) {
            final File info = new File(dir, "arquillian-tomee-info.txt");
            if (info.exists()) {
                try {
                    is = new FileInputStream(info);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if (is == null) {
            is = classLoader.getResourceAsStream("/arquillian-tomee-info.txt");
        }

        if (is != null) {
            try {
                return org.apache.openejb.loader.IO.slurp(is);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                org.apache.openejb.loader.IO.close(is);
            }
        }
        return null;
    }
}

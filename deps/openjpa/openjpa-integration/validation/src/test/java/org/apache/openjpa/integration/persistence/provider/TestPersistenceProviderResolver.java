/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.integration.persistence.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.apache.openjpa.lib.util.J2DoPrivHelper;

import junit.framework.TestCase;

public class TestPersistenceProviderResolver extends TestCase {
    File persistenceProviderFile;
    File targetJar1;
    File targetJar2;
    URL classesDirUrl;
    String currentDir;
    String openjpaProvider = "org.apache.openjpa.persistence.PersistenceProviderImpl";
    String dummyProvider1 = "org.apache.openjpa.integration.persistence.provider.DummyProvider1";
    String dummyProvider2 = "org.apache.openjpa.integration.persistence.provider.DummyProvider2";
    DummyPersistenceProviderResolver dummyResolver = new DummyPersistenceProviderResolver();
    
    ClassLoader originalLoader = null;
    TempUrlLoader tempLoader = null;
    
    public void setUp() throws Exception {
        super.setUp();
        
        currentDir = System.getProperty("user.dir");
        
        targetJar1 = new File(currentDir + File.separator + "target" + 
            File.separator + 
            "TestPersistenceProviderResolver1.jar");
        targetJar2 = new File(currentDir + File.separator + "target" + 
            File.separator + 
            "TestPersistenceProviderResolver2.jar");
        
        deleteTargetJars();
        
        File classesDir = new File(currentDir + File.separator + "target" +
            File.separator + "test-classes" + File.separator);
        classesDirUrl = classesDir.toURI().toURL();
        
        originalLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        // Restore the original classloader, in case there was an exception
        Thread.currentThread().setContextClassLoader(originalLoader);
    }
    
    public void testDefault() {
        List<String> providerNames = new LinkedList<String>();
        providerNames.add(openjpaProvider);
        checkProviders(providerNames);
    }
    
    
    public void testDefaultMultipleJars() throws Exception {
        String[] contents = new String[]{dummyProvider1};
        buildFile("testPersistenceProviderResolver1", contents);
        buildTargetJar(targetJar1);
        tempLoader = new TempUrlLoader(new URL[]{targetJar1.toURI().toURL(), classesDirUrl}
            ,originalLoader);
        AccessController.doPrivileged(J2DoPrivHelper
            .setContextClassLoaderAction(tempLoader));
        
        List<String> providerNames = new LinkedList<String>();
        providerNames.add(openjpaProvider);
        providerNames.add(dummyProvider1);
        checkProviders(providerNames);
        
        AccessController.doPrivileged(J2DoPrivHelper
            .setContextClassLoaderAction(originalLoader));
    }
    
    public void testDefaultMultipleProviders() throws Exception {
        String[] contents = new String[]{dummyProvider1, dummyProvider2};
        buildFile("testPersistenceProviderResolver2", contents);
        buildTargetJar(targetJar2);
        tempLoader = new TempUrlLoader(new URL[]{targetJar2.toURI().toURL(), classesDirUrl}
            ,originalLoader); 
        
        AccessController.doPrivileged(J2DoPrivHelper
            .setContextClassLoaderAction(tempLoader));
        
        List<String> providerNames = new LinkedList<String>();
        providerNames.add(openjpaProvider);
        providerNames.add(dummyProvider1);
        providerNames.add(dummyProvider2);
        checkProviders(providerNames);
        
        
        AccessController.doPrivileged(J2DoPrivHelper
            .setContextClassLoaderAction(originalLoader));
    }
    
    public void testClearCachedProviders() {
        PersistenceProviderResolver resolver = 
            PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
        assertNotNull(providers);
        resolver.clearCachedProviders();
        
        List<String> providerNames = new LinkedList<String>();
        providerNames.add(openjpaProvider);
        checkProviders(providerNames);
    }
    
    public void testNonDefaultResolver() {
        PersistenceProviderResolver originalResolver =
            PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(dummyResolver);
        PersistenceProviderResolver retrievedResolver = 
            PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        assertTrue(retrievedResolver instanceof DummyPersistenceProviderResolver);
        
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(originalResolver);
    }
    
    
    private void deleteTargetJars() {
        if (targetJar1.exists()) {
            targetJar1.delete();
        }
        if (targetJar2.exists()) {
            targetJar2.delete();
        }
    }
    
    private void buildFile(String dir, String[] contents) throws Exception {
        File servicesDir = new File(currentDir + File.separator + "target" + File.separator
            + "test-classes" + File.separator + dir + File.separator + "META-INF" + File.separator
            + "services");
        servicesDir.mkdirs();
        assertTrue(servicesDir.exists());
        persistenceProviderFile = new File(servicesDir, "javax.persistence.spi.PersistenceProvider");
        if (persistenceProviderFile.exists()) {
            persistenceProviderFile.delete();
        }
        try {
            persistenceProviderFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(persistenceProviderFile));
            for (String line : contents) {
                bw.write(line);
                bw.newLine();
            }
            
            bw.flush();
            bw.close();
        } catch (IOException e) {
            System.out.println("Problem writing to file: " + persistenceProviderFile.getAbsolutePath());
            throw e;
        }
        assertTrue(persistenceProviderFile.exists());
    }
    
    
    private void buildTargetJar(File targetJar) throws Exception {
        JarOutputStream out = new JarOutputStream(
            new BufferedOutputStream(new FileOutputStream(targetJar)));
        
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(persistenceProviderFile));

        out.putNextEntry(new JarEntry("META-INF/"));
        out.putNextEntry(new JarEntry("META-INF/services/"));
        out.putNextEntry(new JarEntry("META-INF/services/javax.persistence.spi.PersistenceProvider"));
        //write the file to the jar
        byte[] buf = new byte[1024];
        int i;
        while ((i = in.read(buf)) != -1) {
          out.write(buf, 0, i);
        }
        
        out.close();
        in.close();        
    }
    
    private void checkProviders(List<String> providerNames) {
        PersistenceProviderResolver resolver = 
            PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
        assertNotNull(providers);
        assertFalse(providers.isEmpty());
        for (PersistenceProvider provider : providers) {
            String providerName = provider.getClass().getName();
            if (providerNames.contains(providerName)) {
                providerNames.remove(providerName);
            }
        }
        assertTrue(providerNames.isEmpty());
    }
    
    class TempUrlLoader extends URLClassLoader {
        public TempUrlLoader(URL[] urls, ClassLoader parent) {
            super(urls,parent);
        }
    }
}

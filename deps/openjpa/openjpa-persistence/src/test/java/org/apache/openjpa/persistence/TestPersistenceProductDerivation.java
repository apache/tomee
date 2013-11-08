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
package org.apache.openjpa.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.encryption.EncryptionProvider;
import org.apache.openjpa.lib.util.J2DoPrivHelper;

public class TestPersistenceProductDerivation extends TestCase {
    private File sourceFile;
    private File targetFile;
    
    ClassLoader originalLoader = null;
    ClassLoader tempLoader = null;
    
    protected void setUp() throws Exception {
        super.setUp();
        String currentDir = System.getProperty("user.dir");
        
        // openjpa-persistence/target/test-classes/resources/second-persistence/META-INF/persistence.xml
        sourceFile = new File(currentDir + File.separator 
            + "target" + File.separator + "test-classes"
            + File.separator  + "second-persistence" + File.separator 
            + "META-INF" + File.separator + "persistence.xml");
        
        // openjpa-persistence/target/test-classes/
        //   TestPersistenceProductDerivation_generated_(time_stamp).jar        
        targetFile = new File(currentDir + File.separator + "target" + 
            File.separator + "test-classes" + File.separator + 
            "TestPersistenceProductDerivation_generated_" +
            System.currentTimeMillis() + ".jar");
        
        AccessController.doPrivileged(J2DoPrivHelper
            .deleteOnExitAction(targetFile));
        buildJar(sourceFile,targetFile);
        
        // Hold a reference to the current classloader so we can cleanup
        // when we're done.
        originalLoader = Thread.currentThread().getContextClassLoader();
        tempLoader = new TempUrlLoader(new URL[]{targetFile.toURI().toURL()}
            ,originalLoader);        
        AccessController.doPrivileged(J2DoPrivHelper
            .setContextClassLoaderAction(tempLoader));
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        // Restore the original classloader.
        Thread.currentThread().setContextClassLoader(originalLoader);
        
        // For whatever reason, this file won't ever delete. I searched around
        // and found numerous documented problems with deleting files. Perhaps
        // sometime in the future this problem will be fixed. For now it doesn't
        // really matter since we generate a new file every time.
        boolean deleted = AccessController.doPrivileged(J2DoPrivHelper
            .deleteAction(targetFile));
        if(deleted==false){
            System.out.println("The file " + targetFile + " wasn't deleted.");
        }
    }
    /**
     * Added for OPENJPA-932. Verifies a PersistenceProductDerivation properly loads pu's from multiple 
     * archives.
     * 
     * @throws Exception
     */
    public void testGetAnchorsInResource()throws Exception {
        
        List<String> expectedPUs = Arrays.asList(
            new String[]{"pu_1","pu_2","pu_3"});
        
        PersistenceProductDerivation ppd = new PersistenceProductDerivation();
        List<String> actual = ppd.getAnchorsInResource("META-INF/persistence.xml");
        
        assertTrue(actual.containsAll(expectedPUs));
        
        // Added for OPENJPA-993
        assertFalse(actual.contains("bad_provider"));
    }
    public void testEncryptionPluginConfiguration() throws Exception {
		PersistenceProductDerivation ppd = new PersistenceProductDerivation();
		OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
		String encryptedPassword = "encrypted_password";
		ClassLoader loader = null;

		ConfigurationProvider provider = ppd.load(
				PersistenceProductDerivation.RSRC_DEFAULT,
				"encryption_plugin_pu", loader);
		provider.setInto(conf);
		EncryptionProvider ep = conf.getEncryptionProvider();
		assertNotNull(ep);
		// Cast to test impl
		TestEncryptionProvider tep = (TestEncryptionProvider) ep;

		conf.setConnectionPassword(encryptedPassword);
		// Validate that when we get the ConnectionPassword from configuration
		// that it is decrypted
		assertEquals(TestEncryptionProvider.decryptedPassword, conf
				.getConnectionPassword());
		// Validate that the EncryptionProvider is called with the 'encrypted'
		// password
		assertEquals(encryptedPassword, tep.getEncryptedPassword());
	}
    public void testEncryptionPluginConfigurationDefaultValue() throws Exception {
		PersistenceProductDerivation ppd = new PersistenceProductDerivation();
		OpenJPAConfiguration conf = new OpenJPAConfigurationImpl();
		ClassLoader loader = null;

		ConfigurationProvider provider = ppd.load(
				PersistenceProductDerivation.RSRC_DEFAULT,
				"encryption_plugin_default_pu", loader);
		provider.setInto(conf);

		assertNull(conf.getEncryptionProvider());
	}
    
    /*
     * Verifies value of exclude-unlisted-classes with a version 1.0
     * persistence.xml.
     */
    public void testJPA1ExcludeUnlistedClasses() throws Exception {
        PersistenceProductDerivation.ConfigurationParser cp = 
                new PersistenceProductDerivation.ConfigurationParser(new HashMap());

        List<URL> urls = getResourceURL(PersistenceProductDerivation.RSRC_DEFAULT);
        assertNotNull(urls);
        assertEquals(1, urls.size());
        cp.parse(urls.get(0));
        
        List<PersistenceUnitInfoImpl> units = cp.getResults();
                
        int vfyCount = 0;
        for (PersistenceUnitInfoImpl ppui : units) {
            if ("exclude_not_specified".equals(ppui.getPersistenceUnitName())) {
                vfyCount++;
                assertFalse(ppui.excludeUnlistedClasses());
            }
            // Verify case where exclude-unlisted-classes was specified, but
            // is the default ie <exclude-unlisted-classes/>
            if ("exclude_default".equals(ppui.getPersistenceUnitName())) {
                vfyCount++;
                assertFalse(ppui.excludeUnlistedClasses());
            }
            // Verify case where exclude-unlisted-classes was specified as
            // true <exclude-unlisted-classes>true</exclude-unlisted-classes>
            if ("exclude_true".equals(ppui.getPersistenceUnitName())) {
                vfyCount++;
                assertTrue(ppui.excludeUnlistedClasses());
            }
            // Verify case where exclude-unlisted-classes was specified as
            // false <exclude-unlisted-classes>false</exclude-unlisted-classes>
            if ("exclude_false".equals(ppui.getPersistenceUnitName())) {
                vfyCount++;
                assertFalse(ppui.excludeUnlistedClasses());
            }            
        }
        // Make sure all pu's were validated
        assertEquals(4, vfyCount);                
    }

    /*
     * Verifies value of exclude-unlisted-classes with a version 2.0
     * persistence.xml.
     */
    public void testExcludeUnlistedClasses() throws Exception {
        PersistenceProductDerivation.ConfigurationParser cp = 
            new PersistenceProductDerivation.ConfigurationParser(new HashMap());

    List<URL> urls = getResourceURL("META-INF/persistence-2_0.xml");
    assertNotNull(urls);
    assertEquals(1, urls.size());
    cp.parse(urls.get(0));
    
    List<PersistenceUnitInfoImpl> units = cp.getResults();
            
    int vfyCount = 0;
    for (PersistenceUnitInfoImpl ppui : units) {
        // Verify case where exclude-unlisted-classes was not specified
        if ("exclude_not_specified".equals(ppui.getPersistenceUnitName())) {
            vfyCount++;
            assertTrue(ppui.excludeUnlistedClasses());
        }
        // Verify case where exclude-unlisted-classes was specified, but
        // is the default ie <exclude-unlisted-classes/>
        if ("exclude_default".equals(ppui.getPersistenceUnitName())) {
            vfyCount++;
            assertTrue(ppui.excludeUnlistedClasses());
        }
        // Verify case where exclude-unlisted-classes was specified as
        // true <exclude-unlisted-classes>true</exclude-unlisted-classes>
        if ("exclude_true".equals(ppui.getPersistenceUnitName())) {
            vfyCount++;
            assertTrue(ppui.excludeUnlistedClasses());
        }
        // Verify case where exclude-unlisted-classes was specified as
        // false <exclude-unlisted-classes>false</exclude-unlisted-classes>
        if ("exclude_false".equals(ppui.getPersistenceUnitName())) {
            vfyCount++;
            assertFalse(ppui.excludeUnlistedClasses());
        }            
    }
    // Make sure all pu's were validated
    assertEquals(4, vfyCount);                
        
    }

    private static List<URL> getResourceURL(String rsrc)
        throws IOException {
    Enumeration<URL> urls = null;
    try {
        ClassLoader cl = TestPersistenceProductDerivation.class.getClassLoader();
        urls = AccessController.doPrivileged(
            J2DoPrivHelper.getResourcesAction(cl, rsrc)); 
        if (!urls.hasMoreElements()) {
            if (!rsrc.startsWith("META-INF"))
              urls = AccessController.doPrivileged(
                  J2DoPrivHelper.getResourcesAction(cl, "META-INF/" + rsrc));
            if (!urls.hasMoreElements())
                return null;
        }
    } catch (PrivilegedActionException pae) {
        throw (IOException) pae.getException();
    }

    return Collections.list(urls);
}

    private void buildJar(File sourceFile, File targetFile) throws Exception {
        
        JarOutputStream out = new JarOutputStream(
            new BufferedOutputStream(new FileOutputStream(targetFile)));
        
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(sourceFile));

        out.putNextEntry(new JarEntry("META-INF/"));
        out.putNextEntry(new JarEntry("META-INF/persistence.xml"));
        //write the xml to the jar
        byte[] buf = new byte[1024];
        int i;
        while ((i = in.read(buf)) != -1) {
          out.write(buf, 0, i);
        }
        
        out.close();
        in.close();        
    }

    class TempUrlLoader extends URLClassLoader {
        public TempUrlLoader(URL[] urls, ClassLoader parent) {
            super(urls,parent);
        }
    }
    public static class TestEncryptionProvider implements EncryptionProvider {
		public static final String decryptedPassword = "decypted_password";
		// Save the 'encrypted' password so our UT can perform validation.
		private String encryptedPassword;

		public String getEncryptedPassword() {
			return encryptedPassword;
		}

		/**
		 * This method ALWAYS returns the String "decypted_password".
		 * 
		 * @see EncryptionProvider#decrypt(String)
		 */
		public String decrypt(String password) {
			encryptedPassword = password;

			return decryptedPassword;
		}

		/**
		 * @see EncryptionProvider#encrypt(String)
		 */
		public String encrypt(String password) {
			return password;
		}
	}
}

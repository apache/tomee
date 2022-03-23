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
package org.apache.openejb.cipher;

import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

@SimpleLog
@Classes(innerClassesAsBean = true, cdi = true)
@RunWith(ApplicationComposer.class)
public class CdiPasswordCipherTest {
    @jakarta.annotation.Resource
    private AResource resource;

    @Test
    public void checkPwd() {
        assertEquals("decrypted", resource.thePassword);
    }

    @Module
    public Resources resources() {
        final Resource resource = new Resource();
        resource.setClassName(AResource.class.getName());
        resource.setId("the");
        resource.getProperties().setProperty("thePassword", "cipher:cdi:" + AdvancedAlgorithm.class.getName() + ":this");

        final Resources resources = new Resources();
        resources.getResource().add(resource);
        return resources;
    }

    public static class AResource {
        private String thePassword;
    }

    public static class AdvancedAlgorithm implements PasswordCipher {
        @Inject
        private Decrypter decrypter;

        @Override
        public char[] encrypt(final String plainPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String decrypt(final char[] encryptedPassword) {
            return decrypter.decrypt(encryptedPassword);
        }
    }

    @ApplicationScoped
    public static class Decrypter {
        public String decrypt(final char[] encryptedPassword) {
            return new String(encryptedPassword).equals("this") ? "decrypted" : "failed";
        }
    }
}

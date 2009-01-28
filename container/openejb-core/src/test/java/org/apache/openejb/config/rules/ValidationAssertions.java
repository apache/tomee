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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationException;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

import junit.framework.Assert;

/**
 * @version $Rev$ $Date$
*/
public class ValidationAssertions {

    public static void assertFailures(List<String> expectedKeys, ValidationFailedException e) {
        assertValidation(expectedKeys, e.getFailures());
    }

    public static void assertErrors(List<String> expectedKeys, ValidationFailedException e) {
        assertValidation(expectedKeys, e.getErrors());
    }

    public static void assertWarnings(List<String> expectedKeys, ValidationFailedException e) {
        assertValidation(expectedKeys, e.getWarnings());
    }

    private static void assertValidation(List<String> expectedKeys, ValidationException[] validations) {
        List<String> actualKeys = new ArrayList<String>();
        for (ValidationException validation : validations) {
            actualKeys.add(validation.getMessageKey());
        }

        Collections.sort(expectedKeys);
        
        Collections.sort(actualKeys);

        String actual = org.apache.openejb.util.Join.join("\n", actualKeys);
        String expected = org.apache.openejb.util.Join.join("\n", expectedKeys);

        Assert.assertEquals("Keys do not match", expected, actual);

        // Check for the expected keys
        for (String key : expectedKeys) {
            Assert.assertTrue("Missing key: "+key, actualKeys.contains(key));
        }

        Assert.assertEquals("Number of failures don't match", expectedKeys.size(), actualKeys.size());

        // Ensure the i18n message is there by checking
        // the key is not in the getMessage() output
        for (ValidationException validation : validations) {
            String key = validation.getMessageKey();

            for (Integer level : Arrays.asList(1, 2, 3)) {
                String message = validation.getMessage(level);
                Assert.assertFalse("No message text (key=" + key + ", level=" + level + "): " + message, message.contains(key));
                Assert.assertFalse("Not all parameters substituted (key=" + key + ", level=" + level + "): " + message, message.matches(".*\\{[0-9]\\}.*"));
            }

        }
    }
}

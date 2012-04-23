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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class CheckDescriptorLocationTestFileDeletionHelper {

    @Test
    public void deleteFile() {
        File fileLocation = new File(System.getProperty("java.io.tmpdir"));
        assertTrue(fileLocation.isDirectory());
        final File[] list = fileLocation.listFiles();
        List<File> asList = null;
        if (list != null) {
            asList = Arrays.asList(list);
        } else{
            asList = new ArrayList<File>();
        }
        deleteTestCreatedFiles(asList);

    }

    private void deleteTestCreatedFiles(List<File> asList) {
        for (File file : asList) {
            deleteOrMarkForDelete(file);
        }
    }

    private void deleteOrMarkForDelete(File file) {
        if (file.getName().contains(
                CheckDescriptorLocationTest.JAR_FILENAME_PREFIX)) {

            boolean deleted = file.delete();
            if (!deleted) {
                file.deleteOnExit();
            }

        }
    }

}

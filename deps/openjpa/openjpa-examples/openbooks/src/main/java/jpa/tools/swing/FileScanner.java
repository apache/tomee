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

package jpa.tools.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileScanner {
    private String ext;
    boolean recursive;
    
    public FileScanner(String ext, boolean recurse) {
        this.ext = ext;
        this.recursive = recurse;
    }
    
    /**
     * Scans the given  
     * @param root
     * @return
     */
    public List<File> scan(File dir) {
        List<File> bag = new ArrayList<File>();
        scan(dir, bag);
        return bag;
    }
    
    private void scan(File dir, List<File> bag) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        File[] all = dir.listFiles();
        for (File f : all) {
            if (ext == null || f.getName().endsWith(ext)) {
                bag.add(f);
            }
            if (recursive && f.isDirectory()) {
                scan(f, bag);
            }
        }
    }
    
}

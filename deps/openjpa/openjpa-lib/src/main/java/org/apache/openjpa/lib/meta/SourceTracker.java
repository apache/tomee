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
package org.apache.openjpa.lib.meta;

import java.io.File;

/**
 * Interface that can optionally be implemented by metadata
 * to include the source file from which the metadata was originally parsed.
 *
 * @author Abe White
 */
public interface SourceTracker {

    public static final int SRC_OTHER = 0;
    public static final int SRC_ANNOTATIONS = 1;
    public static final int SRC_XML = 2;

    /**
     * Return the file from which this instance was parsed.
     */
    public File getSourceFile();

    /**
     * Return the domain-dependent scope of this instance within its file.
     */
    public Object getSourceScope();

    /**
     * Return the type of source.
     */
    public int getSourceType();

    /**
     * Return the domain-meaningful name of the resource that was loaded
     * from this source. I.e., if we had loaded the source for a Java
     * class, this would return the name of the class.
     */
    public String getResourceName();
    
    /**
     * Return the line number of the file at which this instance was parsed.
     */
    public int getLineNumber();

    /**
     * Return the column number in the line of the file at which this
     * instance was parsed.
     */
    public int getColNumber();
}

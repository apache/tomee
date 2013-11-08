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
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.List;

/**
 * Interface for metadata parsers.
 *
 * @author Abe White
 * @nojavadoc
 */
public interface MetaDataParser {

    /**
     * The classloader to use to resolve resources, or null for impl-defined
     * default.
     */
    public void setClassLoader(ClassLoader loader);

    /**
     * Return the results from the last parse.
     */
    public List getResults();

    /**
     * Parse the given resource.
     */
    public void parse(String rsrc) throws IOException;

    /**
     * Parse the given resource.
     */
    public void parse(URL url) throws IOException;

    /**
     * Parse the given file, which may be a directory, in which case it
     * will be scanned recursively for metadata files.
     */
    public void parse(File file) throws IOException;

    /**
     * Parse all possible metadata locations for the given class, going
     * top-down or bottom-up. If the class is null, only top-level locations
     * will be parsed.
     */
    public void parse(Class<?> cls, boolean topDown) throws IOException;

    /**
     * Parse the metadata in the given reader.
     *
     * @param content reader containing the metadata to parse
     * @param sourceName the name of the source being parsed, for use
     * in error messages
     */
    public void parse(Reader content, String sourceName) throws IOException;

    /**
     * Parse the metadata supplied by the given iterator.
     */
    public void parse(MetaDataIterator itr) throws IOException;

    /**
     * Clears the cache of parsed resource names.
     */
    public void clear();
}

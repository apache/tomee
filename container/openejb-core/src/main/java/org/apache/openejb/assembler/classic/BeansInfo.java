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

package org.apache.openejb.assembler.classic;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class BeansInfo extends InfoObject {

    public final List<String> interceptors = new ArrayList<String>();
    public final List<String> decorators = new ArrayList<String>();
    public final List<String> alternativeClasses = new ArrayList<String>();
    public final List<String> alternativeStereotypes = new ArrayList<String>();

    public final List<String> duplicatedInterceptors = new ArrayList<String>();
    public final List<String> duplicatedDecorators = new ArrayList<String>();
    public final List<String> duplicatedAlternativeClasses = new ArrayList<String>();
    public final List<String> duplicatedAlternativeStereotypes = new ArrayList<String>();

    public String version = "1.1";
    public String discoveryMode;
    public final List<ExclusionEntryInfo> excludes = new LinkedList<>();
    public final List<BDAInfo> bdas = new LinkedList<>();

    public static class ExclusionEntryInfo extends InfoObject {
        public String name;
        public ExclusionInfo exclusion;
    }

    public static class BDAInfo extends InfoObject {
        public final List<String> managedClasses = new ArrayList<String>();
        public String discoveryMode;
        public URI uri;
    }
}

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BeansInfo extends InfoObject {
    public final List<String> duplicatedInterceptors = new ArrayList<>();
    public final List<String> duplicatedDecorators = new ArrayList<>();
    public final List<String> duplicatedAlternativeClasses = new ArrayList<>();
    public final List<String> duplicatedAlternativeStereotypes = new ArrayList<>();

    public final Set<String> startupClasses = new HashSet<>();

    public String version = "1.1";
    public String discoveryMode;
    public final List<ExclusionEntryInfo> excludes = new LinkedList<>();
    public final List<BDAInfo> bdas = new LinkedList<>();
    public final List<BDAInfo> noDescriptorBdas = new LinkedList<>();

    public static class ExclusionEntryInfo extends InfoObject {
        public String name;
        public ExclusionInfo exclusion;
    }

    public static class BDAInfo extends InfoObject {
        public final Set<String> managedClasses = new HashSet<>();
        public final List<String> interceptors = new LinkedList<>();
        public final List<String> decorators = new LinkedList<>();
        public final List<String> alternatives = new LinkedList<>();
        public final List<String> stereotypeAlternatives = new LinkedList<>();
        public String discoveryMode;
        public boolean trim;
        public URI uri;
    }
}

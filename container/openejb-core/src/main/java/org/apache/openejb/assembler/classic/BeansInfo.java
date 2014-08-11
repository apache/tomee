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

import javax.xml.bind.annotation.XmlTransient;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public final Map<String, ExclusionInfo> excludes = new HashMap<>();

    // TODO: rework these two maps to make then more adapted to info tree
    // Tip: get a real CompositeBeansInfo and each BeansInfo has a List<String> classes and a discovery mode
    //      to remove these maps
    //      NB: doesn't only need to change it here but also in DeploymentLoader and AnnotationDeployer

    @XmlTransient // ClassListInfo instead of transient?
    public final Map<URL, List<String>> managedClasses = new HashMap<>();

    @XmlTransient // ClassListInfo wouldn't work since we need a key for this field + this is mainly a built info, not a tree info
    public final Map<URL, String> discoveryModeByUrl = new HashMap<>();
}

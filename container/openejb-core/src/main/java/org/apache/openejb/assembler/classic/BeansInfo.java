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
package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class BeansInfo extends InfoObject {


    public final List<String> interceptors = new ArrayList<String>();
    public final List<String> decorators = new ArrayList<String>();
    public final List<String> alternativeClasses = new ArrayList<String>();
    public final List<String> alternativeStereotypes = new ArrayList<String>();

    public final Set<String> managedClasses = new TreeSet<String>();
    
}

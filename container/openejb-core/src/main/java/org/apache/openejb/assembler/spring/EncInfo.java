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
package org.apache.openejb.assembler.spring;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="enc"
 */
public class EncInfo {
    public final Collection<EjbReferenceInfo> ejbRefs = new ArrayList<EjbReferenceInfo>();
    public final Collection<EnvEntryInfo> envEntries = new ArrayList<EnvEntryInfo>();
    public final Collection<ResourceReferenceInfo> resourceRefs = new ArrayList<ResourceReferenceInfo>();

    /**
     * @org.apache.xbean.FlatCollection childElement="ejbRef"
     */
    public EjbReferenceInfo[] getEjbRefs() {
        return ejbRefs.toArray(new EjbReferenceInfo[ejbRefs.size()]);
    }

    public void setEjbRefs(EjbReferenceInfo[] ejbRefs) {
        this.ejbRefs.addAll(Arrays.asList(ejbRefs));
    }

    /**
     * @org.apache.xbean.FlatCollection childElement="envEntry"
     */
    public EnvEntryInfo[] getEnvEntries() {
        return envEntries.toArray(new EnvEntryInfo[envEntries.size()]);
    }

    public void setEnvEntries(EnvEntryInfo[] envEntries) {
        this.envEntries.addAll(Arrays.asList(envEntries));
    }

    /**
     * @org.apache.xbean.FlatCollection childElement="resourceRef"
     */
    public ResourceReferenceInfo[] getResourceRefs() {
        return resourceRefs.toArray(new ResourceReferenceInfo[resourceRefs.size()]);
    }

    public void setResourceRefs(ResourceReferenceInfo[] resourceRefs) {
        this.resourceRefs.addAll(Arrays.asList(resourceRefs));
    }

}

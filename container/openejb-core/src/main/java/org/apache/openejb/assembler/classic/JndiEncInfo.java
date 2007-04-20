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
package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.List;


public class JndiEncInfo extends InfoObject {
    public final List<EnvEntryInfo> envEntries = new ArrayList<EnvEntryInfo>();
    public final List<EjbReferenceInfo> ejbReferences = new ArrayList<EjbReferenceInfo>();
    public final List<EjbLocalReferenceInfo> ejbLocalReferences = new ArrayList<EjbLocalReferenceInfo>();
    public final List<ResourceReferenceInfo> resourceRefs = new ArrayList<ResourceReferenceInfo>();
    public final List<PersistenceUnitReferenceInfo> persistenceUnitRefs = new ArrayList<PersistenceUnitReferenceInfo>();
    public final List<PersistenceContextReferenceInfo> persistenceContextRefs = new ArrayList<PersistenceContextReferenceInfo>();
    public final List<ResourceEnvReferenceInfo> resourceEnvRefs = new ArrayList<ResourceEnvReferenceInfo>();
    public final List<ServiceReferenceInfo> serviceRefs = new ArrayList<ServiceReferenceInfo>();
}

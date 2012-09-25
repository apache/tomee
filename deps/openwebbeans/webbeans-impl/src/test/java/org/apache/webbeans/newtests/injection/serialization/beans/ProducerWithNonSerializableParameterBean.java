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
package org.apache.webbeans.newtests.injection.serialization.beans;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

/**
 * Producer method with non serializable producer method parameters.
 * See specification issues CDI-140, CDI-141 and CDI-153.
 * @since CDI-1.1
 */
public class ProducerWithNonSerializableParameterBean
{

    /**
     * Non serializable parameters did crush in CDI-1.0 but are fine in CDI-1.1.
     */
    @Produces
    @SessionScoped
    public SerializableBean newSerializableBean(NonSerializableDependentBean someParameter)
    {
        return new SerializableBean();
    }
}

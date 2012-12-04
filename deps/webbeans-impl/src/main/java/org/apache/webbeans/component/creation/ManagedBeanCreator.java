/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.component.creation;

/**
 * Contract for {@link org.apache.webbeans.component.ManagedBean}.
 * 
 * @version $Rev: 952591 $ $Date: 2010-06-08 11:45:27 +0200 (mar., 08 juin 2010) $
 *
 * @param <T> bean class
 */
public interface ManagedBeanCreator<T> extends InjectedTargetBeanCreator<T>
{
    /**
     * Define managed bean constructor.
     */
    public void defineConstructor();
        
}

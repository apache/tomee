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
package org.apache.webbeans.portable.events.generics;

public interface GenericBeanEvent
{

    /**
     * If this is a Foo<X> event and we are considering it as a Bar<Y> event,
     * returns the generic type of Foo as a Bar.  Normally this is X, but in at least one case
     * (ProcessSessionBean) the generic type is different.
     * @param eventClass the class of event we are treating this event as
     * @return the generic type parameter of this event considered as an "eventClass"
     */
    public Class<?> getBeanClassFor(Class<?> eventClass);
}

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

import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.Producer;

import org.apache.webbeans.portable.events.ProcessProducerImpl;
import org.apache.webbeans.util.ClassUtil;

@SuppressWarnings("unchecked")
public class GProcessProducer extends ProcessProducerImpl implements GenericProducerObserverEvent
{
    public GProcessProducer(Producer<?> producer, AnnotatedMember<?> annotateMember)
    {
        super(producer,annotateMember);
    }

    public Class<?> getBeanClass()
    {
        return getAnnotatedMember().getDeclaringType().getJavaClass();
    }

    public Class<?> getProducerOrObserverType()
    {
        return ClassUtil.getClazz(getAnnotatedMember().getBaseType());
    }


}

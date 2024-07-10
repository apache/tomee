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
package org.apache.tomee.mojarra.owb;

import jakarta.enterprise.inject.spi.Bean;
import org.apache.webbeans.component.third.PassivationCapableThirdpartyBeanImpl;
import org.apache.webbeans.config.WebBeansContext;

import java.lang.reflect.Type;

public class MojarraPassivationCapableThirdPartyBeanImpl<T> extends PassivationCapableThirdpartyBeanImpl<T> {
    private final Class<T> returnType;

    public MojarraPassivationCapableThirdPartyBeanImpl(WebBeansContext webBeansContext, Bean<T> bean) {
        super(webBeansContext, bean);

        Class<?> tempReturnType = Object.class;
        for (Type type : bean.getTypes()) {
            if (!(type instanceof Class<?> clazz)) {
                continue;
            }

            if (tempReturnType.isAssignableFrom(clazz))
            {
                tempReturnType = clazz;
            }
        }

        this.returnType = (Class<T>) tempReturnType;
    }

    @Override
    public Class<T> getReturnType() {
        return returnType;
    }
}

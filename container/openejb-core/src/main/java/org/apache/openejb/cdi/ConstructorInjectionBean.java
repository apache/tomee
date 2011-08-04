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
package org.apache.openejb.cdi;

import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.InjectableConstructor;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.spi.CreationalContext;
import java.lang.reflect.Constructor;

/**
* @version $Rev$ $Date$
*/
public class ConstructorInjectionBean<T> extends AbstractInjectionTargetBean<T> {

    private final Constructor<T> constructor;

    public ConstructorInjectionBean(WebBeansContext webBeansContext, Class<T> returnType) {
        super(WebBeansType.DEPENDENT, returnType, webBeansContext);

        if (webBeansContext == null) throw new NullPointerException("webBeansContext");
        if (returnType == null) throw new NullPointerException("returnType");

        final WebBeansUtil webBeansUtil = webBeansContext.getWebBeansUtil();

        if (webBeansUtil == null) throw new NullPointerException("webBeansUtil");

        constructor = webBeansUtil.defineConstructor(returnType);

        if (constructor == null) throw new NullPointerException("constructor");

        final DefinitionUtil definitionUtil = getWebBeansContext().getDefinitionUtil();

        if (definitionUtil == null) throw new NullPointerException("definitionUtil");

        definitionUtil.addConstructorInjectionPointMetaData(this, constructor);

        // these are not used immediately in createInstance()
//        definitionUtil.defineInjectedFields(this);
//        definitionUtil.defineInjectedMethods(this);
    }

    @Override
    protected T createInstance(CreationalContext<T> tCreationalContext) {
        InjectableConstructor<T> ic = new InjectableConstructor<T>(constructor, this, tCreationalContext);
        return ic.doInjection();
    }
}

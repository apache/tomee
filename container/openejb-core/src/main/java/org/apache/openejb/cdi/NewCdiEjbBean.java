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

import org.apache.webbeans.annotation.NewLiteral;
import org.apache.webbeans.component.NewBean;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.New;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
* @version $Rev$ $Date$
*/
public class NewCdiEjbBean<T> extends CdiEjbBean<T> implements NewBean<T> {

    public NewCdiEjbBean(CdiEjbBean<T> that) {
        super(that.getBeanContext(), that.getWebBeansContext());

        this.getImplQualifiers().add(new NewLiteral(getReturnType()));

        this.apiTypes.clear();
        this.apiTypes.addAll(that.getTypes());
        this.setName(null);
        this.getInjectedFields().addAll(that.getInjectedFields());
        this.getInjectedFromSuperFields().addAll(that.getInjectedFromSuperFields());
        this.getInjectedFromSuperMethods().addAll(that.getInjectedFromSuperMethods());
        this.getInjectedMethods().addAll(that.getInjectedMethods());
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<Method> getObservableMethods() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String getId() {
        return super.getId()+"@NewBean";
    }
}

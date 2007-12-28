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
package org.superbiz.interceptors;

import javax.ejb.Stateless;
import javax.interceptor.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
@Stateless
@Interceptors({ClassLevelInterceptorOne.class, ClassLevelInterceptorTwo.class})
@ExcludeDefaultInterceptors
public class ThirdSLSBean implements ThirdSLSBeanLocal {

    @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})
    public List<String> businessMethod() {
        List<String> list = new ArrayList<String>();
        list.add("businessMethod");
        return list;
    }

    @Interceptors({MethodLevelInterceptorOne.class, MethodLevelInterceptorTwo.class})    
    @ExcludeClassInterceptors
    public List<String> anotherBusinessMethod() {
        List<String> list = new ArrayList<String>();
        list.add("anotherBusinessMethod");
        return list;
    }


    @AroundInvoke
    protected Object beanClassBusinessMethodInterceptor(InvocationContext ic) throws Exception {
        return Utils.addClassSimpleName(ic, this.getClass().getSimpleName());
    }    
}

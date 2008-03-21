/**
 *
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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.core.interceptor.InterceptorData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

public class InterceptorBuilder {
    public final List<InterceptorData> defaultInterceptors;
    public final boolean excludeDefaultInterceptors;
    public final List<InterceptorData> classInterceptors;
    public final List<MethodInterceptorInfo> methodInterceptors;

    public InterceptorBuilder(List<InterceptorInfo> defaultInterceptors, EnterpriseBeanInfo bean) {
        this.defaultInterceptors = toInterceptorDatas(defaultInterceptors);
        this.excludeDefaultInterceptors = false;//bean.excludeDefaultInterceptors;
        this.classInterceptors = null; //toInterceptorDatas(bean.classInterceptors);
        this.methodInterceptors = null;//new ArrayList<MethodInterceptorInfo>(bean.methodInterceptors);
        Collections.sort(methodInterceptors, METHOD_INTERCEPTOR_INFO_COMPARATOR);
    }

    public List<InterceptorData> build(Method method) {
        List<InterceptorData> interceptors = new ArrayList<InterceptorData>();

        // check for a method level interceptor
        for (MethodInterceptorInfo methodInterceptorInfo : methodInterceptors) {
            if (MethodInfoUtil.matches(method, methodInterceptorInfo.methodInfo)) {
                if (!methodInterceptorInfo.excludeDefaultInterceptors) {
                    for (InterceptorData interceptorData : defaultInterceptors) {
                        interceptors.add(interceptorData);
                    }
                }

                if (!methodInterceptorInfo.excludeClassInterceptors) {
                    for (InterceptorData interceptorData : classInterceptors) {
                        interceptors.add(interceptorData);
                    }
                }

                //interceptors.addAll(toInterceptorDatas(methodInterceptorInfo.interceptors));

                return interceptors;
            }
        }

        // no method level interceptor

        if (!excludeDefaultInterceptors) {
            for (InterceptorData interceptorData : defaultInterceptors) {
                interceptors.add(interceptorData);
            }
        }

        for (InterceptorData interceptorData : classInterceptors) {
            interceptors.add(interceptorData);
        }

        return interceptors;
    }

    private static List<InterceptorData> toInterceptorDatas(List<InterceptorInfo> interceptorInfos) {
        ArrayList<InterceptorData> interceptorDatas = new ArrayList<InterceptorData>(interceptorInfos.size());
//        for (InterceptorInfo interceptorInfo : interceptorInfos) {
//            interceptorDatas.add(new InterceptorData(interceptorInfo.clazz, interceptorInfo.methodName));
//        }
        return interceptorDatas;
    }

    public static final MethodInterceptorInfoComparator METHOD_INTERCEPTOR_INFO_COMPARATOR = new MethodInterceptorInfoComparator();
    public static class MethodInterceptorInfoComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            MethodInterceptorInfo mi1 = (MethodInterceptorInfo) o1;
            MethodInterceptorInfo mi2 = (MethodInterceptorInfo) o2;

            MethodInfo m1 = mi1.methodInfo;
            MethodInfo m2 = mi2.methodInfo;

            int val = m1.methodName.compareTo(m2.methodName);
            if (val != 0) return val;

            if (m1.methodParams == null) {
                return m2.methodParams == null ? 0 : -1;
            } else if (m2.methodParams == null) {
                return 1;
            }

            if (m1.methodParams.size() != m2.methodParams.size()) {
                return m1.methodParams.size() - m2.methodParams.size();
            }

            Iterator<String> iterator = m1.methodParams.iterator();
            for (String param2 : m2.methodParams) {
                String param1 = iterator.next();
                val = param1.compareTo(param2);
                if (val != 0) return val;
            }
            return 0;
        }
    }
}

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
package org.apache.webbeans.newtests.definition.proxyable.beans;

import javax.enterprise.context.RequestScoped;

/**
 * This bean has a private final method.
 * It should be possible to create a proxy for it.
 */
@RequestScoped
public class BeanWithPrivateFinalMethod
{
    private final int internalMethod()
    {
        return 42;
    }

    public static int staticMethod()
    {
        return 4711;
    }

    public int externalMethod()
    {
        return internalMethod();
    }
}

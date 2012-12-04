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
package org.apache.webbeans.annotation;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

/**
 * {@link Default} literal annotation.
 * 
 * @since 1.0
 */
public class DefaultLiteral extends AnnotationLiteral<Default> implements Default
{
    private static final String TOSTRING = "@javax.enterprise.inject.Default()";
    private static final long serialVersionUID = 6788272256977634238L;

    @Override
    public int hashCode()
    {
        // implemented for performance reasons
        // currently this is needed because AnnotationLiteral always returns 0 as hashCode
        return 0;
    }

    @Override
    public boolean equals(Object other)
    {
        // implemented for performance reasons
        if (other instanceof Default)
        {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        // implemented for performance reasons
        return TOSTRING;
    }
}

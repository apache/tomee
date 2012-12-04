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
package org.apache.webbeans.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Custom parametrized type implementation.
 * @version $Rev: 1182780 $ $Date: 2011-10-13 13:11:03 +0200 (jeu., 13 oct. 2011) $
 *
 */
public class OwbParametrizedTypeImpl implements ParameterizedType
{
    /**Owner type*/
    private final Type owner;
    
    /**Raw type*/
    private final Type rawType;
    
    /**Actual type arguments*/
    private final List<Type> types = new ArrayList<Type>();
    
    /**
     * New instance.
     * @param owner owner
     * @param raw raw
     */
    public OwbParametrizedTypeImpl(Type owner, Type raw)
    {
        this.owner = owner;
        rawType = raw;
    }
    
    public Type[] getActualTypeArguments()
    {
        return types.toArray(new Type[types.size()]);
    }
    
    public void addTypeArgument(Type type)
    {
        types.add(type);
    }

    public Type getOwnerType()
    {
        return owner;
    }

    public Type getRawType()
    {
        return rawType;
    }

    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(getActualTypeArguments());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        OwbParametrizedTypeImpl other = (OwbParametrizedTypeImpl) obj;
        if (!Arrays.equals(getActualTypeArguments(), other.getActualTypeArguments()))
        {
            return false;
        }
        if (owner == null)
        {
            if (other.owner != null)
            {
                return false;
            }
        }
        else if (!owner.equals(other.owner))
        {
            return false;
        }
        if (rawType == null)
        {
            if (other.rawType != null)
            {
                return false;
            }
        }
        else if (!rawType.equals(other.rawType))
        {
            return false;
        }
        
        return true;
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(((Class<?>) rawType).getSimpleName());
        Type[] actualTypes = getActualTypeArguments();
        if(actualTypes.length > 0)
        {
            buffer.append("<");
            int length = actualTypes.length;
            for(int i=0;i<length;i++)
            {
                buffer.append(actualTypes[i].toString());
                if(i != actualTypes.length-1)
                {
                    buffer.append(",");
                }
            }
            
            buffer.append(">");
        }
        
        return buffer.toString();
    }
}

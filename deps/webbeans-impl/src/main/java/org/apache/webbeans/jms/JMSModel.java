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
package org.apache.webbeans.jms;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class JMSModel
{
    public enum JMSType
    {
        QUEUE,
        TOPIC
    }

    private final JMSType jmsType;    
    private String jndiName;    
    private final String mappedName;    
    private boolean isJndiNameDefined;    
    private final Set<Annotation> bindings = new HashSet<Annotation>();
    
    
    public JMSModel(JMSType jmsType, String jndiName, String mappedName)
    {
        this.jmsType = jmsType;
        
        if(jndiName != null)
        {
            this.jndiName = jndiName;
            isJndiNameDefined = true;
        }
        
        this.mappedName = mappedName;
    }

    public void addBinding(Annotation annotation)
    {
        bindings.add(annotation);
    }
    
    public Annotation[] getBindings()
    {
        return bindings.toArray(new Annotation[bindings.size()]);
    }

    /**
     * @return the jmsType
     */
    public JMSType getJmsType()
    {
        return jmsType;
    }


    /**
     * @return the jndiName
     */
    public String getJndiName()
    {
        return jndiName;
    }


    /**
     * @return the mappedName
     */
    public String getMappedName()
    {
        return mappedName;
    }


    /**
     * @return the isJndiNameDefined
     */
    public boolean isJndiNameDefined()
    {
        return isJndiNameDefined;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bindings == null) ? 0 : bindings.hashCode());
        result = prime * result + ((jmsType == null) ? 0 : jmsType.hashCode());
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
        JMSModel other = (JMSModel) obj;
        if (bindings == null)
        {
            if (other.bindings != null)
            {
                return false;
            }
        }
        else if (!bindings.equals(other.bindings))
        {
            return false;
        }
        if (jmsType == null)
        {
            if (other.jmsType != null)
            {
                return false;
            }
        }
        else if (!jmsType.equals(other.jmsType))
        {
            return false;
        }
        return true;
    }
   
}

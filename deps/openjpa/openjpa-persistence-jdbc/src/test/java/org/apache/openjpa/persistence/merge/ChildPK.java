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
package org.apache.openjpa.persistence.merge;

import java.io.Serializable;

public class ChildPK implements Serializable{
  private static final long serialVersionUID = 1L;

  private ParentPK parent;

  private Integer childKey;

  public ParentPK getParent(){ return parent; }
  public void setParent(ParentPK parent) {    this.parent = parent; }
  public Integer getChildKey() {   return childKey;  }
  public void setChildKey(Integer childKey)  {    this.childKey = childKey;  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((childKey == null) ? 0 : childKey.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ChildPK other = (ChildPK) obj;
    if (childKey == null)
    {
      if (other.childKey != null)
        return false;
    }
    else if (!childKey.equals(other.childKey))
      return false;
    if (parent == null)
    {
      if (other.parent != null)
        return false;
    }
    else if (!parent.equals(other.parent))
      return false;
    return true;
  }

}

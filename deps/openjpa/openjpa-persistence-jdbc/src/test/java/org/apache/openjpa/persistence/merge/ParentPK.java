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


public class ParentPK implements Serializable {
  private static final long serialVersionUID = 1L;

  private String key1;

  private Integer key2;

  public ParentPK()  {
    this.key1 = "00000000000000000000000000000000";
  }

  public ParentPK(Integer key2)  {
    this();
    this.key2 = key2;
  }

  public String getKey1()  {    return key1;  }
  public void setKey1(String key1)  {    this.key1 = key1;  }
  public Integer getKey2()  {    return key2;  }
  public void setKey2(Integer key2)  {    this.key2 = key2;  }

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
    result = prime * result + ((key1 == null) ? 0 : key1.hashCode());
    result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
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
    ParentPK other = (ParentPK) obj;
    if (key1 == null)
    {
      if (other.key1 != null)
        return false;
    }
    else if (!key1.equals(other.key1))
      return false;
    if (key2 == null)
    {
      if (other.key2 != null)
        return false;
    }
    else if (!key2.equals(other.key2))
      return false;
    return true;
  }

}

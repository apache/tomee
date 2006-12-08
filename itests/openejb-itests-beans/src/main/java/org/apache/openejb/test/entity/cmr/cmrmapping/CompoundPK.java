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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.test.entity.cmr.cmrmapping;

import java.io.Serializable;


/**
 *
 * @version $Revision: 472584 $ $Date: 2006-11-08 10:47:55 -0800 (Wed, 08 Nov 2006) $
 */
public class CompoundPK implements Serializable {
    private static final long serialVersionUID = 3210397138847726239L;
    public Integer id;
    public Integer field1;

    public CompoundPK() {}
    
    public CompoundPK(Integer id, Integer field1) {
        this.id = id;
        this.field1 = field1;
    }
    
    public boolean equals(Object other) {
      if (!(other instanceof CompoundPK) ) {
          return false;
      }
      CompoundPK otherPK = (CompoundPK) other;
      return field1.equals(otherPK.field1) && id.equals(otherPK.id);
    }
    
    public int hashCode() {
      return field1.hashCode() ^ id.hashCode();
    }
}

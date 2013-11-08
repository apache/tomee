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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.Entity;

/**
 * Concrete subclass that defines two more primary key fields than its
 * abstract superclass.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
@Entity
public class AppIdSubC
    extends AppIdSubB {

    private long pk1c;
    private String pk2c;
    private String stringFieldC;

    public void setPk1c(long pk1c) {
        this.pk1c = pk1c;
    }

    public long getPk1c() {
        return this.pk1c;
    }

    public void setPk2c(String pk2c) {
        this.pk2c = pk2c;
    }

    public String getPk2c() {
        return this.pk2c;
    }

    public void setStringFieldC(String stringFieldC) {
        this.stringFieldC = stringFieldC;
    }

    public String getStringFieldC() {
        return this.stringFieldC;
    }

    /*
     public static class ID
         extends AppIdSubB.ID
     {
         private long pk1c;
         private String pk2c;


         public ID ()
         {
             super ();
         }


         public ID (String str)
         {
             super ();
             fromString (str);
         }


         public int hashCode ()
         {
             return (int)((super.hashCode () + pk1c
                 + (pk2c == null ? 0 : pk2c.hashCode ())) % Integer.MAX_VALUE);
         }


         public boolean equals (Object other)
         {
             return super.equals (other)
                 && ((ID)other).pk1c == pk1c
                 && ((ID)other).pk2c == null ? pk2c == null
                     : ((ID)other).pk2c.equals (pk2c);
         }


         public String toString ()
         {
             return super.toString () + DELIMITER + pk1c + DELIMITER + pk2c;
         }


         StringTokenizer fromString (String idString)
         {
             StringTokenizer tok = super.fromString (idString);
             pk1c = new Long (tok.nextToken ()).longValue ();
             pk2c = tok.nextToken ();
             return tok; // return the tokenizer for subclasses to use
         }
     }
     */
}

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
 * Concrete subclass that defines one more primary key field than its
 * abstract superclass.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
@Entity
public class AppIdSubD
    extends AppIdSubB {

    private double pkd;
    private String stringFieldD;

    public void setPkd(double pkd) {
        this.pkd = pkd;
    }

    public double getPkd() {
        return this.pkd;
    }

    public void setStringFieldD(String stringFieldD) {
        this.stringFieldD = stringFieldD;
    }

    public String getStringFieldD() {
        return this.stringFieldD;
    }

    /*
     public static class ID
         extends AppIdSubB.ID
     {
         private double pkd;


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
             return (int)((super.hashCode () + pkd) % Integer.MAX_VALUE);
         }


         public boolean equals (Object other)
         {
             return super.equals (other)
                 && ((ID)other).pkd == pkd;
         }


         public String toString ()
         {
             return super.toString () + DELIMITER + pkd;
         }


         StringTokenizer fromString (String idString)
         {
             StringTokenizer tok = super.fromString (idString);
             pkd = new Double (tok.nextToken ()).doubleValue ();
             return tok; // return the tokenizer for subclasses to use
         }
     }
     */
}

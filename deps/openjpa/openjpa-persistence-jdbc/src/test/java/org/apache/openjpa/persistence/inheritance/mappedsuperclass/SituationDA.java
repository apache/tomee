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
package org.apache.openjpa.persistence.inheritance.mappedsuperclass;

import javax.persistence.*;

/**
 * Entity used to test MappedSuperClass which does not have IdClass. 
 * 
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-873">OPENJPA-873</A>
 *  
 * @author pioneer_ip@yahoo.com
 * @author Fay Wang
 *
 */

@Entity
@Table (name = "cc2SITUATION")
@IdClass(SituationDA.SituationId.class)
public class SituationDA extends CashBaseEntity {
   
    @Id
    @Column(name="C2008SRL")
    private String cashBoxPeriodSerial;
   
    @Id
    @Column(name="C2012TYPE")
    private short type;
    
    public static class SituationId implements java.io.Serializable{
        private static final long serialVersionUID = 1L;
        private String cashBoxPeriodSerial;
        private short type;
       
        public SituationId(){}
        
        public SituationId(String cashBoxPeriodSerial, short type){
            this.cashBoxPeriodSerial = cashBoxPeriodSerial;
            this.type = type;
        }
        
        
        public boolean equals(Object other){
            if (other instanceof SituationId) {
                final SituationId otherId = (SituationId)other;
                return ( otherId.cashBoxPeriodSerial.equals(
                    this.cashBoxPeriodSerial) && otherId.type == this.type );
                }
                return false;   
        }
       
        public int hashCode() {
            return super.hashCode();
        }
    }
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public String getCashBoxPeriodSerial() {
        return cashBoxPeriodSerial;
    }
    
    public void setCashBoxPeriodSerial(String cashBoxPeriodSerial) {
        this.cashBoxPeriodSerial = cashBoxPeriodSerial;
    }
    
}



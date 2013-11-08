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
package org.apache.openjpa.persistence.inheritance.entities;

import java.lang.reflect.Constructor;

import org.apache.openjpa.persistence.inheritance.entities.idmsc.
    PrimitiveIDMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.chardiscriminator.PIdJTCDMSCRootEntity;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.intdiscriminator.PIdJTIDMSCRootEntity;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.
    PIdJTSDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.jointable.stringdiscriminator.PIdJTSDMSCRootEntity;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.
    PIdSTCDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.chardiscriminator.PIdSTCDMSCRootEntity;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.
    PIdSTIDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.intdiscriminator.PIdSTIDMSCRootEntity;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCEntityB;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCEntityD;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafA;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafB1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafB2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafC;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafD1;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCLeafD2;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.
    PIdSTSDMSCMappedSuperclass;
import org.apache.openjpa.persistence.inheritance.entities.idmsc.singletable.stringdiscriminator.PIdSTSDMSCRootEntity;

public class EntityMapping {
    @SuppressWarnings("unchecked")
    public static enum InheritanceEntityMapping {
        /*
         * Primitive Identity defined on a Mapped Superclass, 
         * Joined Table Entity Inheritance
         */
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Joined Table Entity Inheritance, Integer Discriminator
        PIdJTIDMSCRootEntity {         
            public Class getEntityClass() {
                return PIdJTIDMSCRootEntity.class;
            }
        },
        PIdJTIDMSCLeafA {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafA.class;
            }
        },
        PIdJTIDMSCEntityB {
            public Class getEntityClass() {
                return PIdJTIDMSCEntityB.class;
            }
        },
        PIdJTIDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafB1.class;
            }
        },
        PIdJTIDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafB2.class;
            }
        },
        PIdJTIDMSCLeafC {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafC.class;
            }
        },
        PIdJTIDMSCEntityD {
            public Class getEntityClass() {
                return PIdJTIDMSCEntityD.class;
            }
        },
        PIdJTIDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafD1.class;
            }
        },
        PIdJTIDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdJTIDMSCLeafD2.class;
            }
        },
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Joined Table Entity Inheritance, Char Discriminator
        PIdJTCDMSCRootEntity {
            public Class getEntityClass() {
                return PIdJTCDMSCRootEntity.class;
            }
        },
        PIdJTCDMSCLeafA {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafA.class;
            }
        },
        PIdJTCDMSCEntityB {
            public Class getEntityClass() {
                return PIdJTCDMSCEntityB.class;
            }
        },
        PIdJTCDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafB1.class;
            }
        },
        PIdJTCDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafB2.class;
            }
        },
        PIdJTCDMSCLeafC {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafC.class;
            }
        },
        PIdJTCDMSCEntityD {
            public Class getEntityClass() {
                return PIdJTCDMSCEntityD.class;
            }
        },
        PIdJTCDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafD1.class;
            }
        },
        PIdJTCDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdJTCDMSCLeafD2.class;
            }
        },
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Joined Table Entity Inheritance, String Discriminator
        PIdJTSDMSCRootEntity {
            public Class getEntityClass() {
                return PIdJTSDMSCRootEntity.class;
            }
        },
        PIdJTSDMSCLeafA {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafA.class;
            }
        },
        PIdJTSDMSCEntityB {
            public Class getEntityClass() {
                return PIdJTSDMSCEntityB.class;
            }
        },
        PIdJTSDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafB1.class;
            }
        },
        PIdJTSDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafB2.class;
            }
        },
        PIdJTSDMSCLeafC {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafC.class;
            }
        },
        PIdJTSDMSCEntityD {
            public Class getEntityClass() {
                return PIdJTSDMSCEntityD.class;
            }
        },
        PIdJTSDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafD1.class;
            }
        },
        PIdJTSDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdJTSDMSCLeafD2.class;
            }
        },
        
        /*
         * Primitive Identity defined on a Mapped Superclass, 
         * Single Table Entity Inheritance
         */
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Single Table Entity Inheritance, Integer Discriminator
        PIdSTIDMSCRootEntity {
            public Class getEntityClass() {
                return PIdSTIDMSCRootEntity.class;
            }
        },
        PIdSTIDMSCLeafA {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafA.class;
            }
        },
        PIdSTIDMSCEntityB {
            public Class getEntityClass() {
                return PIdSTIDMSCEntityB.class;
            }
        },
        PIdSTIDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafB1.class;
            }
        },
        PIdSTIDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafB2.class;
            }
        },
        PIdSTIDMSCLeafC {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafC.class;
            }
        },
        PIdSTIDMSCEntityD {
            public Class getEntityClass() {
                return PIdSTIDMSCEntityD.class;
            }
        },
        PIdSTIDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafD1.class;
            }
        },
        PIdSTIDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdSTIDMSCLeafD2.class;
            }
        },
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Single Table Entity Inheritance, Char Discriminator
        PIdSTCDMSCRootEntity {
            public Class getEntityClass() {
                return PIdSTCDMSCRootEntity.class;
            }
        },
        PIdSTCDMSCLeafA {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafA.class;
            }
        },
        PIdSTCDMSCEntityB {
            public Class getEntityClass() {
                return PIdSTCDMSCEntityB.class;
            }
        },
        PIdSTCDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafB1.class;
            }
        },
        PIdSTCDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafB2.class;
            }
        },
        PIdSTCDMSCLeafC {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafC.class;
            }
        },
        PIdSTCDMSCEntityD {
            public Class getEntityClass() {
                return PIdSTCDMSCEntityD.class;
            }
        },
        PIdSTCDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafD1.class;
            }
        },
        PIdSTCDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdSTCDMSCLeafD2.class;
            }
        },
        
        // Primitive Identity defined on a Mapped Superclass, 
        // Single Table Entity Inheritance, String Discriminator
        PIdSTSDMSCRootEntity {
            public Class getEntityClass() {
                return PIdSTSDMSCRootEntity.class;
            }
        },
        PIdSTSDMSCLeafA {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafA.class;
            }
        },
        PIdSTSDMSCEntityB {
            public Class getEntityClass() {
                return PIdSTSDMSCEntityB.class;
            }
        },
        PIdSTSDMSCLeafB1 {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafB1.class;
            }
        },
        PIdSTSDMSCLeafB2 {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafB2.class;
            }
        },
        PIdSTSDMSCLeafC {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafC.class;
            }
        },
        PIdSTSDMSCEntityD {
            public Class getEntityClass() {
                return PIdSTSDMSCEntityD.class;
            }
        },
        PIdSTSDMSCLeafD1 {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafD1.class;
            }
        },
        PIdSTSDMSCLeafD2 {
            public Class getEntityClass() {
                return PIdSTSDMSCLeafD2.class;
            }
        },

        
        /*
         * Non Entity PC-Aware Types
         */
        PrimitiveIDMappedSuperclass {
            public Class getEntityClass() {
                return PrimitiveIDMappedSuperclass.class;
            }
        }, 
        PIdJTIDMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdJTIDMSCMappedSuperclass.class;
            }
        },
        PIdJTICMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdJTCDMSCMappedSuperclass.class;
            }
        },
        PIdJTISMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdJTSDMSCMappedSuperclass.class;
            }
        },
        PIdSTIDMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdSTIDMSCMappedSuperclass.class;
            }
        },
        PIdSTICMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdSTCDMSCMappedSuperclass.class;
            }
        },
        PIdSTISMSCMappedSuperclass {
            public Class getEntityClass() {
                return PIdSTSDMSCMappedSuperclass.class;
            }
        }
        
        ;
        
        public abstract Class getEntityClass();
        public String getEntityName() {
            return getEntityClass().getSimpleName();
        }
    };
    
    public final static InheritanceEntityMapping[] PIdJTIDMSC = {
        InheritanceEntityMapping.PIdJTIDMSCRootEntity,
        InheritanceEntityMapping.PIdJTIDMSCLeafA,
        InheritanceEntityMapping.PIdJTIDMSCEntityB,
        InheritanceEntityMapping.PIdJTIDMSCLeafB1,
        InheritanceEntityMapping.PIdJTIDMSCLeafB2,
        InheritanceEntityMapping.PIdJTIDMSCLeafC,
        InheritanceEntityMapping.PIdJTIDMSCEntityD,
        InheritanceEntityMapping.PIdJTIDMSCLeafD1,
        InheritanceEntityMapping.PIdJTIDMSCLeafD2
    };
    
    public final static InheritanceEntityMapping[] PIdJTCDMSC = {
        InheritanceEntityMapping.PIdJTCDMSCRootEntity,
        InheritanceEntityMapping.PIdJTCDMSCLeafA,
        InheritanceEntityMapping.PIdJTCDMSCEntityB,
        InheritanceEntityMapping.PIdJTCDMSCLeafB1,
        InheritanceEntityMapping.PIdJTCDMSCLeafB2,
        InheritanceEntityMapping.PIdJTCDMSCLeafC,
        InheritanceEntityMapping.PIdJTCDMSCEntityD,
        InheritanceEntityMapping.PIdJTCDMSCLeafD1,
        InheritanceEntityMapping.PIdJTCDMSCLeafD2
    };
    
    public final static InheritanceEntityMapping[] PIdJTSDMSC = {
        InheritanceEntityMapping.PIdJTSDMSCRootEntity,
        InheritanceEntityMapping.PIdJTSDMSCLeafA,
        InheritanceEntityMapping.PIdJTSDMSCEntityB,
        InheritanceEntityMapping.PIdJTSDMSCLeafB1,
        InheritanceEntityMapping.PIdJTSDMSCLeafB2,
        InheritanceEntityMapping.PIdJTSDMSCLeafC,
        InheritanceEntityMapping.PIdJTSDMSCEntityD,
        InheritanceEntityMapping.PIdJTSDMSCLeafD1,
        InheritanceEntityMapping.PIdJTSDMSCLeafD2
    };
    
    public final static InheritanceEntityMapping[] PIdSTIDMSC = {
        InheritanceEntityMapping.PIdSTIDMSCRootEntity,
        InheritanceEntityMapping.PIdSTIDMSCLeafA,
        InheritanceEntityMapping.PIdSTIDMSCEntityB,
        InheritanceEntityMapping.PIdSTIDMSCLeafB1,
        InheritanceEntityMapping.PIdSTIDMSCLeafB2,
        InheritanceEntityMapping.PIdSTIDMSCLeafC,
        InheritanceEntityMapping.PIdSTIDMSCEntityD,
        InheritanceEntityMapping.PIdSTIDMSCLeafD1,
        InheritanceEntityMapping.PIdSTIDMSCLeafD2
    };
    
    public final static InheritanceEntityMapping[] PIdSTCDMSC = {
        InheritanceEntityMapping.PIdSTCDMSCRootEntity,
        InheritanceEntityMapping.PIdSTCDMSCLeafA,
        InheritanceEntityMapping.PIdSTCDMSCEntityB,
        InheritanceEntityMapping.PIdSTCDMSCLeafB1,
        InheritanceEntityMapping.PIdSTCDMSCLeafB2,
        InheritanceEntityMapping.PIdSTCDMSCLeafC,
        InheritanceEntityMapping.PIdSTCDMSCEntityD,
        InheritanceEntityMapping.PIdSTCDMSCLeafD1,
        InheritanceEntityMapping.PIdSTCDMSCLeafD2
    };
    
    public final static InheritanceEntityMapping[] PIdSTSDMSC = {
        InheritanceEntityMapping.PIdSTSDMSCRootEntity,
        InheritanceEntityMapping.PIdSTSDMSCLeafA,
        InheritanceEntityMapping.PIdSTSDMSCEntityB,
        InheritanceEntityMapping.PIdSTSDMSCLeafB1,
        InheritanceEntityMapping.PIdSTSDMSCLeafB2,
        InheritanceEntityMapping.PIdSTSDMSCLeafC,
        InheritanceEntityMapping.PIdSTSDMSCEntityD,
        InheritanceEntityMapping.PIdSTSDMSCLeafD1,
        InheritanceEntityMapping.PIdSTSDMSCLeafD2
    };

    
    @SuppressWarnings("unchecked")
    public static Object createEntityObjectInstance(
            InheritanceEntityMapping eType) throws Exception {
        Class eClassType = eType.getEntityClass();
        
        Constructor c = eClassType.getConstructor(new Class[] {});
        Object eObj = c.newInstance(new Object[] {});
        return eObj;
    }
}

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
package org.apache.openjpa.persistence.jdbc.schema;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.kernel.ClassTableJDBCSeq;
import org.apache.openjpa.jdbc.kernel.TableJDBCSeq;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.SequenceMetaData;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestSequenceGeneratorEnsureCapacityCall 
    extends org.apache.openjpa.persistence.jdbc.kernel.TestSQLListenerTestCase {
   
    
    
    /** Creates a new instance of TestSequenceGeneratorEnsureCapacityCall */
    public TestSequenceGeneratorEnsureCapacityCall(String name) 
    {
    	super(name);
    }

    // class SeqA uses the default sequence factory.
    public void testDBSequenceFactory() {
        TableJDBCSeq seq = (TableJDBCSeq) seqForClass(SeqA.class);
        incrementTest(SeqA.class, seq.getAllocate());
    }

    // class SeqD uses the db-class sequence factory.
    public void testClassDBSequenceFactory() {
        ClassTableJDBCSeq seq = (ClassTableJDBCSeq) seqForClass(SeqD.class);
        incrementTest(SeqD.class, seq.getAllocate());
    }

    private void incrementTest(Class cls, int generatorIncrement) {
        // guaranteed to be more than a single batch, so ensureCapacity should
        // do its thing.
        int amountToIncrement = generatorIncrement * 2;

        OpenJPAEntityManager pm =(OpenJPAEntityManager)currentEntityManager();
        //FIXME jthomas
        //Sequence s = pm.getIdentitySequence(cls);
        Sequence s =null;
        s.setAllocate(amountToIncrement);

        sql.clear();
        for (int i = 0; i < amountToIncrement; i++) {
            s.getIncrement();
            assertEquals("SQL list should have been empty on sequence"
                + " fetch #" + i + " of #" + amountToIncrement
                + ", but contained: " + sql, 0, sql.size());
        }
    }

    private Seq seqForClass(Class cls) {
        OpenJPAConfiguration conf = getConfiguration();
        ClassMetaData meta = conf.getMetaDataRepositoryInstance().getMetaData
            (cls, null, true);
        SequenceMetaData smeta = meta.getIdentitySequenceMetaData();
        return (smeta == null) ? conf.getSequenceInstance()
            : smeta.getInstance(null);
    }
}

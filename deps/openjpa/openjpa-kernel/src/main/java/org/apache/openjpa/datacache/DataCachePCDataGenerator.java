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
package org.apache.openjpa.datacache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PCDataGenerator;
import org.apache.openjpa.kernel.AbstractPCData;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import serp.bytecode.BCClass;
import serp.bytecode.BCField;
import serp.bytecode.BCMethod;
import serp.bytecode.Code;
import serp.bytecode.Instruction;
import serp.bytecode.JumpInstruction;

/**
 * A {@link PCDataGenerator} instance which generates properly
 * synchronized instances suitable for use in the cache. In addition,
 * proper timed behavior is added.
 *
 * @author Steve Kim
 * @since 0.3.3.0
 */
public class DataCachePCDataGenerator extends PCDataGenerator {

    public static final String POSTFIX = "datacache";

    private static final Set _synchs = new HashSet(Arrays.asList
        (new String []{ "getData", "setData", "clearData", "getImplData",
            "setImplData", "setIntermediate", "getIntermediate",
            "isLoaded", "setLoaded", "setVersion", "getVersion", "store"
        }));

    public DataCachePCDataGenerator(OpenJPAConfiguration conf) {
        super(conf);
    }

    protected String getUniqueName(Class type) {
        return super.getUniqueName(type) + POSTFIX;
    }

    protected void finish(DynamicPCData data, ClassMetaData meta) {
        int timeout = meta.getDataCacheTimeout();
        if (timeout > 0)
            ((Timed) data).setTimeout(timeout + System.currentTimeMillis());
        else
            ((Timed) data).setTimeout(-1);
    }

    protected void decorate(BCClass bc, ClassMetaData meta) {
        enhanceToData(bc);
        enhanceToNestedData(bc);
        replaceNewEmbeddedPCData(bc);
        addSynchronization(bc);
        addTimeout(bc);
    }

    private void enhanceToData(BCClass bc) {
        BCMethod meth = bc.declareMethod("toData", Object.class,
            new Class []{ FieldMetaData.class, Object.class, 
            StoreContext.class });
        Code code = meth.getCode(true);
        // if (fmd.isLRS ()))
        // 		return NULL;
        code.aload().setParam(0);
        code.invokevirtual().setMethod(FieldMetaData.class, "isLRS",
            boolean.class, null);
        JumpInstruction ifins = code.ifeq();
        code.getstatic().setField(AbstractPCData.class, "NULL", Object.class);
        code.areturn();
        // super.toData (fmd, val, ctx);
        ifins.setTarget(code.aload().setThis());
        code.aload().setParam(0);
        code.aload().setParam(1);
        code.aload().setParam(2);
        code.invokespecial().setMethod(AbstractPCData.class, "toData",
            Object.class, new Class[]{ FieldMetaData.class, Object.class,
            StoreContext.class });
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    private void enhanceToNestedData(BCClass bc) {
        BCMethod meth = bc.declareMethod("toNestedData", Object.class,
            new Class []{ ValueMetaData.class, Object.class, 
            StoreContext.class });
        Code code = meth.getCode(true);

        // if (val == null)
        // 		return null;
        code.aload().setParam(1);
        JumpInstruction ifins = code.ifnonnull();
        code.constant().setNull();
        code.areturn();

        // int type = vmd.getDeclaredTypeCode ();
        ifins.setTarget(code.aload().setParam(0));
        code.invokeinterface().setMethod(ValueMetaData.class,
            "getDeclaredTypeCode", int.class, null);
        int local = code.getNextLocalsIndex();
        code.istore().setLocal(local);

        // if (type != JavaTypes.COLLECTION &&
        // 	   type != JavaTypes.MAP &&
        // 	   type != JavaTypes.ARRAY)
        // 	   return super.toNestedData(type, val, ctx);
        // 	else
        // 		return NULL;
        Collection jumps = new ArrayList(3);
        code.iload().setLocal(local);
        code.constant().setValue(JavaTypes.COLLECTION);
        jumps.add(code.ificmpeq());
        code.iload().setLocal(local);
        code.constant().setValue(JavaTypes.MAP);
        jumps.add(code.ificmpeq());
        code.iload().setLocal(local);
        code.constant().setValue(JavaTypes.ARRAY);
        jumps.add(code.ificmpeq());
        code.aload().setThis();
        code.aload().setParam(0);
        code.aload().setParam(1);
        code.aload().setParam(2);
        code.invokespecial().setMethod(AbstractPCData.class, "toNestedData",
            Object.class, new Class[]{ ValueMetaData.class, Object.class,
            StoreContext.class });
        code.areturn();
        setTarget(code.getstatic().setField
            (AbstractPCData.class, "NULL", Object.class), jumps);
        code.areturn();
        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    private void replaceNewEmbeddedPCData(BCClass bc) {
        BCMethod meth = bc.declareMethod("newEmbeddedPCData", 
            AbstractPCData.class, new Class[]{ OpenJPAStateManager.class });
        Code code = meth.getCode(true);

        // return new DataCachePCDataImpl(sm.getObjectId(), sm.getMetaData());
        code.anew().setType(DataCachePCDataImpl.class);
        code.dup();
        code.aload().setParam(0);
        code.invokeinterface().setMethod(OpenJPAStateManager.class, "getId", 
            Object.class, null);
        code.aload().setParam(0);
        code.invokeinterface().setMethod(OpenJPAStateManager.class, 
            "getMetaData", ClassMetaData.class, null);
        code.invokespecial().setMethod(DataCachePCDataImpl.class, "<init>",
            void.class, new Class[] { Object.class, ClassMetaData.class });
        code.areturn();

        code.calculateMaxLocals();
        code.calculateMaxStack();
    }

    private void addTimeout(BCClass bc) {
        bc.declareInterface(DataCachePCData.class);
        bc.declareInterface(Timed.class);

        // public boolean isTimedOut ();
        BCField field = addBeanField(bc, "timeout", long.class);
        BCMethod meth = bc.declareMethod("isTimedOut", boolean.class, null);
        Code code = meth.getCode(true);

        // if (timeout == -1) ...
        code.aload().setThis();
        code.getfield().setField(field);
        code.constant().setValue(-1L);
        code.lcmp();
        JumpInstruction ifneg = code.ifeq();

        // if (timeout >= System.currentTimeMillis ())
        code.aload().setThis();
        code.getfield().setField(field);
        code.invokestatic().setMethod(System.class, "currentTimeMillis",
            long.class, null);
        code.lcmp();
        JumpInstruction ifnexp = code.ifge();

        // return true;
        code.constant().setValue(1);

        // ... else return false;
        JumpInstruction go2 = code.go2();
        Instruction flse = code.constant().setValue(0);
        ifneg.setTarget(flse);
        ifnexp.setTarget(flse);
        go2.setTarget(code.ireturn());

        code.calculateMaxStack();
        code.calculateMaxLocals();
    }

    private void addSynchronization(BCClass bc) {
        BCMethod[] methods = bc.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isPublic()
                && _synchs.contains(methods[i].getName()))
                methods[i].setSynchronized(true);
        }

        // add synchronized isLoaded call.
        // public synchronized boolean isLoaded (int field)
        // {
        // 		return super.isLoaded (field);
        // }
        BCMethod method = bc.declareMethod("isLoaded", boolean.class,
            new Class[]{ int.class });
        method.setSynchronized(true);
        Code code = method.getCode(true);
        code.aload().setThis();
        code.iload().setParam(0);
        code.invokespecial().setMethod(AbstractPCData.class, "isLoaded",
            boolean.class, new Class[]{ int.class });
        code.calculateMaxLocals();
        code.calculateMaxStack();
        code.ireturn();
    }

    /**
     * Simple interface to give access to expiration time.
     */
    public static interface Timed {

        public void setTimeout(long time);
    }
}

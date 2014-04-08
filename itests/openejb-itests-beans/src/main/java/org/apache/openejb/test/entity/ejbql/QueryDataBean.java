/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.ejbql;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

public abstract class QueryDataBean implements EntityBean {
    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract boolean getBooleanField();
    public abstract void setBooleanField(boolean value);

    public abstract char getCharField();
    public abstract void setCharField(char value);

    public abstract byte getByteField();
    public abstract void setByteField(byte value);

    public abstract short getShortField();
    public abstract void setShortField(short value);

    public abstract int getIntField();
    public abstract void setIntField(int value);

    public abstract long getLongField();
    public abstract void setLongField(long value);

    public abstract float getFloatField();
    public abstract void setFloatField(float value);

    public abstract double getDoubleField();
    public abstract void setDoubleField(double value);

    public abstract String getStringField();
    public abstract void setStringField(String value);

    public Integer ejbCreate(int value) {
        setId(value);
        setBooleanField(value == 2);
        setCharField((char) ('0' + value));
        setByteField((byte) value);
        setShortField((short) value);
        setIntField(value);
        setLongField(value);
        setFloatField(value);
        setDoubleField(value);
        setStringField("" + value);
        return null;
    }

    public void ejbPostCreate(int field) {
    }

    public void setEntityContext(EntityContext ctx) {
    }

    public void unsetEntityContext() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }
}

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

import javax.ejb.EJBLocalObject;

public interface QueryDataLocal extends EJBLocalObject {
    public Integer getId();
    public void setId(Integer id);

    public boolean getBooleanField();
    public void setBooleanField(boolean value);

    public char getCharField();
    public void setCharField(char value);

    public byte getByteField();
    public void setByteField(byte value);

    public short getShortField();
    public void setShortField(short value);

    public int getIntField();
    public void setIntField(int value);

    public long getLongField();
    public void setLongField(long value);

    public float getFloatField();
    public void setFloatField(float value);

    public double getDoubleField();
    public void setDoubleField(double value);

    public String getStringField();
    public void setStringField(String value);

}

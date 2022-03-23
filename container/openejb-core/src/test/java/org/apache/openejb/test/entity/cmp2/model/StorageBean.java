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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.cmp2.model;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EntityBean;
import jakarta.ejb.EntityContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class StorageBean implements EntityBean {
    private EntityContext ctx;

    // CMP
    public abstract Integer getId();

    public abstract void setId(Integer primaryKey);

    public abstract byte[] getBlob();

    public abstract void setBlob(byte[] blob);

    public abstract char getChar();

    public abstract void setChar(char value);

    public void setBytes(final byte[] bytes) {
        try {
            final DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/DefaultDatabase");
            final Connection c = ds.getConnection();
            final PreparedStatement ps = c.prepareStatement("UPDATE storage SET blob_column = ? WHERE id = ?");
            ps.setBinaryStream(1, new ByteArrayInputStream(bytes), bytes.length);
            ps.setInt(2, (Integer) ctx.getPrimaryKey());
            ps.executeUpdate();
            ps.close();
            c.close();
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

    public byte[] getBytes() {
        try {
            final DataSource ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/DefaultDatabase");
            final Connection c = ds.getConnection();
            final PreparedStatement ps = c.prepareStatement("SELECT blob_column FROM storage WHERE id = ?");
            ps.setInt(1, (Integer) ctx.getPrimaryKey());
            final ResultSet rs = ps.executeQuery();
            rs.next();
            final InputStream is = rs.getBinaryStream(1);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int count;
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
            is.close();
            os.close();
            rs.close();
            ps.close();
            c.close();
            return os.toByteArray();
        } catch (final Exception e) {
            throw new EJBException(e);
        }
    }

    public Integer ejbCreate(final Integer id) throws CreateException {
        setId(id);
        return null;
    }

    public void ejbPostCreate(final Integer id) {
    }

    public void ejbLoad() {
    }

    public void setEntityContext(final EntityContext ctx) {
        this.ctx = ctx;
    }

    public void unsetEntityContext() {
        this.ctx = null;
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }
}
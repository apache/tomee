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
package org.apache.openjpa.persistence.fields;

import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestEnumsInJPQL
    extends SingleEMTestCase {

    public void setUp() {
        setUp(EnumFieldType.class, CLEAR_TABLES);

        EnumFieldType o = new EnumFieldType();
        o.setEnumField(SampleEnum.BAR);
        o.getEnumList().add(SampleEnum.FOO);

        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
    }

    public void testEnumLiteralInSelect() {
        Query q = em.createQuery("select count(o) from EnumFieldType o where " +
            "o.enumField = " +
            "org.apache.openjpa.persistence.fields.SampleEnum.BAR");
        assertEquals(1, ((Number) q.getSingleResult()).intValue());
    }

    public void testEnumLiteralInSetInUpdate() {
        testEnumLiteralInSelect();
        em.getTransaction().begin();
        Query q = em.createQuery("update EnumFieldType o set " +
            "o.enumField = " +
            "org.apache.openjpa.persistence.fields.SampleEnum.BAZ");
        assertEquals(1, ((Number) q.executeUpdate()).intValue());
        em.getTransaction().commit();
        postUpdateCheck(true);
    }

    public void testEnumLiteralInWhereInUpdate() {
        testEnumLiteralInSelect();
        em.getTransaction().begin();
        Query q = em.createQuery("update EnumFieldType o set o.intField = 3 " +
            "where o.enumField = " +
            "org.apache.openjpa.persistence.fields.SampleEnum.BAR");
        assertEquals(1, ((Number) q.executeUpdate()).intValue());
        em.getTransaction().commit();
        postUpdateCheck(false);
    }

    private void postUpdateCheck(boolean wasEnumModified) {
        Query q = em.createQuery("select count(o) from EnumFieldType o where " +
            "o.enumField = " +
            "org.apache.openjpa.persistence.fields.SampleEnum.BAR");
        assertEquals(wasEnumModified ? 0 : 1,
            ((Number) q.getSingleResult()).intValue());

        q = em.createQuery("select count(o) from EnumFieldType o where " +
            "o.enumField = " +
            "org.apache.openjpa.persistence.fields.SampleEnum.BAZ");
        assertEquals(wasEnumModified ? 1 : 0,
            ((Number) q.getSingleResult()).intValue());
    }

    public void testEnumPositionalParamInSelect() {
        Query q = em.createQuery("select count(o) from EnumFieldType o where " +
            "o.enumField = ?1");
        q.setParameter(1, SampleEnum.BAR);
        assertEquals(1, ((Number) q.getSingleResult()).intValue());
    }

    public void testEnumNamedParamInSelect() {
        Query q = em.createQuery("select count(o) from EnumFieldType o where " +
            "o.enumField = :e");
        q.setParameter("e", SampleEnum.BAR);
        assertEquals(1, ((Number) q.getSingleResult()).intValue());
    }

    public void testEnumParamInSetInUpdate() {
        testEnumLiteralInSelect();
        em.getTransaction().begin();
        Query q = em.createQuery("update EnumFieldType o set o.enumField = :e");
        q.setParameter("e", SampleEnum.BAZ);
        assertEquals(1, ((Number) q.executeUpdate()).intValue());
        em.getTransaction().commit();
        postUpdateCheck(true);
    }

    public void testEnumParamInWhereInUpdate() {
        testEnumLiteralInSelect();
        em.getTransaction().begin();
        Query q = em.createQuery("update EnumFieldType o set o.intField = 3 " +
            "where o.enumField = :e");
        q.setParameter("e", SampleEnum.BAR);
        assertEquals(1, ((Number) q.executeUpdate()).intValue());
        em.getTransaction().commit();
        postUpdateCheck(false);
    }

    public void testMemberOf() {
        assertEquals(Long.valueOf(1),
            em.createQuery("select count(o) from EnumFieldType o where " +
                "(:param member of o.enumList or :param2 member of o.enumList)")
                .setParameter("param", SampleEnum.FOO)
                .setParameter("param2", SampleEnum.BAR)
                .getSingleResult());
    }
}

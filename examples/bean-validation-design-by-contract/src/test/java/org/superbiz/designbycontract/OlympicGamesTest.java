package org.superbiz.designbycontract;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.validation.ConstraintViolationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OlympicGamesTest {

    private static EJBContainer ejbContainer;

    @EJB
    private OlympicGamesManager gamesManager;

    @EJB
    private PoleVaultingManagerBean poleVaultingManager;

    @BeforeClass
    public static void start() {
        ejbContainer = EJBContainer.createEJBContainer();
    }

    @Before
    public void inject() throws Exception {
        ejbContainer.getContext().bind("inject", this);
    }

    @AfterClass
    public static void stop() throws Exception {
        if (ejbContainer != null) {
            ejbContainer.close();
        }
    }

    @Test
    public void sportMenOk() throws Exception {
        assertEquals("IWin [FR]", gamesManager.addSportMan("IWin", "FR"));
    }

    @Test
    public void sportMenKoBecauseOfName() throws Exception {
        try {
            gamesManager.addSportMan("I lose", "EN");
            fail("no space should be in names");
        } catch (final EJBException wrappingException) {
            assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
            assertEquals(1, exception.getConstraintViolations().size());
        }
    }

    @Test
    public void sportMenKoBecauseOfCountry() throws Exception {
        try {
            gamesManager.addSportMan("ILoseTwo", "TOO-LONG");
            fail("country should be between 2 and 4 characters");
        } catch (final EJBException wrappingException) {
            assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
            assertEquals(1, exception.getConstraintViolations().size());
        }
    }

    @Test
    public void polVaulting() throws Exception {
        assertEquals(100, poleVaultingManager.points(220));
    }

    @Test
    public void tooShortPolVaulting() throws Exception {
        try {
            poleVaultingManager.points(119);
            fail("the jump is too short");
        } catch (final EJBException wrappingException) {
            assertTrue(wrappingException.getCause() instanceof ConstraintViolationException);
            ConstraintViolationException exception = ConstraintViolationException.class.cast(wrappingException.getCausedByException());
            assertEquals(1, exception.getConstraintViolations().size());
        }
    }
}

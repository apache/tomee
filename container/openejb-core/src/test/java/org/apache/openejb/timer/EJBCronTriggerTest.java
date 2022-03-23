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
package org.apache.openejb.timer;

import org.apache.openejb.core.timer.EJBCronTrigger;
import org.apache.openejb.core.timer.EJBCronTrigger.ParseException;
import org.apache.openejb.core.timer.TimerExpiredException;
import org.junit.Test;

import jakarta.ejb.ScheduleExpression;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class EJBCronTriggerTest {

    @Test
    public void shouldBeAbleToCreateExpiredTrigger() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2008).month(12).dayOfMonth(1).end(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertNotNull(trigger);
    }

    @Test(expected = TimerExpiredException.class)
    public void computeFailsOnExpiredTriggers() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2008).month(12).dayOfMonth(1).end(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertNotNull(trigger);
        trigger.computeFirstFireTime(null);
    }

    @Test(timeout = 1000)
    public void testSimpleDate() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2008).month(12).dayOfMonth(1).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        Calendar calendar = new GregorianCalendar(2008, 1, 1);
        final Date firstTime = trigger.getFireTimeAfter(calendar.getTime());
        final Date finalTime = trigger.getFinalFireTime();

        // The trigger only fires once so these should be the same
        assertEquals(firstTime, finalTime);

        // Let's make sure that single fire time is what we wanted
        calendar = new GregorianCalendar(2008, 11, 1);
        assertEquals(calendar.getTime(), firstTime);
    }

    @Test(timeout = 1000)
    public void testWeekdaysA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2008).dayOfWeek("Wed").start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);

        // Should fire on January 16th
        Calendar calendar = new GregorianCalendar(2008, 0, 16);
        final Date startTime = new Date(calendar.getTimeInMillis() - 1000);
        assertEquals(calendar.getTime(), trigger.getFireTimeAfter(startTime));

        // And for the last time, on the 31st of December
        calendar = new GregorianCalendar(2008, 11, 31);
        final Date expectedTime = calendar.getTime();
        assertEquals(expectedTime, trigger.getFinalFireTime());
    }


    @Test(timeout = 1000)
    public void testIncrementsA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2008).month(1).dayOfMonth(20)
            .hour("6/3").minute(30).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);

        // Should fire on Sunday, January 20th, first at 6:30
        Calendar calendar = new GregorianCalendar(2008, 0, 20);
        Date startTime = new Date(calendar.getTimeInMillis() - 1000);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 30);
        assertEquals(calendar.getTime(), trigger.getFireTimeAfter(startTime));

        // Next on 9:30
        startTime = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        assertEquals(calendar.getTime(), trigger.getFireTimeAfter(startTime));

        // Won't be fired after the 20th so it should return null
        calendar = new GregorianCalendar(2008, 0, 21);
        startTime = new Date(calendar.getTimeInMillis());
        assertNull(trigger.getFireTimeAfter(startTime));
    }

    @Test(timeout = 1000)
    public void testIncrementsB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).month(5).dayOfMonth(5).hour("23").minute("25/35").start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 5, 23, 25, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 5, 22, 0, 0).getTime()));
    }

    @Test(timeout = 1000)
    public void testIncrementsC() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).month(5).dayOfMonth(5).hour("*").minute("20/40").start(new GregorianCalendar(2011, 4, 5, 10, 21, 0).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 5, 11, 20, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 5, 10, 21, 0).getTime()));
    }


    @Test(timeout = 1000)
    public void testEndTime() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth(20).dayOfWeek("sat").start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);

        // Should not be fired at all since the first Saturday the 20th is in September
        Calendar calendar = new GregorianCalendar(2008, 0, 4);
        trigger.setEndTime(calendar.getTime());
        calendar = new GregorianCalendar(2008, 0, 1);
        assertNull(trigger.getFireTimeAfter(calendar.getTime()));
        //Since we did not specify the start time, the trigger will backward until finding the target time
        //assertNull(trigger.getFinalFireTime());
    }

    @Test(timeout = 5000)
    public void testSecond() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().hour("*").minute("*").second(5).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 1, 5, 0, 0, 5).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 5, 0, 0, 4).getTime()));
        assertEquals(new GregorianCalendar(2011, 1, 5, 0, 1, 5).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 5, 0, 0, 6).getTime()));
    }


    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5").dayOfWeek("6").year(2010).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 3, 0, 0, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("last").dayOfWeek("3").year(2011).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 11, 0, 0, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 7, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekC() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("18").dayOfWeek("3").hour(23).minute(59).second(58).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 25, 23, 59, 58).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 23, 59, 59).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekD() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("19").dayOfWeek("3").hour(23).minute(59).second(59).start(new GregorianCalendar(2011, 4, 18, 23, 59, 58).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 18, 23, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 23, 59, 58).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekE() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("19").dayOfWeek("3").hour(23).minute(59).second(58).start(new GregorianCalendar(2011, 4, 18, 23, 59, 58).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 19, 23, 59, 58).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 23, 59, 59).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekF() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("19").dayOfWeek("3").hour(20).minute(59).second(59).start(new GregorianCalendar(2011, 4, 18, 20, 59, 58).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 18, 20, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 20, 59, 58).getTime()));
        assertEquals(new GregorianCalendar(2011, 4, 19, 20, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 20, 59, 59).getTime()));
    }

    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekG() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("12").dayOfWeek("6").hour(20).minute(59).second(59).start(new GregorianCalendar(2011, 5, 11, 20, 59, 58).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        //assertEquals(new GregorianCalendar(2011, 5, 11, 20, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 5, 11, 20, 59, 58).getTime()));
        assertEquals(new GregorianCalendar(2011, 5, 12, 20, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 5, 11, 20, 59, 59).getTime()));
    }


    @Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekH() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).dayOfMonth("28").dayOfWeek("3").hour(20).minute(59).second(59).start(new GregorianCalendar(2011, 5, 11, 20, 59, 58).getTime());
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 6, 28, 20, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 6, 27, 21, 59, 59).getTime()));
    }

    @Test(timeout = 5000)
    public void testLastDayOfMonthA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("Last").hour(23).minute(59).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2009, 1, 28, 23, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 0, 0, 0).getTime()));
        //Test Leap year
        assertEquals(new GregorianCalendar(2000, 1, 29, 23, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testLastDayOfMonthB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).month(6).dayOfMonth("Last").hour(23).minute(59).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 5, 30, 23, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 1, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testMinusDayOfMonth() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth(-2).hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2009, 1, 26, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 0, 0, 0).getTime()));
        //Test Leap year
        assertEquals(new GregorianCalendar(2000, 1, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 26, 0, 0, 0).getTime()));
        //Test next month
        assertEquals(new GregorianCalendar(2000, 1, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 26, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testOrdinalNumbersDayOfMonthA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("2nd mon").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 9, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 1, 0, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2011, 5, 13, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 10, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testOrdinalNumbersDayOfMonthB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("last mon").hour(23).minute(1).second(59);
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2100, 1, 22, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2100, 1, 1, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testOrdinalNumbersDayOfMonthC() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("last sun").hour(23).minute(1).second(59);
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2100, 1, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2100, 1, 1, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testOrdinalNumbersDayOfMonthD() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("1st sun").hour(23).minute(1).second(59);
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2100, 1, 7, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2100, 1, 1, 0, 0, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testOrdinalNumbersDayOfMonthE() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5th Sun-Last Sun").hour(23).minute(1).second(59);
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2100, 0, 31, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2100, 0, 1, 0, 0, 0).getTime()));
    }


    @Test(timeout = 500)
    public void testSimpleDayOfWeek() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("7").hour(23).minute(1).second(59).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 5, 23, 1, 30).getTime()));
    }

    @Test(timeout = 500)
    public void testSimpleDayOfWeekA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("0").hour(23).minute(1).second(59).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 5, 23, 1, 30).getTime()));
    }

    @Test(timeout = 500)
    public void testSimpleDayOfWeekB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("5").hour(14).minute(1).second(59).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 6, 14, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 5, 23, 1, 30).getTime()));
    }

    @Test(timeout = 5000)
    public void testSimpleDayOfWeekC() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year(2011).month(6).dayOfWeek("3").hour(22).minute(1).second(1).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(null, trigger.getFireTimeAfter(new GregorianCalendar(2011, 5, 29, 23, 1, 1).getTime()));
    }


    @Test(timeout = 5000)
    public void testRangeYearsA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year("2009-2013").month(2).dayOfMonth(29).hour(23).minute(1).second(0).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2012, 1, 29, 23, 1, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 23, 0, 0).getTime()));

    }

    @Test(timeout = 500)
    public void testRangeYearsB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year("2013-2016").month(2).dayOfMonth(29).hour(23).minute(1).second(0).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2016, 1, 29, 23, 1, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 1, 23, 0, 0).getTime()));

    }

    @Test(timeout = 500)
    public void testRangeMonthA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().year("2011").month("dec-dec").dayOfMonth(29).hour(23).minute(1).second(0).start(new Date(0));
        ;
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 11, 29, 23, 1, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 23, 0, 0).getTime()));

    }


    @Test(timeout = 5000)
    public void testRangeDayOfMonthA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("3-27").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 4, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 3, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 26, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 7, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 2, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testRangeDayOfMonthB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("27-3").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 2, 0).getTime()));
    }

    @Test(timeout = 500)
    public void testRangeDayOfMonthC() throws ParseException {
        //7-27
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("1st Sat - 4th FRI").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 4, 7, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2011, 4, 9, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 8, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2011, 4, 18, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 18, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2011, 4, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 26, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2011, 5, 4, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 4, 28, 23, 3, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testRangeDayOfMonthD() throws ParseException {
        //current day is later than start day of range.
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("-7-1").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 5, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 5, 28, 23, 0, 0).getTime()));
    }

    @Test(timeout = 500)
    public void testRangeDayOfWeekA() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("tue-fri").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
    }

    @Test(timeout = 5000000)
    public void testRangeDayOfWeekB() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("fri-tue").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
    }

    @Test(timeout = 500)
    public void testRangeCDayOfWeek() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfWeek("0-7").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
    }


    @Test(timeout = 5000)
    public void testListDayOfMonth() throws ParseException {
        final ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5,10,24").hour(23).minute(1).second(59).start(new Date(0));
        final EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
    }

    @Test(timeout = 5000)
    public void testCompoundListDayOfMonth() throws ParseException {
        final String[] dayOfMonthsA = {"5,6-8,10,24", "5,10,24,6-8", "5,10,24,6-8,7"};
        for (final String dayOfMonth : dayOfMonthsA) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 7, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
        }
        final String[] dayOfMonthsB = {"5,6-8,10,24", "5,10,24,6-8", "5,10,24,6-8,7", "5,10,7,24,6-8"};
        for (final String dayOfMonth : dayOfMonthsB) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 7, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 6, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
        }
        final String[] dayOfMonthsC = {"5, 10,26,25-3", "5,25-3,10,26", "5,25-LAST,1-3,26,10", "5,5,10,10,25-26,26-3", "5,5,10,10,25-26,26-LAST, 1-3"};
        for (final String dayOfMonth : dayOfMonthsC) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 30, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 25, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 3, 0).getTime()));
            //Test Leap year
            assertEquals(new GregorianCalendar(2012, 0, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 0, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 0, 31, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 1, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 5, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 10, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 25, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 10, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 26, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 28, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 28, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 1, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2012, 2, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2012, 1, 29, 23, 3, 0).getTime()));
        }
    }

    @Test(timeout = 5000)
    public void testListDayOfWeekA() throws ParseException {
        final String[] dayOfWeeks = {"tue,wed,thu,fri", "wed,tue,thu,fri", "tue,wed,thu,fri,tue"};
        for (final String dayOfWeek : dayOfWeeks) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfWeek(dayOfWeek).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
        }
    }

    @Test(timeout = 5000)
    public void testListDayOfWeekB() throws ParseException {
        final String[] dayOfWeeks = {"2,3,4,5", "3,2,4,5", "2,3,4,5,2"};
        for (final String dayOfWeek : dayOfWeeks) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfWeek(dayOfWeek).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
        }
    }


    @Test(timeout = 5000)
    public void testCompoundListDayOfWeek() throws ParseException {
        final String[] dayOfWeeks = {"tue,wed,thu-fri", "wed,thu-fri,tue", "tue,wed,thu,thu-fri,fri,tue"};
        for (final String dayOfWeek : dayOfWeeks) {
            final ScheduleExpression expr = new ScheduleExpression().dayOfWeek(dayOfWeek).hour(23).minute(1).second(59).start(new Date(0));
            final EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
        }
    }


    @Test(timeout = 500)
    public void testInvalidSingleInputs() throws ParseException {

        // invalid day of month
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth(-8).hour(23).minute(1).second(59).start(new Date(0));

        boolean parseExceptionThrown = false;
        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  year
        expr = new ScheduleExpression().year(98).month(5).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  month
        expr = new ScheduleExpression().month(-4).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  days in week
        expr = new ScheduleExpression().month(-4).dayOfWeek(9).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  month
        expr = new ScheduleExpression().month("XXXX").dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  hour
        expr = new ScheduleExpression().dayOfMonth(6).hour("-4").minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  hour
        expr = new ScheduleExpression().dayOfMonth(6).hour("24/2").minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  minute
        expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(-1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  second
        expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(1).second(-4).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


    }

    @Test(timeout = 500)
    public void testInvalidListInputs() throws ParseException {

        // invalid day of month
        final String invalid_day_of_month = "2ndXXX,-8";
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("1stsun,4,6," + invalid_day_of_month).hour(23).minute(1).second(59).start(new Date(0));

        boolean parseExceptionThrown = false;
        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  year
        final String invalid_years = "19876,87";
        expr = new ScheduleExpression().year("1999,2012" + invalid_years).month(5).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  month
        final String invalid_month = "XXX,14";
        expr = new ScheduleExpression().month("1,2,4,sep," + invalid_month).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  days in week
        final String invalid_days_in_week = "8,WEEE";
        expr = new ScheduleExpression().month(5).dayOfWeek("SUN,4,5," + invalid_days_in_week).hour(2).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);


        // invalid  hours
        final String invalid_hours = "15,-2";

        expr = new ScheduleExpression().dayOfMonth(6).hour("1,5,9,18,22," + invalid_hours).minute(1).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  minute
        final String invalid_minutes = "61,-4";
        expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute("1,45,58," + invalid_minutes).second(59).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

        // invalid  second
        final String invalid_seconds = "61,-4";
        expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(1).second("1,45,58," + invalid_seconds).start(new Date(0));
        parseExceptionThrown = false;

        try {
            new EJBCronTrigger(expr);
        } catch (final ParseException e) {
            parseExceptionThrown = true;
        }
        assertTrue(parseExceptionThrown);

    }


}

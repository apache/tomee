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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.ScheduleExpression;

import org.apache.openejb.core.timer.EJBCronTrigger;
import org.apache.openejb.core.timer.EJBCronTrigger.ParseException;
import org.junit.Test;

public class EJBCronTriggerTest {

	@Test(timeout = 1000000)
	public void testSimpleDate() throws ParseException {
		ScheduleExpression expr = new ScheduleExpression().year(2008).month(12).dayOfMonth(1).start(new Date(0));
		EJBCronTrigger trigger = new EJBCronTrigger(expr);
		Calendar calendar = new GregorianCalendar(2008, 1, 1);
		Date firstTime = trigger.getFireTimeAfter(calendar.getTime());
		Date finalTime = trigger.getFinalFireTime();

		// The trigger only fires once so these should be the same
		assertEquals(firstTime, finalTime);

		// Let's make sure that single fire time is what we wanted
		calendar = new GregorianCalendar(2008, 11, 1);
		assertEquals(calendar.getTime(), firstTime);
	}

	@Test(timeout = 1000)
	public void testWeekdays() throws ParseException {
		ScheduleExpression expr = new ScheduleExpression().year(2008).dayOfWeek("Wed").start(new Date(0));
		EJBCronTrigger trigger = new EJBCronTrigger(expr);

		// Should fire on January 16th
		Calendar calendar = new GregorianCalendar(2008, 0, 16);
		Date startTime = new Date(calendar.getTimeInMillis() - 1000);
		assertEquals(calendar.getTime(), trigger.getFireTimeAfter(startTime));

		// And for the last time, on the 31st of December
		calendar = new GregorianCalendar(2008, 11, 31);
		Date expectedTime = calendar.getTime();
		assertEquals(expectedTime, trigger.getFinalFireTime());
	}
	
	

	@Test(timeout = 1000)
	public void testIncrements() throws ParseException {
		ScheduleExpression expr = new ScheduleExpression().year(2008).month(1).dayOfMonth(20)
				.dayOfWeek("sun").hour("6/3").minute(30).start(new Date(0));
		EJBCronTrigger trigger = new EJBCronTrigger(expr);

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
	public void testEndTime() throws ParseException {
		ScheduleExpression expr = new ScheduleExpression().dayOfMonth(20).dayOfWeek("sat").start(new Date(0));
		EJBCronTrigger trigger = new EJBCronTrigger(expr);

		// Should not be fired at all since the first Saturday the 20th is in September
		Calendar calendar = new GregorianCalendar(2008, 6, 1);
		trigger.setEndTime(calendar.getTime());
		calendar = new GregorianCalendar(2008, 0, 1);
		assertNull(trigger.getFireTimeAfter(calendar.getTime()));
		//Since we did not specify the start time, the trigger will backward until finding the target time
		//assertNull(trigger.getFinalFireTime());
	}

	@Test(timeout = 5000)
	public void testSecond() throws ParseException {
	    ScheduleExpression expr = new ScheduleExpression().hour("*").minute("*").second(5).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 1, 5, 0, 0, 5).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 5, 0, 0, 4).getTime()));
        assertEquals(new GregorianCalendar(2011, 1, 5, 0, 1, 5).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2011, 1, 5, 0, 0, 6).getTime()));
	}

	@Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekNullValue() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5").dayOfWeek("6").year(2010).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertNull(trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 0, 0, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testBothDayOfMonthAndDayOfWeekNonNullValue() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5").dayOfWeek("6").year(2011).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2011, 1, 5, 0, 0, 0).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 0, 0, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testLastDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("Last").hour(23).minute(59).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2009, 1, 28, 23, 59, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 0, 0, 0).getTime()));
        //Test Leap year
        assertEquals(new GregorianCalendar(2000,1,29,23,59,59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 28, 0, 0, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testMinusDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth(-2).hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2009, 1, 26, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2009, 1, 1, 0, 0, 0).getTime()));
        //Test Leap year
        assertEquals(new GregorianCalendar(2000,1,27,23,1,59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 26, 0, 0, 0).getTime()));
        //Test next month
        assertEquals(new GregorianCalendar(2000,1,27,23,1,59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2000, 1, 26, 0, 0, 0).getTime()));
    }

	@Test(timeout = 5000000)
    public void testOrdinalNumbersDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("2nd mon").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 12, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 0, 0, 0).getTime()));
    }


	@Test(timeout = 5000)
    public void testRangeADayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("3-27").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 4, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 3, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 26, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 7, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 2, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testRangeBDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("27-3").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 2, 0).getTime()));
    }
	
    @Test(timeout = 5000000)
    public void testRangeCDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("Last Fri - 1st Mon").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 26, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 27, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 28, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 27, 23, 2, 0).getTime()));
    }	
	
	

	@Test(timeout = 5000)
    public void testRangeADayOfWeek() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfWeek("tue-fri").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testRangeBDayOfWeek() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfWeek("fri-tue").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 3, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testListDayOfMonth() throws ParseException {
        ScheduleExpression expr = new ScheduleExpression().dayOfMonth("5,10,24").hour(23).minute(1).second(59).start(new Date(0));
        EJBCronTrigger trigger = new EJBCronTrigger(expr);
        assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 0, 0).getTime()));
        assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
    }

	@Test(timeout = 5000)
    public void testCompoundListDayOfMonth() throws ParseException {
        String[] dayOfMonthsA = { "5,6-8,10,24", "5,10,24,6-8", "5,10,24,6-8,7" };
        for (String dayOfMonth : dayOfMonthsA) {
            ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 7, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
        }
        String[] dayOfMonthsB = { "5,6-8,10,24", "5,10,24,6-8", "5,10,24,6-8,7", "5,10,7,24,6-8" };
        for (String dayOfMonth : dayOfMonthsB) {
            ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 6, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 5, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 7, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 6, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 8, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 8, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 10, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 24, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 10, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 7, 5, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 24, 23, 3, 0).getTime()));
        }
        String[] dayOfMonthsC = { "5, 10,26,25-3", "5,25-3,10,26", "5,25-LAST,1-3,26,10", "5,5,10,10,25-26,26-3", "5,5,10,10,25-26,26-LAST, 1-3" };
        for (String dayOfMonth : dayOfMonthsC) {
            ScheduleExpression expr = new ScheduleExpression().dayOfMonth(dayOfMonth).hour(23).minute(1).second(59).start(new Date(0));
            EJBCronTrigger trigger = new EJBCronTrigger(expr);
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
    public void testListDayOfWeek() throws ParseException {
        String[] dayOfWeeks = { "tue,wed,thu,fri", "wed,tue,thu,fri", "tue,wed,thu,fri,tue" };
        for (String dayOfWeek : dayOfWeeks) {
            ScheduleExpression expr = new ScheduleExpression().dayOfWeek(dayOfWeek).hour(23).minute(1).second(59).start(new Date(0));
            EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
        }
    }

	@Test(timeout = 5000)
    public void testCompoundListDayOfWeek() throws ParseException {
        String[] dayOfWeeks = { "tue,wed,thu-fri", "wed,thu-fri,tue", "tue,wed,thu,thu-fri,fri,tue"};
        for (String dayOfWeek : dayOfWeeks) {
            ScheduleExpression expr = new ScheduleExpression().dayOfWeek(dayOfWeek).hour(23).minute(1).second(59).start(new Date(0));
            EJBCronTrigger trigger = new EJBCronTrigger(expr);
            assertEquals(new GregorianCalendar(2010, 5, 29, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 5, 30, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 5, 29, 23, 2, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 1, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 0, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 2, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 1, 23, 3, 0).getTime()));
            assertEquals(new GregorianCalendar(2010, 6, 6, 23, 1, 59).getTime(), trigger.getFireTimeAfter(new GregorianCalendar(2010, 6, 2, 23, 2, 0).getTime()));
        }
	}
	
	
	@Test(timeout = 50000000)
    public void testInvalidSingleInputs1() throws ParseException {
    
    // invalid  hour
    ScheduleExpression expr = new ScheduleExpression().dayOfMonth(6).hour("24/1").minute(1).second(59).start(new Date(0));
    boolean parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    
    }
	
	
	@Test(timeout = 500)
    public void testInvalidSingleInputs() throws ParseException {
	
	// invalid day of month
    ScheduleExpression expr = new ScheduleExpression().dayOfMonth(-8).hour(23).minute(1).second(59).start(new Date(0));
    
    boolean parseExceptionThrown = false;
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    
    // invalid  year
    expr = new ScheduleExpression().year(98).month(5).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);        
    
    // invalid  month
    expr = new ScheduleExpression().month(-4).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    // invalid  days in week
    expr = new ScheduleExpression().month(-4).dayOfWeek(9).hour(2).minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown); 
    
    
    // invalid  month
    expr = new ScheduleExpression().month("XXXX").dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);  
    
    // invalid  hour
    expr = new ScheduleExpression().dayOfMonth(6).hour("-4").minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    
    // invalid  hour
    expr = new ScheduleExpression().dayOfMonth(6).hour("24/2").minute(1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    
    // invalid  minute	
    expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(-1).second(59).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    // invalid  second      
    expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(1).second(-4).start(new Date(0));
    parseExceptionThrown = false;
    
    try {
        new EJBCronTrigger(expr);
    } catch (ParseException e){
        parseExceptionThrown=true;
    }
    assertTrue(parseExceptionThrown);
    
    
    }
	
	@Test(timeout = 500)
    public void testInvalidListInputs() throws ParseException {
    
	    // invalid day of month
	    String invalid_day_of_month="2ndXXX,-8";
	    ScheduleExpression expr = new ScheduleExpression().dayOfMonth("1stsun,4,6,"+invalid_day_of_month).hour(23).minute(1).second(59).start(new Date(0));
	    
	    boolean parseExceptionThrown = false;
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);
	    
	    
	    // invalid  year
	    String invalid_years = "19876,87";
	    expr = new ScheduleExpression().year("1999,2012"+invalid_years).month(5).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);        
	    
	    
	    // invalid  month
	    String invalid_month = "XXX,14";
	    expr = new ScheduleExpression().month("1,2,4,sep,"+invalid_month).dayOfMonth(6).hour(2).minute(1).second(59).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);
	    
	    // invalid  days in week
	    String invalid_days_in_week = "8,WEEE";
	    expr = new ScheduleExpression().month(5).dayOfWeek("SUN,4,5,"+ invalid_days_in_week).hour(2).minute(1).second(59).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown); 
	    
	    
	    
	    // invalid  hours
        String invalid_hours="15,-2";	    
	    
	    expr = new ScheduleExpression().dayOfMonth(6).hour("1,5,9,18,22,"+invalid_hours).minute(1).second(59).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);
	    
	    // invalid  minute  
	    String invalid_minutes="61,-4";
	    expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute("1,45,58,"+invalid_minutes).second(59).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);
	    
	    // invalid  second   
	    String invalid_seconds="61,-4";
	    expr = new ScheduleExpression().dayOfMonth(6).hour(2).minute(1).second("1,45,58,"+invalid_seconds).start(new Date(0));
	    parseExceptionThrown = false;
	    
	    try {
	        new EJBCronTrigger(expr);
	    } catch (ParseException e){
	        parseExceptionThrown=true;
	    }
	    assertTrue(parseExceptionThrown);
    
    }
	
	
}

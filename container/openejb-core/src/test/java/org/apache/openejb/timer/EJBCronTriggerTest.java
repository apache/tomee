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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ejb.ScheduleExpression;

import org.apache.openejb.core.timer.EJBCronTrigger;
import org.apache.openejb.core.timer.EJBCronTrigger.ParseException;
import org.junit.Test;

public class EJBCronTriggerTest {

	@Test(timeout = 1000)
	public void testSimpleDate() throws ParseException {
		ScheduleExpression expr = new ScheduleExpression().year(2008).month(12).dayOfMonth(1);
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
		ScheduleExpression expr = new ScheduleExpression().year(2008).dayOfWeek("Wed");
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
				.dayOfWeek("sun").hour("6/3").minute(30);
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
		ScheduleExpression expr = new ScheduleExpression().dayOfMonth(20).dayOfWeek("sat");
		EJBCronTrigger trigger = new EJBCronTrigger(expr);
		
		// Should not be fired at all since the first Saturday the 20th is in September
		Calendar calendar = new GregorianCalendar(2008, 6, 1);
		trigger.setEndTime(calendar.getTime());
		calendar = new GregorianCalendar(2008, 0, 1);
		assertNull(trigger.getFireTimeAfter(calendar.getTime()));
		assertNull(trigger.getFinalFireTime());
	}

}

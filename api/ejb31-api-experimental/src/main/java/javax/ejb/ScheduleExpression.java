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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.ejb;

import java.io.Serializable;
import java.util.Date;

public final class ScheduleExpression implements Serializable {
	private String dayOfMonth = "*";
	private String dayOfWeek = "*";
	private String hour = "0";
	private String minute = "0";
	private String month = "*";
	private String second = "0";
	private String year = "*";
	private String timezone;
	private Date start;
	private Date end;
	
	public ScheduleExpression dayOfMonth(int d) {
		dayOfMonth = Integer.toString(d);
		return this;
	}

	public ScheduleExpression dayOfMonth(String d) {
		dayOfMonth = d;
		return this;
	}

	public ScheduleExpression dayOfWeek(int d) {
		dayOfWeek = Integer.toString(d);
		return this;
	}

	public ScheduleExpression dayOfWeek(String d) {
		dayOfWeek = d;
		return this;
	}

	public ScheduleExpression end(Date e) {
		end = e;
		return this;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public Date getEnd() {
		return end;
	}

	public String getHour() {
		return hour;
	}

	public String getMinute() {
		return minute;
	}

	public String getMonth() {
		return month;
	}

	public String getSecond() {
		return second;
	}

	public Date getStart() {
		return start;
	}

	public String getYear() {
		return year;
	}
	
	public String getTimezone() {
		return timezone;
	}

	public ScheduleExpression hour(int h) {
		hour = Integer.toString(h);
		return this;
	}

	public ScheduleExpression hour(String h) {
		hour = h;
		return this;
	}

	public ScheduleExpression minute(int m) {
		minute = Integer.toString(m);
		return this;
	}

	public ScheduleExpression minute(String m) {
		minute = m;
		return this;
	}

	public ScheduleExpression month(int m) {
		month = Integer.toString(m);
		return this;
	}

	public ScheduleExpression month(String m) {
		month = m;
		return this;
	}

	public ScheduleExpression second(int s) {
		second = Integer.toString(s);
		return this;
	}

	public ScheduleExpression second(String s) {
		second = s;
		return this;
	}

	public ScheduleExpression start(Date s) {
		start = s;
		return this;
	}

	public ScheduleExpression year(int y) {
		year = Integer.toString(y);
		return this;
	}

	public ScheduleExpression year(String y) {
		year = y;
		return this;
	}

    /**
     * See http://en.wikipedia.org/wiki/List_of_zoneinfo_timezones for valid timezones 
     * @param t
     * @return
     */
	public ScheduleExpression timezone(String t) {
		timezone = t;
		return this;
	}
}

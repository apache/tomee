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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.timer;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.ScheduleExpression;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public class EJBCronTrigger extends Trigger {

    private static final Pattern INCREMENTS = Pattern.compile("(\\d+|\\*)/(\\d+)*");
	private static final Pattern LIST = Pattern.compile("\\p{Alnum}+(?:,\\p{Alnum}+)*");
	private static final Pattern RANGE = Pattern.compile("(\\p{Alnum}+)-(\\p{Alnum}+)");
	private static final Pattern WEEKDAY = Pattern.compile("(1ST|2ND|3RD|4TH|5TH|LAST)(\\p{Alpha}+)");
	private static final Pattern DAYS_TO_LAST = Pattern.compile("-([1-7])");

	private final List<FieldExpression> expressions = new ArrayList<FieldExpression>(7);
	
	// Optional settings
	private Date startTime;
	private Date endTime;
	
	// Internal variables
	private Date nextFireTime;
	private Date previousFireTime;

	public EJBCronTrigger(ScheduleExpression expr) throws ParseException {
		Map<Integer, String> fieldValues = new TreeMap<Integer, String>();
		fieldValues.put(Calendar.YEAR, expr.getYear());
		fieldValues.put(Calendar.MONTH, expr.getMonth());
		fieldValues.put(Calendar.DAY_OF_WEEK, expr.getDayOfWeek());
		fieldValues.put(Calendar.DAY_OF_MONTH, expr.getDayOfMonth());
		fieldValues.put(Calendar.HOUR_OF_DAY, expr.getHour());
		fieldValues.put(Calendar.MINUTE, expr.getMinute());
		fieldValues.put(Calendar.SECOND, expr.getSecond());
		
		// If parsing fails on a field, record the error and move to the next field
		Map<Integer, ParseException> errors = new HashMap<Integer, ParseException>();
		for (Entry<Integer, String> entry : fieldValues.entrySet()) {
			int field = entry.getKey();
			String value = entry.getValue();
			try {
				FieldExpression fieldExpr = parseExpression(field, value);
				if (fieldExpr != null) {
					expressions.add(fieldExpr);
				}
			} catch (ParseException e) {
				errors.put(field, e);
			}
		}

		// If there were parsing errors, throw a "master exception" that contains all
		// exceptions from individual fields
		if (!errors.isEmpty()) {
			throw new ParseException(errors);
		}
	}

	/**
	 * Computes a set of allowed values for the given field of a calendar based
	 * time expression.
	 * 
	 * @param field
	 *            field type from <code>java.util.Calendar</code>
	 * @param expr
	 *            a time expression
	 * @throws ParseException
	 *             when there is a syntax error in the expression, or its values
	 *             are out of range
	 */
	protected FieldExpression parseExpression(int field, String expr) throws ParseException {
		// Get rid of whitespace and convert to uppercase 
		expr = expr.replaceAll("\\s+", "").toUpperCase();

		if (expr.equals("*")) {
			return null;
		}

		Matcher m = RANGE.matcher(expr);
		if (m.matches()) {
			return new RangeExpression(m, field);
		}
		
		switch (field) {
		case Calendar.HOUR_OF_DAY:
		case Calendar.MINUTE:
		case Calendar.SECOND:
			m = INCREMENTS.matcher(expr);
			if (m.matches()) {
				return new IncrementExpression(m, field);
			}
			break;

		case Calendar.DAY_OF_MONTH:
			if (expr.equals("LAST")) {
				return new DaysFromLastDayExpression();
			}

			m = DAYS_TO_LAST.matcher(expr);
			if (m.matches()) {
				return new DaysFromLastDayExpression(m);
			}

			m = WEEKDAY.matcher(expr);
			if (m.matches()) {
				return new WeekdayExpression(m);
			}
			break;
		}

		m = LIST.matcher(expr);
		if (m.matches()) {
			return new ListExpression(m, field);
		}
		
		throw new ParseException(field, expr, "Unparseable time expression");
	}
	
	@Override
	public Date computeFirstFireTime(org.quartz.Calendar calendar) {
		// Copied from org.quartz.CronTrigger
        nextFireTime = getFireTimeAfter(new Date(getStartTime().getTime() - 1000l));

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }

        return nextFireTime;
	}

	@Override
	public int executionComplete(JobExecutionContext context,
			JobExecutionException result) {
		// Copied from org.quartz.CronTrigger
        if (result != null && result.refireImmediately()) {
            return INSTRUCTION_RE_EXECUTE_JOB;
        }

        if (result != null && result.unscheduleFiringTrigger()) {
            return INSTRUCTION_SET_TRIGGER_COMPLETE;
        }

        if (result != null && result.unscheduleAllTriggers()) {
            return INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE;
        }

        if (!mayFireAgain()) {
            return INSTRUCTION_DELETE_TRIGGER;
        }

        return INSTRUCTION_NOOP;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Works similarly to getFireTimeAfter() but backwards.
	 */
	@Override
	public Date getFinalFireTime() {
		Calendar calendar = new GregorianCalendar();
		calendar.setLenient(false);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		if (endTime == null) {
			// If the year field has been left default, there is no end time
			if (expressions.get(0).field != Calendar.YEAR) {
				return null;
			}
			resetFields(calendar, 0, true);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar.setTime(endTime);
		}
		
		// Calculate time to give up scheduling
		Calendar stopCalendar = new GregorianCalendar();
		if (startTime != null) {
			stopCalendar.setTime(startTime);
		} else {
			stopCalendar.setTimeInMillis(0);
		}

		ListIterator<FieldExpression> fieldIterator = expressions.listIterator();
		while (fieldIterator.hasNext() && calendar.after(stopCalendar)) {
			FieldExpression expr = fieldIterator.next();
			Integer value = expr.getPreviousValue(calendar);
			if (value != null) {
				int oldValue = calendar.get(expr.field);
				if (oldValue != value) {
					// The value has changed, so update the calendar and reset all
					// less significant fields
					calendar.set(expr.field, value);
					resetFields(calendar, expr.field, true);
					
					// If the weekday changed, the day of month changed too
					if (expr.field == Calendar.DAY_OF_WEEK) {
						fieldIterator.previous();
						fieldIterator.previous();
					}
				}
			} else if (fieldIterator.previousIndex() >= 1) {
				// No suitable value was found, so move back to the previous field
				// and decrease the value
				fieldIterator.previous();
				expr = fieldIterator.previous();
				calendar.add(expr.field, -1);
			} else {
				return null; // The job will never be run
			}
		}
		return calendar.after(stopCalendar) ? calendar.getTime() : null;
	}

	@Override
	public Date getFireTimeAfter(Date afterTime) {
		Calendar calendar = new GregorianCalendar();
		calendar.setLenient(false);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		// Calculate starting time
		if (startTime != null && startTime.after(afterTime)) {
			calendar.setTime(startTime);
		} else {
			calendar.setTime(afterTime);
			calendar.add(Calendar.SECOND, 1);
		}

		// Calculate time to give up scheduling
		Calendar stopCalendar = new GregorianCalendar();
		if (endTime != null) {
			stopCalendar.setTime(endTime);
		} else {
			int stopYear = calendar.get(Calendar.YEAR) + 100;
			stopCalendar.set(Calendar.YEAR, stopYear);
		}

		ListIterator<FieldExpression> fieldIterator = expressions.listIterator();
		while (fieldIterator.hasNext() && calendar.before(stopCalendar)) {
			FieldExpression expr = fieldIterator.next();
			Integer value = expr.getNextValue(calendar);
			if (value != null) {
				int oldValue = calendar.get(expr.field);
				if (oldValue != value) {
					// The value has changed, so update the calendar and reset all
					// less significant fields
					calendar.set(expr.field, value);
					resetFields(calendar, expr.field, false);
					
					// If the weekday changed, the day of month changed too
					if (expr.field == Calendar.DAY_OF_WEEK) {
						fieldIterator.previous();
						fieldIterator.previous();
					}
				}
			} else if (fieldIterator.previousIndex() >= 1) {
				// No suitable value was found, so move back to the previous field
				// and increase the value
				fieldIterator.previous();
				expr = fieldIterator.previous();
				calendar.add(expr.field, 1);
			} else {
				return null;
			}
		}
		return calendar.before(stopCalendar) ? calendar.getTime() : null;
	}

	@Override
	public Date getNextFireTime() {
		return nextFireTime;
	}

	@Override
	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public boolean mayFireAgain() {
		return getNextFireTime() != null;
	}

	private void resetFields(Calendar calendar, int currentField, boolean max) {
		final int[] fields = { Calendar.YEAR, Calendar.MONTH,
				Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
				Calendar.SECOND };
		for (int field : fields) {
			if (field > currentField) {
				int value = max ? calendar.getActualMaximum(field) : calendar.getActualMinimum(field);
				calendar.set(field, value);
			}
		}

	}
	
	@Override
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Override
	public void triggered(org.quartz.Calendar calendar) {
		// Copied from org.quartz.CronTrigger
		previousFireTime = nextFireTime;
        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }
	}

	@Override
	public void updateAfterMisfire(org.quartz.Calendar cal) {
    	// TODO verify misfire policy
        if (isVolatile()) {
            Date newFireTime = getFireTimeAfter(new Date());
            while (newFireTime != null && cal != null
                    && !cal.isTimeIncluded(newFireTime.getTime())) {
                newFireTime = getFireTimeAfter(newFireTime);
            }
            nextFireTime = newFireTime;
        } else {
            nextFireTime = new Date();
        }
	}

	@Override
	public void updateWithNewCalendar(org.quartz.Calendar cal, long misfireThreshold) {
        if (cal == null) {
        	return;
        }
        
        Date now = new Date();
        nextFireTime = getFireTimeAfter(previousFireTime);
        while (nextFireTime != null && !cal.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
            if (nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if (diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                }
            }
        }
	}

	@Override
	protected boolean validateMisfireInstruction(int misfireInstruction) {
		return misfireInstruction == MISFIRE_INSTRUCTION_SMART_POLICY;
	}
	
	public static class ParseException extends Exception {

		private final Map<Integer, ParseException> children;
		private final Integer field;
		private final String value;
		private final String error;

		protected ParseException(int field, String value, String message) {
			this.children = null;
			this.field = field;
			this.value = value;
			this.error = message;
		}
		
		protected ParseException(Map<Integer, ParseException> children) {
			this.children = children;
			this.field = null;
			this.value = null;
			this.error = null;
		}

		public Map<Integer, ParseException> getChildren() {
			return children != null ? Collections.unmodifiableMap(children) : null;
		}

		public Integer getField() {
			return field;
		}

		public String getValue() {
			return value;
		}

		public String getError() {
			return error;
		}
		
	}
	
	private abstract static class FieldExpression {

		private static final Map<String, Integer> MONTHS_MAP = new HashMap<String, Integer>();
		private static final Map<String, Integer> WEEKDAYS_MAP = new HashMap<String, Integer>();
		protected static final Calendar CALENDAR = new GregorianCalendar(Locale.US); // For getting min/max field values
		
		static {
			int i = 1;
			for (String month : new DateFormatSymbols(Locale.US).getShortMonths()) {
				MONTHS_MAP.put(month.toUpperCase(Locale.US), i++);
			}
			i = 0;
			for (String weekday : new DateFormatSymbols(Locale.US).getShortWeekdays()) {
				WEEKDAYS_MAP.put(weekday.toUpperCase(Locale.US), i++);
			}
		}
		
		protected static int convertValue(String value, int field) throws ParseException {
			// If the value begins with a digit, parse it as a number
			if (Character.isDigit(value.charAt(0))) {
				int numValue;
				try {
					numValue = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new ParseException(field, value, "Unparseable value");
				}
				
				if (field == Calendar.DAY_OF_WEEK) {
					numValue++;
					if (numValue == 8) {
						numValue = 1;
					}
				} else if (field == Calendar.MONTH) {
					numValue--; // Months are 0-based
				}
				
				// Validate the value
				if (numValue < CALENDAR.getMinimum(field) || numValue > CALENDAR.getMaximum(field)) {
					throw new ParseException(field, value, "Value is out of range");
				}
				return numValue;
			}

			// Try converting a textual value to numeric
			switch (field) {
			case Calendar.MONTH:
				return MONTHS_MAP.get(value);
			case Calendar.DAY_OF_WEEK:
				return WEEKDAYS_MAP.get(value);
			}

			throw new ParseException(field, value, "Unparseable value");
		}
		
		public final int field;
		
		protected FieldExpression(int field) {
			this.field = field;
		}

		protected int convertValue(String value) throws ParseException {
			return convertValue(value, field);
		}
		
		/**
		 * Returns the next allowed value in this calendar for the given
		 * field.
		 * 
		 * @param calendar
		 *            a Calendar where all the more significant fields have
		 *            been filled out
		 * @return the next value allowed by this expression, or
		 *         <code>null</code> if none further allowed values are
		 *         found
		 */
		public abstract Integer getNextValue(Calendar calendar);
		
		/**
		 * Returns the last allowed value in this calendar for the given field.
		 * 
		 * @param calendar
		 *            a Calendar where all the more significant fields have
		 *            been filled out
		 * @return the last value allowed by this expression, or
		 *         <code>null</code> if none further allowed values are
		 *         found
		 */
		public abstract Integer getPreviousValue(Calendar calendar);

	}
	
	private static class RangeExpression extends FieldExpression {

		private final int start;
		private final int end;

		public RangeExpression(Matcher m, int field) throws ParseException {
			super(field);
			start = convertValue(m.group(1));
			end = convertValue(m.group(2));
			if (end < start) {
				throw new ParseException(field, m.group(), "End value must be higher than start value");
			}
		}

		@Override
		public Integer getNextValue(Calendar calendar) {
			int currValue = calendar.get(field);
			int maxValue = calendar.getActualMaximum(field);
			return currValue <= end && currValue <= maxValue ?
					Math.min(start, maxValue) : null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			int maxValue = calendar.getActualMaximum(field);
			return end <= maxValue ? end : null;
		}

	}
	
	private static class ListExpression extends FieldExpression {

		private final List<Integer> values = new ArrayList<Integer>();
		
		public ListExpression(Matcher m, int field) throws ParseException {
			super(field);
			for (String num : m.group().split(",")) {
				values.add(convertValue(num));
			}
			Collections.sort(values);
		}

		@Override
		public Integer getNextValue(Calendar calendar) {
			int currValue = calendar.get(field);
			for (Integer day : values) {
				if (day >= currValue) {
					return day;
				}
			}
			return null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			int currValue = calendar.get(field);
			ListIterator<Integer> iterator = values.listIterator(values.size());
			while (iterator.hasPrevious()) {
				int day = iterator.previous();
				if (day <= currValue) {
					return day;
				}
			}
			return null;
		}
		
	}
	
	private static class IncrementExpression extends FieldExpression {
		
		private final int start;
		private final int interval;
		
		public IncrementExpression(Matcher m, int field) {
			super(field);
			int minValue = CALENDAR.getMinimum(field);
			start = m.group(1).equals("*") ? minValue : Integer.parseInt(m.group(1));
			interval = Integer.parseInt(m.group(2));
		}

		@Override
		public Integer getNextValue(Calendar calendar) {
			// Only applicable for seconds, minutes and hours so the actual maximum does not vary
			// so currValue is always valid
			int currValue = Math.max(calendar.get(field), start);
			int maxValue = calendar.getMaximum(field);
			if (currValue % interval > 0) {
				int nextValue = currValue + (interval - currValue % interval);
				return nextValue <= maxValue ? nextValue : null;
			}
			return currValue;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			// Only applicable for seconds, minutes and hours so the actual maximum does not vary
			// so currValue is always valid
			int maxValue = calendar.getMaximum(field);
			int currValue = Math.min(calendar.get(field), maxValue);
			int nextValue = currValue - currValue % interval;
			return nextValue >= start ? nextValue : null;
		}

	}
	
	private static class WeekdayExpression extends FieldExpression {

		private final Integer ordinal; // null means last
		private final int weekday;
		
		public WeekdayExpression(Matcher m) throws ParseException {
			super(Calendar.DAY_OF_MONTH);
			Character firstChar = m.group(1).charAt(0);
			ordinal = Character.isDigit(firstChar) ? new Integer(firstChar) : null;
			weekday = convertValue(m.group(2), Calendar.DAY_OF_WEEK);
		}
		
		@Override
		public Integer getNextValue(Calendar calendar) {
			int currDay = calendar.get(Calendar.DAY_OF_MONTH);
			Integer nthDay = getPreviousValue(calendar);
			return nthDay != null && nthDay >= currDay ? nthDay : null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			int currDay = calendar.get(Calendar.DAY_OF_MONTH);
			int currWeekday = calendar.get(Calendar.DAY_OF_WEEK);
			int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			// Calculate the first day in the month whose weekday is the same as the
			// one we're looking for
			int firstWeekday = (currDay % 7) - (currWeekday - weekday);
			// Then calculate how many such weekdays there is in this month
			int numWeekdays = (maxDay - firstWeekday) / 7;

			// Then calculate the Nth of those days, or the last one if ordinal is null
			int multiplier = ordinal != null ? ordinal : numWeekdays;
			int nthDay = firstWeekday + multiplier * 7;
			
			// Return the calculated day, or null if the day is out of range
			return nthDay <= maxDay ? nthDay : null;
		}
		
	}
	
	private static class DaysFromLastDayExpression extends FieldExpression {

		private final int days;
		
		public DaysFromLastDayExpression(Matcher m) {
			super(Calendar.DAY_OF_MONTH);
			days = new Integer(m.group(1));
		}

		public DaysFromLastDayExpression() {
			super(Calendar.DAY_OF_MONTH);
			this.days = 0;
		}

		@Override
		public Integer getNextValue(Calendar calendar) {
			int currValue = calendar.get(field);
			int maxValue = calendar.getActualMaximum(field);
			int value = maxValue - days;
			return currValue <= value ? value : null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			int maxValue = calendar.getActualMaximum(field);
			return maxValue - days;
		}
		
	}
	
}
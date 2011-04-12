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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.ScheduleExpression;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public class EJBCronTrigger extends Trigger {

    private static final Pattern INCREMENTS = Pattern.compile("(\\d+|\\*)/(\\d+)*");

	private static final Pattern LIST = Pattern.compile("(([A-Za-z0-9]+)(-[A-Za-z0-9]+)?)?((1ST|2ND|3RD|4TH|5TH|LAST)([A-za-z]+))?(-([0-9]+))?(LAST)?" +
			"(?:,(([A-Za-z0-9]+)(-[A-Za-z0-9]+)?)?((1ST|2ND|3RD|4TH|5TH|LAST)([A-za-z]+))?(-([0-9]+))?(LAST)?)*");
	
	private static final Pattern RANGE = Pattern.compile("([A-Za-z0-9]+)-([A-Za-z0-9]+)");
	private static final Pattern WEEKDAY = Pattern.compile("(1ST|2ND|3RD|4TH|5TH|LAST)([A-za-z]+)");
	private static final Pattern DAYS_TO_LAST = Pattern.compile("-([0-9]+)");

	private static final String LAST_IDENTIFIER = "LAST";

    private static final int[] ORDERED_CALENDAR_FIELDS = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };

    private static final Map<Integer, Integer> CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP = new LinkedHashMap<Integer, Integer>();

    static {
        //Initialize a calendar field -> ordered array index map
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.YEAR, 0);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.MONTH, 1);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.DAY_OF_MONTH, 2);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.DAY_OF_WEEK, 3);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.HOUR_OF_DAY, 4);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.MINUTE, 5);
        CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.put(Calendar.SECOND, 6);
    }

	private final FieldExpression[] expressions = new FieldExpression[7];

	// Optional settings
	private Date startTime;
	private Date endTime;

	// Internal variables
	private Date nextFireTime;
	private Date previousFireTime;

	private TimeZone timezone;

	public EJBCronTrigger(ScheduleExpression expr) throws ParseException {
		Map<Integer, String> fieldValues = new LinkedHashMap<Integer, String>();
		fieldValues.put(Calendar.YEAR, expr.getYear());
		fieldValues.put(Calendar.MONTH, expr.getMonth());
        fieldValues.put(Calendar.DAY_OF_MONTH, expr.getDayOfMonth());
		fieldValues.put(Calendar.DAY_OF_WEEK, expr.getDayOfWeek());
		fieldValues.put(Calendar.HOUR_OF_DAY, expr.getHour());
		fieldValues.put(Calendar.MINUTE, expr.getMinute());
		fieldValues.put(Calendar.SECOND, expr.getSecond());

		timezone = expr.getTimezone() == null ? TimeZone.getDefault() : TimeZone.getTimeZone(expr.getTimezone());
        startTime = expr.getStart() == null ? new Date() : expr.getStart();
		endTime = expr.getEnd();

		// If parsing fails on a field, record the error and move to the next field
		Map<Integer, ParseException> errors = new HashMap<Integer, ParseException>();
		int index = 0;
		for (Entry<Integer, String> entry : fieldValues.entrySet()) {
			int field = entry.getKey();
			String value = entry.getValue();
			try {
				expressions[index++] = parseExpression(field, value);
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
		    return new AsteriskExpression(field);
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
			if (expr.equals(LAST_IDENTIFIER)) {
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
        System.out.println("computeFirstFireTime [" + calendar + "] nextFireTime [" + nextFireTime + "]");
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
		Calendar calendar = new GregorianCalendar(timezone);
		calendar.setLenient(false);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		if (endTime == null) {
			// If the year field has been left default, there is no end time
            if (expressions[0] instanceof AsteriskExpression) {
                return null;
            }
			resetFields(calendar, 0, true);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar.setTime(endTime);
		}

		// Calculate time to give up scheduling
		Calendar stopCalendar = new  GregorianCalendar(timezone);
		if (startTime != null) {
			stopCalendar.setTime(startTime);
		} else {
			stopCalendar.setTimeInMillis(0);
		}

		int currentFieldIndex = 0;
        while (currentFieldIndex <= 6 && calendar.after(stopCalendar)) {
            FieldExpression expr = expressions[currentFieldIndex];
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
                        currentFieldIndex--;
                    } else {
                        currentFieldIndex++;
                    }
                } else {
                    currentFieldIndex++;
                }
            } else if (currentFieldIndex >= 1) {
                // No suitable value was found, so move back to the previous field
                // and decrease the value
                int maxAffectedFieldType = upadteCalendar(calendar, expressions[currentFieldIndex - 1].field, -1);
                currentFieldIndex = CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.get(maxAffectedFieldType);
                resetFields(calendar, maxAffectedFieldType, true);
            } else {
                return null; // The job will never be run
            }
		}
		return calendar.after(stopCalendar) ? calendar.getTime() : null;
	}

	@Override
	public Date getFireTimeAfter(Date afterTime) {
		Calendar calendar = new GregorianCalendar(timezone);
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
		Calendar stopCalendar = new GregorianCalendar(timezone);
		if (endTime != null) {
			stopCalendar.setTime(endTime);
		} else {
			int stopYear = calendar.get(Calendar.YEAR) + 100;
			stopCalendar.set(Calendar.YEAR, stopYear);
		}

		int currentFieldIndex = 0;
		while (currentFieldIndex <=6 && calendar.before(stopCalendar)) {
			FieldExpression expr = expressions[currentFieldIndex];
			Integer value = expr.getNextValue(calendar);
            if (value != null) {
                int oldValue = calendar.get(expr.field);
                if (oldValue != value) {
                    // The value has changed, so update the calendar and reset all
                    // less significant fields
                    calendar.set(expr.field, value);
                    resetFields(calendar, expr.field, false);

                    // If the weekday changed, the day of month changed too
                    if (currentFieldIndex == 3) {
                        currentFieldIndex--;
                    } else {
                        currentFieldIndex++;
                    }
                } else {
                    currentFieldIndex++;
                }
            } else if (currentFieldIndex >= 1) {
                // No suitable value was found, so move back to the previous field
                // and increase the value
                // When current field is HOUR_OF_DAY, its upper field is DAY_OF_MONTH, so we need to -2 due to DAY_OF_WEEK.
                int parentFieldIndex = currentFieldIndex ==4 ? currentFieldIndex- 2 : currentFieldIndex - 1;
                int maxAffectedFieldType = upadteCalendar(calendar, expressions[parentFieldIndex].field, 1);
                currentFieldIndex = CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.get(maxAffectedFieldType);
                resetFields(calendar, maxAffectedFieldType, false);
            } else {
                return null;
            }
        }
		return calendar.before(stopCalendar) ? calendar.getTime() : null;
	}

	/**
	 * Update the value of target field by one, and return the max affected field value
	 * @param calendar
	 * @param field
	 * @return
	 */
    private int upadteCalendar(Calendar calendar, int field, int amount) {
        Calendar old = new GregorianCalendar(timezone);
        old.setTime(calendar.getTime());
        calendar.add(field, amount);
        for (int fieldType : ORDERED_CALENDAR_FIELDS) {
            if (calendar.get(fieldType) != old.get(fieldType)) {
                return fieldType;
            }
        }
        //Should never get here
        return -1;
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

    /**
     * reset those sub field values, we need to configure from the end to begin, as getActualMaximun consider other fields' values
     * @param calendar
     * @param currentField
     * @param max
     */
	private void resetFields(Calendar calendar, int currentField, boolean max) {
        for (int index = ORDERED_CALENDAR_FIELDS.length - 1; index >= 0; index--) {
            int calendarField = ORDERED_CALENDAR_FIELDS[index];
            if (calendarField > currentField) {
                int value = max ? calendar.getActualMaximum(calendarField) : calendar.getActualMinimum(calendarField);
                calendar.set(calendarField, value);
            } else {
                break;
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
        nextFireTime = getFireTimeAfter(nextFireTime);
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
			//Jan -> 1
			for (String month : new DateFormatSymbols(Locale.US).getShortMonths()) {
				MONTHS_MAP.put(month.toUpperCase(Locale.US), i++);
			}
			i = 0;
			//SUN -> 1
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
		private int start2;

        public RangeExpression(int field, int start, int end, int start2) {
            super(field);
            this.start = start;
            this.end = end;
            this.start2 = start2;
        }

		public RangeExpression(Matcher m, int field) throws ParseException {
			super(field);
			int beginValue = convertValue(m.group(1));
            if (m.group(2).equals(LAST_IDENTIFIER)) {
                start = -1;
                end = -1;
                start2= beginValue;
            } else {
                int endValue = convertValue(m.group(2));
                if (beginValue > endValue) {
                    start = CALENDAR.getMinimum(field);
                    end = endValue;
                    start2 = beginValue;
                } else {
                    start = beginValue;
                    end = endValue;
                    start2 = -1;
                }
            }
		}

        @Override
        public Integer getNextValue(Calendar calendar) {
            int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return currValue;
                } else if (currValue > end) {
                    return start2;
                }
            }
            if (currValue <= start) {
                return start;
            } else if (currValue <= end) {
                return currValue;
            } else {
                return null;
            }
        }

		@Override
		public Integer getPreviousValue(Calendar calendar) {
		    int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return currValue;
                }
            }
            if (currValue <= start) {
                return null;
            } else if (currValue <= end) {
                return currValue;
            } else {
                return end;
            }
		}
	}

	/*
	 * Just find that it is hard to keep those ranges in the list are not overlapped.
	 * The easy way is to list all the values, also we keep a range expression if user defines a LAST expression, e.g. 12-LAST
	 */
	private static class ListExpression extends FieldExpression {

		private List<Integer> values;

		private RangeExpression rangeExpression;
		
		private List<WeekdayExpression> weekDayExpressions = new ArrayList<WeekdayExpression>();
		
		private List<DaysFromLastDayExpression> daysFromLastDayExpressions = new ArrayList<DaysFromLastDayExpression>();;

		public ListExpression(Matcher m, int field) throws ParseException {
			super(field);
			initialize(m);
		}

        private void initialize(Matcher m) throws ParseException {
            Set<Integer> individualValues = new HashSet<Integer>();
            for (String value : m.group().split("[,]")) {
                Matcher rangeMatcher = RANGE.matcher(value);
                Matcher weekDayMatcher = WEEKDAY.matcher(value);
                Matcher daysToLastMatcher = DAYS_TO_LAST.matcher(value);
                if (value.equals(LAST_IDENTIFIER)) {
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression());
                }else if(daysToLastMatcher.matches()){
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression(daysToLastMatcher));
                } else if (weekDayMatcher.matches()){
                    weekDayExpressions.add(new WeekdayExpression(weekDayMatcher));
                    continue;
                } else if (rangeMatcher.matches()) {
                    int rangeBeginIndex = -1;
                    int beginValue = convertValue(rangeMatcher.group(1));
                    if (rangeMatcher.group(2).equals(LAST_IDENTIFIER)) {
                        rangeBeginIndex = beginValue;
                    } else {
                        int endValue = convertValue(rangeMatcher.group(2));
                        if (beginValue <= endValue) {
                            for (int i = beginValue; i <= endValue; i++) {
                                individualValues.add(i);
                            }
                        } else {
                            rangeBeginIndex = beginValue;
                            for (int i = CALENDAR.getMinimum(field); i <= endValue; i++) {
                                individualValues.add(i);
                            }
                        }
                    }
                    if(rangeBeginIndex != -1) {
                        if(rangeExpression == null) {
                            rangeExpression = new RangeExpression(field, -1,-1, rangeBeginIndex);
                        } else {
                            if(rangeBeginIndex < rangeExpression.start2) {
                                for(int i = rangeExpression.start2; i< rangeBeginIndex; i++) {
                                    individualValues.add(i);
                                }
                                rangeExpression.start2 = rangeBeginIndex;
                            }
                        }
                    }
                } else {
                    int individualValue = convertValue(value);
                    if(rangeExpression == null ||  individualValue < rangeExpression.start2) {
                        individualValues.add(individualValue);
                    }
                }
            }
            //Add individualValues in to values list;
            //Double check whether those individual values are included in the range
            values = new ArrayList<Integer>(individualValues.size());
            if (rangeExpression != null) {
                for (Integer individualValue : individualValues) {
                    if (individualValue < rangeExpression.start2) {
                        values.add(individualValue);
                    }
                }
            } else {
                values.addAll(individualValues);
            }
            Collections.sort(values);
        }
        
        private List<Integer> getNewValuesFromDynamicExpressions(Calendar calendar){
            
            List<Integer> newValues = new ArrayList<Integer>(values.size() + weekDayExpressions.size());
            
            if (rangeExpression != null) {
                for (Integer value : values) {
                    if (value < rangeExpression.start2) {
                        newValues.add(value);
                    }
                }
                
                for (WeekdayExpression weekdayExpression : weekDayExpressions) {
                    Integer value=weekdayExpression.getNextValue(calendar);
                    if (value != null && value < rangeExpression.start2) {
                        newValues.add(value);
                    }
                }
                
                for (DaysFromLastDayExpression daysFromLastDayExpression : daysFromLastDayExpressions) {
                    Integer value=daysFromLastDayExpression.getNextValue(calendar);
                    if (value != null && value < rangeExpression.start2) {
                        newValues.add(value);
                    }
                }


            } else {
                newValues.addAll(values);
                for (WeekdayExpression weekdayExpression : weekDayExpressions) {
                    Integer value=weekdayExpression.getNextValue(calendar);
                    if (value != null) {
                        newValues.add(value);
                    }
                }
                
                for (DaysFromLastDayExpression daysFromLastDayExpression : daysFromLastDayExpressions) {
                    Integer value=daysFromLastDayExpression.getNextValue(calendar);
                    if (value != null) {
                        newValues.add(value);
                    }
                }
                
            }

            if (newValues.size() > 0) {
                Collections.sort(newValues);
            }

            return newValues;
            
        }

		@Override
		public Integer getNextValue(Calendar calendar) {
		    
		    List<Integer> newValues= getNewValuesFromDynamicExpressions(calendar);
		    
			int currValue = calendar.get(field);
			for (Integer day : newValues) {
				if (day >= currValue) {
					return day;
				}
			}
			if(rangeExpression != null) {
			    return rangeExpression.getNextValue(calendar);
			}
			return null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
		    if(rangeExpression != null) {
		        Integer previousValue = rangeExpression.getPreviousValue(calendar);
		        if(previousValue != null) {
		            return previousValue;
		        }
		    }
		    
		    List<Integer> newValues= getNewValuesFromDynamicExpressions(calendar);
		    
			int currValue = calendar.get(field);
			ListIterator<Integer> iterator = newValues.listIterator(newValues.size());
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
			ordinal = Character.isDigit(firstChar) ? Integer.valueOf(firstChar.toString()) : null;
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
			int nthDay = firstWeekday>0?(firstWeekday + (multiplier-1) * 7):(firstWeekday + multiplier * 7);

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

	private static class AsteriskExpression extends FieldExpression {

        public AsteriskExpression(int field){
            super(field);
        }

        @Override
        public Integer getNextValue(Calendar calendar) {
           return calendar.get(field);
        }

        @Override
        public Integer getPreviousValue(Calendar calendar) {
            return calendar.get(field);
        }
    }
}
/*
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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.quartz.impl.triggers.CronTriggerImpl;

import javax.ejb.ScheduleExpression;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EJBCronTrigger extends CronTriggerImpl {
    
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, EJBCronTrigger.class);

    private static final Pattern INCREMENTS = Pattern.compile("(\\d+|\\*)/(\\d+)*");

	private static final Pattern LIST = Pattern.compile("(([A-Za-z0-9]+)(-[A-Za-z0-9]+)?)?((1ST|2ND|3RD|4TH|5TH|LAST)([A-za-z]+))?(-([0-7]+))?(LAST)?" +
			"(?:,(([A-Za-z0-9]+)(-[A-Za-z0-9]+)?)?((1ST|2ND|3RD|4TH|5TH|LAST)([A-za-z]+))?(-([0-7]+))?(LAST)?)*");
	
	private static final Pattern WEEKDAY = Pattern.compile("(1ST|2ND|3RD|4TH|5TH|LAST)(SUN|MON|TUE|WED|THU|FRI|SAT)");
	private static final Pattern DAYS_TO_LAST = Pattern.compile("-([0-7]+)");
	
	private static final Pattern VALID_YEAR = Pattern.compile("([0-9][0-9][0-9][0-9])|\\*");
	private static final Pattern VALID_MONTH = Pattern.compile("(([0]?[1-9])|(1[0-2]))|\\*");
	private static final Pattern VALID_DAYS_OF_WEEK = Pattern.compile("[0-7]|\\*");
	private static final Pattern VALID_DAYS_OF_MONTH = Pattern.compile("((1ST|2ND|3RD|4TH|5TH|LAST)(SUN|MON|TUE|WED|THU|FRI|SAT))|(([1-9])|(0[1-9])|([12])([0-9]?)|(3[01]?))|(LAST)|-([0-7])|[*]");
	private static final Pattern VALID_HOUR = Pattern.compile("(([0-1]?[0-9])|([2][0-3]))|\\*");
	private static final Pattern VALID_MINUTE = Pattern.compile("([0-5]?[0-9])|\\*");
	private static final Pattern VALID_SECOND = Pattern.compile("([0-5]?[0-9])|\\*");
	
    private static final Pattern RANGE = Pattern.compile("(-?[A-Za-z0-9]+)-(-?[A-Za-z0-9]+)");


	private static final String LAST_IDENTIFIER = "LAST";
    
    private static final Map<String, Integer> MONTHS_MAP = new HashMap<String, Integer>();
    private static final Map<String, Integer> WEEKDAYS_MAP = new HashMap<String, Integer>();

    static {
        int i = 0;
        // Jan -> 0
        for (String month : new DateFormatSymbols(Locale.US).getShortMonths()) {
            MONTHS_MAP.put(month.toUpperCase(Locale.US), i++);
        }
        i = 0;
        // SUN -> 1
        for (String weekday : new DateFormatSymbols(Locale.US).getShortWeekdays()) {
            WEEKDAYS_MAP.put(weekday.toUpperCase(Locale.US), i++);
        }
    }
	
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
        setStartTime(expr.getStart() == null ? new Date() : expr.getStart());
		setEndTime(expr.getEnd());

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
	    
	    if (expr == null || expr.isEmpty()){
	        throw new ParseException(field, expr, "expression can't be null");
	    }
	    
		// Get rid of whitespace and convert to uppercase
		expr = expr.replaceAll("\\s+", "").toUpperCase();
		
		
        if (expr.length() > 1 && expr.indexOf(",") > 0) {

            String[] expressions = expr.split(",");

            for (String sub_expression : expressions) {
                validateExpression(field, sub_expression);
            }

        } else {

            validateExpression(field, expr);

        }
		

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
        
	
	private void validateExpression(int field, String expression) throws ParseException {
	    
	    Matcher rangeMatcher= RANGE.matcher(expression);
        Matcher incrementsMatcher= INCREMENTS.matcher(expression);
        
        if (expression.length() > 2 && rangeMatcher.matches()) {
            
                validateSingleToken(field, rangeMatcher.group(1));
                validateSingleToken(field, rangeMatcher.group(2));
            
        } else if (expression.length() > 2 && incrementsMatcher.matches()) {
            
            validateSingleToken(field, incrementsMatcher.group(1));
            validateSingleToken(field, incrementsMatcher.group(2));
            
        } else {
            
            validateSingleToken(field, expression);
            
        }
	    
	}
        
    private void validateSingleToken(int field, String token) throws ParseException{
        
        if(token==null || token.isEmpty()) {
          throw new ParseException(field, token, "expression can't be null");
        }
        
        switch (field) {
        
        case Calendar.YEAR:
            Matcher m = VALID_YEAR.matcher(token);
            if (!m.matches()) {
                throw new ParseException(field, token, "Valid YEAR is four digit");
            }
            break;               
            
        case Calendar.MONTH:
            m = VALID_MONTH.matcher(token);
            if (!(m.matches() || MONTHS_MAP.containsKey(token))) {
                throw new ParseException(field, token, "Valid MONTH is 1-12 or {'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', Dec'}");
            }
            break;
            
        case Calendar.DAY_OF_MONTH:
            m = VALID_DAYS_OF_MONTH.matcher(token);
            if (!m.matches()) {
                throw new ParseException(field, token, "Valid DAYS_OF_MONTH is 0-7 or {'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'} ");
            }
            break;
            
        case Calendar.DAY_OF_WEEK:
            m = VALID_DAYS_OF_WEEK.matcher(token);
            if (!(m.matches() || WEEKDAYS_MAP.containsKey(token))) {
                throw new ParseException(field, token, "Valid DAYS_OF_WEEK is 1-31  -(1-7) or {'1st', '2nd', '3rd', '4th',  '5th', 'Last'} + {'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'} ");
            }
            break;
        
        case Calendar.HOUR_OF_DAY:
            m = VALID_HOUR.matcher(token);
            if (!m.matches()) {
                throw new ParseException(field, token, "Valid HOUR_OF_DAY value is 0-23");
            }
            break;
        case Calendar.MINUTE:
            m = VALID_MINUTE.matcher(token);
            if (!m.matches()) {
                throw new ParseException(field, token, "Valid MINUTE value is 0-59");
            }
            break;
        case Calendar.SECOND:
            m = VALID_SECOND.matcher(token);
            if (!m.matches()) {
                throw new ParseException(field, token, "Valid SECOND value is 0-59");
            }
            break;
            
        }
    
    }
	/**
	 * Works similarly to getFireTimeAfter() but backwards.
	 */
	@Override
	public Date getFinalFireTime() {
		Calendar calendar = new GregorianCalendar(timezone);
        //calendar.setLenient(false);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		if (getEndTime() == null) {
			// If the year field has been left default, there is no end time
            if (expressions[0] instanceof AsteriskExpression) {
                return null;
            }
			resetFields(calendar, 0, true);
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar.setTime(getEndTime());
		}

		// Calculate time to give up scheduling
		Calendar stopCalendar = new  GregorianCalendar(timezone);
		if (getStartTime() != null) {
			stopCalendar.setTime(getStartTime());
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
	    log.debug("start to getFireTimeAfter:"+afterTime);
		Calendar calendar = new GregorianCalendar(timezone);
        // calendar.setLenient(false);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);

		// Calculate starting time
		if (getStartTime() != null && getStartTime().after(afterTime)) {
			calendar.setTime(getStartTime());
		} else {
			calendar.setTime(afterTime);
			calendar.add(Calendar.SECOND, 1);
		}

		// Calculate time to give up scheduling
		Calendar stopCalendar = new GregorianCalendar(timezone);
		if (getEndTime() != null) {
			stopCalendar.setTime(getEndTime());
		} else {
			int stopYear = calendar.get(Calendar.YEAR) + 100;
			stopCalendar.set(Calendar.YEAR, stopYear);
		}

		int currentFieldIndex = 0;
		
        while (currentFieldIndex <= 6 && calendar.before(stopCalendar)) {

           
            FieldExpression expr = expressions[currentFieldIndex];
            Integer value = expr.getNextValue(calendar);

            /*
             * 18.2.1.2 Expression Rules
             * If dayOfMonth has a non-wildcard value and dayOfWeek has a non-wildcard value, then either the
             * dayOfMonth field or the dayOfWeek field must match the current day (even though the other of the
             * two fields need not match the current day).
             */
            if (currentFieldIndex == 2 && !(expressions[3] instanceof AsteriskExpression)) {
                Calendar clonedCalendarDayOfWeek = (Calendar) calendar.clone();
                Integer nextDayOfWeek = expressions[3].getNextValue(clonedCalendarDayOfWeek);
                while (nextDayOfWeek == null) {
                    clonedCalendarDayOfWeek.add(Calendar.DAY_OF_MONTH, 1);
                    nextDayOfWeek = expressions[3].getNextValue(clonedCalendarDayOfWeek);
                }

                if (nextDayOfWeek != null) {
                    clonedCalendarDayOfWeek.set(expressions[3].field, nextDayOfWeek);
                    int newDayOfMonth = clonedCalendarDayOfWeek.get(expressions[2].field);
                    
                    if (value == null) {
                        value = newDayOfMonth;
                    } else if (clonedCalendarDayOfWeek.get(expressions[1].field)==calendar.get(expressions[1].field)){
                        value = Math.min(value, newDayOfMonth);
                    } 
                    
                    //Next valid DayOfWeek might exist in next month.
                    if(expressions[1].getNextValue(clonedCalendarDayOfWeek)==null){
                        return null;
                    } else if (value != calendar.get(expressions[2].field)
                            && clonedCalendarDayOfWeek.get(expressions[1].field) > calendar.get(expressions[1].field)) {
                        calendar.set(Calendar.MONTH, clonedCalendarDayOfWeek.get(Calendar.MONTH));
                    }
                }
            }

            if (currentFieldIndex >= 1 && value == null) {

                if (currentFieldIndex == 3 && !(expressions[2] instanceof AsteriskExpression)) {
                    /*
                     *18.2.1.2 Expression Rules, the day has been resolved when dayOfMonth expression
                     *is not AsteriskExpression.
                     */
                    currentFieldIndex++;
                } else {
                    // No suitable value was found, so move back to the previous field
                    // and increase the value
                    // When current field is HOUR_OF_DAY, its upper field is DAY_OF_MONTH, so we need to -2 due to
                    // DAY_OF_WEEK.
                    int parentFieldIndex = currentFieldIndex == 4 ? currentFieldIndex - 2 : currentFieldIndex - 1;
                    int maxAffectedFieldType = upadteCalendar(calendar, expressions[parentFieldIndex].field, 1);
                    currentFieldIndex = CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.get(maxAffectedFieldType);
                    resetFields(calendar, maxAffectedFieldType, false);
                }
                
            } else if (value != null) {
                
                int oldValue = calendar.get(expr.field);
                if (oldValue != value) {
                    
                    if (currentFieldIndex == 3 && !(expressions[2] instanceof AsteriskExpression)) {
                        /*
                         *18.2.1.2 Expression Rules, the day has been resolved when dayOfMonth expression
                         *is not AsteriskExpression.
                         */
                        currentFieldIndex++;
                    } else {
                        // The value has changed, so update the calendar and reset all
                        // less significant fields
                        calendar.set(expr.field, value);
                        resetFields(calendar, expr.field, false);
                        currentFieldIndex++;
                    }
                } else {
                    currentFieldIndex++;
                }
            } else {
                log.debug("end of getFireTimeAfter, result is:" + null);
                return null;
            }
        }
		
		log.debug("end of getFireTimeAfter, result is:"+ (calendar.before(stopCalendar) ? calendar.getTime() : null));
		
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
		
        @Override
        public String toString() {
            return "ParseException [field=" + field + ", value=" + value + ", error=" + error + "]";
        }		

	}

	private abstract static class FieldExpression {


		protected static final Calendar CALENDAR = new GregorianCalendar(Locale.US); // For getting min/max field values

       

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
				} else if (field == Calendar.MONTH) {
					numValue--; // Months are 0-based
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
		
		protected boolean isValidResult(Calendar calendar, Integer result){
		    
		    if (result!=null && result>=calendar.getActualMinimum(field) && result <=calendar.getActualMaximum(field)){
                return true;
            } 
		    
		    return false;
		    
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

        private int start;
        private int end;
        private int start2 = -1;

        private String startWeekDay;
        private String endWeekDay;
        
        
        private WeekdayExpression startWeekdayExpr = null;
        private WeekdayExpression endWeekdayExpr = null;
        
        private DaysFromLastDayExpression startDaysFromLastDayExpr = null;
        private DaysFromLastDayExpression endDaysFromLastDayExpr = null;
        
        
        
        //Indicate if the range expression is for "1st mon - 2nd fri" style range of days of month.
        private boolean isDynamicRangeExpression = false;


        public boolean isDynamicRangeExpression() {
            return isDynamicRangeExpression;
        }

        public RangeExpression(int field, int start, int end, int start2) {
            super(field);
            this.start = start;
            this.end = end;
            this.start2 = start2;
        }

		public RangeExpression(Matcher m, int field) throws ParseException {
            
			super(field);
            
            startWeekDay = m.group(1);
            endWeekDay = m.group(2);
            
            
            if (field == Calendar.DAY_OF_MONTH) {
                
                Matcher startWeekDayMatcher = WEEKDAY.matcher(m.group(1));
                Matcher endWeekDayMatcher = WEEKDAY.matcher(m.group(2));
                
                Matcher startDaysFromLastDayMatcher = DAYS_TO_LAST.matcher(m.group(1));
                Matcher endDaysFromLastDayMatcher = DAYS_TO_LAST.matcher(m.group(2));
                
                if (startWeekDayMatcher.matches()) {
                    startWeekdayExpr = new WeekdayExpression(startWeekDayMatcher);

                } 
                
                if (endWeekDayMatcher.matches()) {
                    endWeekdayExpr = new WeekdayExpression(endWeekDayMatcher);
                } 
                
                if (startDaysFromLastDayMatcher.matches()) {
                    startDaysFromLastDayExpr = new DaysFromLastDayExpression(startDaysFromLastDayMatcher);
                } 
                
                if (endDaysFromLastDayMatcher.matches()) {
                    endDaysFromLastDayExpr = new DaysFromLastDayExpression(endDaysFromLastDayMatcher);
                } 
                

                if (startWeekdayExpr != null || endWeekdayExpr != null || startDaysFromLastDayExpr != null
                        || endDaysFromLastDayExpr != null || startWeekDay.equals(LAST_IDENTIFIER)|| endWeekDay.equals(LAST_IDENTIFIER)) {
                    
                    isDynamicRangeExpression = true;
                    return;
                }
                
            }
            
            //not a dynamic range expression, go ahead to init start and end values without a calendar
            initStartEndValues(null);
           
            
         }
        
        private void initStartEndValues(Calendar calendar) throws ParseException{
            
            int beginValue;
            int endValue;
            
            if(isDynamicRangeExpression){
                
                if (startWeekDay.equals(LAST_IDENTIFIER)) {
                    beginValue = calendar.getActualMaximum(field);
                } else if (startWeekdayExpr != null) {
                    beginValue = startWeekdayExpr.getWeekdayInMonth(calendar);
                } else if (startDaysFromLastDayExpr != null) {
                    Integer next = startDaysFromLastDayExpr.getNextValue(calendar);
                    beginValue = next == null ? calendar.get(field) : next;
                } else {
                    beginValue = convertValue(startWeekDay);
                }

                if (endWeekDay.equals(LAST_IDENTIFIER)) {
                    endValue = calendar.getActualMaximum(field);
                } else if (endWeekdayExpr != null) {
                    endValue = endWeekdayExpr.getWeekdayInMonth(calendar);
                } else if (endDaysFromLastDayExpr != null) {
                    Integer next = endDaysFromLastDayExpr.getNextValue(calendar);
                    endValue = next == null ? calendar.get(field) : next;
                } else {
                    endValue = convertValue(endWeekDay);
                }
                
            } else {
                beginValue=convertValue(startWeekDay);
                endValue=convertValue(endWeekDay);
            }
            
            
            /*
             * handle 0-7 for day of week range.
             * 
             * both 0 and 7 represent Sun.  We need to remove one from the range.
             * 
             */
            if (field == Calendar.DAY_OF_WEEK) {
                
                if ((beginValue == 8 && endValue == 1)||(endValue == 8 && beginValue == 1)) {
                    beginValue = 1;
                    endValue = 7;
                } else {
                    
                    
                    if (beginValue == 8) {
                        beginValue = 1;
                    }

                    if (endValue == 8) {
                        endValue = 1;
                    }           
                }
            }
            
            
            
            
            // Try converting a textual value to numeric
            if (endWeekDay.equals(LAST_IDENTIFIER)) {
                start = -1;
                end = -1;
                start2= beginValue;
            } else {
                if (beginValue > endValue) {
                    start = CALENDAR.getMinimum(field);
                    end = endValue;
                    start2 = beginValue;
                } else {
                    start = beginValue;
                    end = endValue;
                }
            }
		}


        @Override
        public Integer getNextValue(Calendar calendar) {
            
            if (isDynamicRangeExpression){
                
                Integer nextStartWeekday = startWeekdayExpr == null ? start : startWeekdayExpr
                        .getWeekdayInMonth(calendar);
                
                Integer nextendWeekday = endWeekdayExpr == null ? end : endWeekdayExpr.
                        getWeekdayInMonth(calendar);
                
                if (nextStartWeekday == null || nextendWeekday == null) {
                    return null;
                }
                
                try {
                    initStartEndValues(calendar);
                } catch (ParseException e) {
                    return null;
                }
            }
            
            
            int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return isValidResult(calendar,currValue)?currValue:null;
                } else if (currValue > end) {
                    return isValidResult(calendar,start2)?start2:null;
                }
            }
            if (currValue <= start) {
                return isValidResult(calendar,start)?start:null;
            } else if (currValue <= end) {
                return isValidResult(calendar,currValue)?currValue:null;
            } else {
                return null;
            }
        }

		@Override
		public Integer getPreviousValue(Calendar calendar) {
            
            if (isDynamicRangeExpression){
                try {
                    initStartEndValues(calendar);
                } catch (ParseException e) {
                    return null;
                }
             }
            
            
		    int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return isValidResult(calendar,currValue)?currValue:null;
                }
            }
            if (currValue <= start) {
                return null;
            } else if (currValue <= end) {
                return isValidResult(calendar,currValue)?currValue:null;
            } else {
                return isValidResult(calendar,end)?end:null;
            }
		}

       public List<Integer> getAllValuesInRange(Calendar calendar){
           
           List<Integer> values=new ArrayList<Integer>();
           
           if (isDynamicRangeExpression){
               try {
                   initStartEndValues(calendar);
               } catch (ParseException e) {
                  return values;
               }
            }
           
            if (start2==-1) {
                for (int i = start; i <= end; i++) {
                    values.add(i);
                }
            } else {

                for (int i = start; i <= end; i++) {
                    values.add(i);
                }
                for (int i = start2; i <= CALENDAR.getMaximum(field); i++) {
                    values.add(i);
                }
            }
            
            return values;
       }
        
    }
	/*
	 * Just find that it is hard to keep those ranges in the list are not overlapped.
	 * The easy way is to list all the values, also we keep a range expression if user defines a LAST expression, e.g. 12-LAST
	 */
	private static class ListExpression extends FieldExpression {

        private final Set<Integer> values = new TreeSet<Integer>();

        private final List<RangeExpression> weekDayRangeExpressions = new ArrayList<RangeExpression>();
		
        private final List<WeekdayExpression> weekDayExpressions = new ArrayList<WeekdayExpression>();
		
        private final List<DaysFromLastDayExpression> daysFromLastDayExpressions = new ArrayList<DaysFromLastDayExpression>();;

		public ListExpression(Matcher m, int field) throws ParseException {
			super(field);
			initialize(m);
		}

        private void initialize(Matcher m) throws ParseException {
            
            for (String value : m.group().split("[,]")) {
                
                Matcher rangeMatcher = RANGE.matcher(value);
                Matcher weekDayMatcher = WEEKDAY.matcher(value);
                Matcher daysToLastMatcher = DAYS_TO_LAST.matcher(value);
                
                if (value.equals(LAST_IDENTIFIER)) {
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression());
                    continue;
                } else if(daysToLastMatcher.matches()){
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression(daysToLastMatcher));
                    continue;
                } else if (weekDayMatcher.matches()){
                    weekDayExpressions.add(new WeekdayExpression(weekDayMatcher));
                    continue;
                } else if (rangeMatcher.matches()) {
                    
                    RangeExpression rangeExpression= new RangeExpression(rangeMatcher,field);
                    
                    if (rangeExpression.isDynamicRangeExpression()){
                        weekDayRangeExpressions.add(new RangeExpression(rangeMatcher,field));
                        continue;
                    }
                    
                    values.addAll(rangeExpression.getAllValuesInRange(null));
                    
                } else {
                    int individualValue = convertValue(value);
                    
                    if(field == Calendar.DAY_OF_WEEK && individualValue == 8){
                        individualValue = 1;  
                    }
                    
                    values.add(individualValue);
                }
            }
            
        }
        
        private TreeSet<Integer> getNewValuesFromDynamicExpressions(Calendar calendar){
            
            TreeSet<Integer> newValues = new TreeSet<Integer>();

                newValues.addAll(values);
                
                for (RangeExpression weekDayRangeExpression : weekDayRangeExpressions) {
                    
                        newValues.addAll(weekDayRangeExpression.getAllValuesInRange(calendar));
                }
                
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
                

            return newValues;
            
        }

		@Override
		public Integer getNextValue(Calendar calendar) {
		    
		    TreeSet<Integer> newValues= getNewValuesFromDynamicExpressions(calendar);
		    
			int currValue = calendar.get(field);
			
			Integer result = newValues.ceiling(currValue);
			
			return isValidResult(calendar, result)? result : null;
			
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
		    
		    TreeSet<Integer> newValues= getNewValuesFromDynamicExpressions(calendar);
		    
			int currValue = calendar.get(field);
			
			Integer result =newValues.floor(currValue);
            
			return isValidResult(calendar, result)? result : null;
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
		    
            int currValue = calendar.get(field);

            if (currValue > start) {

                Integer nextValue = start + interval;

                while (isValidResult(calendar, nextValue)) {

                    if (nextValue >= currValue)
                        return nextValue;

                    nextValue = nextValue + interval;

                }

            } else {
                return new Integer(start);
            }

            return null;
		
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
		    
            int currValue = calendar.get(field);

            if (currValue < start) {

                Integer previousValue = start - interval;

                while (isValidResult(calendar, previousValue)) {

                    if (previousValue < currValue)
                        return previousValue;

                    previousValue = previousValue - interval;

                }

            } else {
                return new Integer(start);
            }

            return null;
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
            Integer nthDay = getWeekdayInMonth(calendar);
			Integer result = nthDay != null && nthDay >= currDay ? nthDay : null;
			
			return isValidResult(calendar, result)? result : null;
		}
        
        public Integer getWeekdayInMonth(Calendar calendar){
            
			int currDay = calendar.get(Calendar.DAY_OF_MONTH);
			int currWeekday = calendar.get(Calendar.DAY_OF_WEEK);
			int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

			// Calculate the first day in the month whose weekday is the same as the
			// one we're looking for
			int firstWeekday = (currDay % 7) - (currWeekday - weekday);
			
            firstWeekday = (firstWeekday == 0) ? 7 : firstWeekday;
			
			// Then calculate how many such weekdays there is in this month
			int numWeekdays = firstWeekday>=0?(maxDay - firstWeekday) / 7 +1:(maxDay - firstWeekday) / 7;

			// Then calculate the Nth of those days, or the last one if ordinal is null
			int multiplier = ordinal != null ? ordinal : numWeekdays;
			int nthDay = firstWeekday>=0?(firstWeekday + (multiplier-1) * 7):(firstWeekday + multiplier * 7);

			// Return the calculated day, or null if the day is out of range
			return nthDay <= maxDay ? nthDay : null;
        }

        @Override
        public Integer getPreviousValue(Calendar calendar) {
            
              int currDay = calendar.get(Calendar.DAY_OF_MONTH);
              Integer nthDay = getWeekdayInMonth(calendar);
              Integer result = nthDay != null && nthDay <= currDay ? nthDay : null;
              
              return isValidResult(calendar, result)? result : null;
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
			Integer result = currValue <= value ? value : null;
			return isValidResult(calendar, result)? result : null;
		}

		@Override
		public Integer getPreviousValue(Calendar calendar) {
			int maxValue = calendar.getActualMaximum(field);
			Integer result = maxValue - days;
			return isValidResult(calendar, result)? result : null;
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
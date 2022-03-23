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
import org.apache.openejb.quartz.impl.triggers.CronTriggerImpl;

import jakarta.ejb.ScheduleExpression;
import java.io.Serializable;
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
    private static final long serialVersionUID = 1L;

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

    public static final String DELIMITER = ";";

    private static final String LAST_IDENTIFIER = "LAST";

    private static final Map<String, Integer> WEEKDAYS_MAP = new HashMap<String, Integer>();

    private static final Map<String, Integer> MONTHS_MAP = new HashMap<String, Integer>();

    static {
        int i = 0;
        // Jan -> 0
        for (final String month : new DateFormatSymbols(Locale.US).getShortMonths()) {
            MONTHS_MAP.put(month.toUpperCase(Locale.US), i++);
        }
        i = 0;
        // SUN -> 1
        for (final String weekday : new DateFormatSymbols(Locale.US).getShortWeekdays()) {
            WEEKDAYS_MAP.put(weekday.toUpperCase(Locale.US), i++);
        }
    }

    private static final int[] ORDERED_CALENDAR_FIELDS = {Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND};

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

    private final TimeZone timezone;
    private final String rawValue;
    private final boolean isExpired;

    public EJBCronTrigger(final ScheduleExpression expr) throws ParseException {

        final Map<Integer, String> fieldValues = new LinkedHashMap<>();
        fieldValues.put(Calendar.YEAR, expr.getYear());
        fieldValues.put(Calendar.MONTH, expr.getMonth());
        fieldValues.put(Calendar.DAY_OF_MONTH, expr.getDayOfMonth());
        fieldValues.put(Calendar.DAY_OF_WEEK, expr.getDayOfWeek());
        fieldValues.put(Calendar.HOUR_OF_DAY, expr.getHour());
        fieldValues.put(Calendar.MINUTE, expr.getMinute());
        fieldValues.put(Calendar.SECOND, expr.getSecond());

        timezone = expr.getTimezone() == null ? TimeZone.getDefault() : TimeZone.getTimeZone(expr.getTimezone());
        setStartTime(expr.getStart() == null ? new Date() : expr.getStart());

        /*
         * @testName: endNeverExpires
         *
         * @test_Strategy: create a timer with year="currentYear - currentYear+1", and
         * end="currentYear-1". The end value is prior to the year values, and this
         * timer will never expire. Creating this timer will succeed, but any timer
         * method access in a subsequent business method will result in
         * NoSuchObjectLocalException.
         *
         * EJB32 TCK test tries to create an already expired Timer and it's supposed to not fail.
         * This may happen whe you restart an application for instance.
         * On the other hand, Quartz does not allow endTime to be before StartTime so we need to check first so we don't
         * set the endDate but we flag up this timer as being expired.
         *
         * When the first time is computed we will fail and as a consequence TimerData will be flagged up as being expired.
         * So if endDate is not set or endTime after startTime, then we can consider this timer as not expired.
         * If endTime is set and it's before startTime, we swallow setEndTime to Quartz and set the expired flag to true
         */
        if (expr.getEnd() == null || !isBefore(expr.getEnd(), getStartTime())) {
            setEndTime(expr.getEnd());
            isExpired = false;

        } else {
            isExpired = true;
        }

        // If parsing fails on a field, record the error and move to the next field
        final Map<Integer, ParseException> errors = new HashMap<>();
        int index = 0;
        for (final Entry<Integer, String> entry : fieldValues.entrySet()) {
            final int field = entry.getKey();
            final String value = entry.getValue();
            try {
                expressions[index++] = parseExpression(field, value);
            } catch (final ParseException e) {
                errors.put(field, e);
            }
        }

        // If there were parsing errors, throw a "master exception" that contains all
        // exceptions from individual fields
        if (!errors.isEmpty()) {
            throw new ParseException(errors);
        }

        rawValue = expr.getYear() + DELIMITER + expr.getMonth() + DELIMITER + expr.getDayOfMonth() + DELIMITER + expr.getDayOfWeek()
            + DELIMITER + expr.getHour() + DELIMITER + expr.getMinute() + DELIMITER + expr.getSecond();
    }

    private boolean isBefore(final Date end, final Date start) {
        return start != null && end != null && start.after(end);
    }

    /**
     * Computes a set of allowed values for the given field of a calendar based
     * time expression.
     *
     * @param field field type from <code>java.util.Calendar</code>
     * @param expr  a time expression
     * @throws ParseException when there is a syntax error in the expression, or its values
     *                        are out of range
     */
    protected FieldExpression parseExpression(final int field, String expr) throws ParseException {

        if (expr == null || expr.isEmpty()) {
            throw new ParseException(field, expr, "expression can't be null");
        }

        // Get rid of whitespace and convert to uppercase
        expr = expr.replaceAll("\\s+", "").toUpperCase(Locale.ENGLISH);


        if (expr.length() > 1 && expr.indexOf(',') > 0) {

            final String[] expressions = expr.split(",");

            for (final String subExpression : expressions) {
                validateExpression(field, subExpression);
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


    private void validateExpression(final int field, final String expression) throws ParseException {

        final Matcher rangeMatcher = RANGE.matcher(expression);
        final Matcher incrementsMatcher = INCREMENTS.matcher(expression);

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

    private void validateSingleToken(final int field, final String token) throws ParseException {
        if (token == null || token.isEmpty()) {
            throw new ParseException(field, token, "expression can't be null");
        }
        switch (field) {
            case Calendar.YEAR: {
                final Matcher m = VALID_YEAR.matcher(token);
                if (!m.matches()) {
                    throw new ParseException(field, token, "Valid YEAR is four digit");
                }
                break;
            }
            case Calendar.MONTH: {
                final Matcher m = VALID_MONTH.matcher(token);
                if (!(m.matches() || MONTHS_MAP.containsKey(token))) {
                    throw new ParseException(field, token, "Valid MONTH is 1-12 or {'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', Dec'}");
                }
                break;
            }
            case Calendar.DAY_OF_MONTH: {
                final Matcher m = VALID_DAYS_OF_MONTH.matcher(token);
                if (!m.matches()) {
                    throw new ParseException(field, token, "Valid DAYS_OF_MONTH is 0-7 or {'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'} ");
                }
                break;
            }
            case Calendar.DAY_OF_WEEK: {
                final Matcher m = VALID_DAYS_OF_WEEK.matcher(token);
                if (!(m.matches() || WEEKDAYS_MAP.containsKey(token))) {
                    throw new ParseException(field, token, "Valid DAYS_OF_WEEK is 1-31  -(1-7) or {'1st', '2nd', '3rd', '4th',  '5th', 'Last'} + {'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'} ");
                }
                break;
            }
            case Calendar.HOUR_OF_DAY: {
                final Matcher m = VALID_HOUR.matcher(token);
                if (!m.matches()) {
                    throw new ParseException(field, token, "Valid HOUR_OF_DAY value is 0-23");
                }
                break;
            }
            case Calendar.MINUTE: {
                final Matcher m = VALID_MINUTE.matcher(token);
                if (!m.matches()) {
                    throw new ParseException(field, token, "Valid MINUTE value is 0-59");
                }
                break;
            }
            case Calendar.SECOND: {
                final Matcher m = VALID_SECOND.matcher(token);
                if (!m.matches()) {
                    throw new ParseException(field, token, "Valid SECOND value is 0-59");
                }
                break;
            }
        }
    }

    /**
     * Works similarly to getFireTimeAfter() but backwards.
     */
    @Override
    public Date getFinalFireTime() {
        final Calendar calendar = new GregorianCalendar(timezone);
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
        final Calendar stopCalendar = new GregorianCalendar(timezone);
        if (getStartTime() != null) {
            stopCalendar.setTime(getStartTime());
        } else {
            stopCalendar.setTimeInMillis(0);
        }

        int currentFieldIndex = 0;
        while (currentFieldIndex <= 6 && calendar.after(stopCalendar)) {
            final FieldExpression expr = expressions[currentFieldIndex];
            final Integer value = expr.getPreviousValue(calendar);
            if (value != null) {
                final int oldValue = calendar.get(expr.field);
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
                final int maxAffectedFieldType = updateCalendar(calendar, expressions[currentFieldIndex - 1].field, -1);
                currentFieldIndex = CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.get(maxAffectedFieldType);
                resetFields(calendar, maxAffectedFieldType, true);
            } else {
                return null; // The job will never be run
            }
        }

        return calendar.after(stopCalendar) ? calendar.getTime() : null;


    }

    @Override
    public Date computeFirstFireTime(final org.apache.openejb.quartz.Calendar calendar) {
        // timer may be expired up on creation (see constructor comments)
        if (isExpired) {
            throw new TimerExpiredException(String.format("Timer %s expired.", this));
        }

        return super.computeFirstFireTime(calendar);
    }

    @Override
    public Date getFireTimeAfter(final Date afterTime) {
        log.debug("start to getFireTimeAfter:" + afterTime);
        final Calendar calendar = new GregorianCalendar(timezone);
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
        final Calendar stopCalendar = new GregorianCalendar(timezone);
        if (getEndTime() != null) {
            stopCalendar.setTime(getEndTime());
        } else {
            final int stopYear = calendar.get(Calendar.YEAR) + 100;
            stopCalendar.set(Calendar.YEAR, stopYear);
        }

        int currentFieldIndex = 0;

        while (currentFieldIndex <= 6 && calendar.before(stopCalendar)) {


            final FieldExpression expr = expressions[currentFieldIndex];
            Integer value = expr.getNextValue(calendar);

            /*
             * 18.2.1.2 Expression Rules
             * If dayOfMonth has a non-wildcard value and dayOfWeek has a non-wildcard value, then either the
             * dayOfMonth field or the dayOfWeek field must match the current day (even though the other of the
             * two fields need not match the current day).
             */
            if (currentFieldIndex == 2 && !(expressions[3] instanceof AsteriskExpression)) {
                final Calendar clonedCalendarDayOfWeek = (Calendar) calendar.clone();
                Integer nextDayOfWeek = expressions[3].getNextValue(clonedCalendarDayOfWeek);
                while (nextDayOfWeek == null) {
                    clonedCalendarDayOfWeek.add(Calendar.DAY_OF_MONTH, 1);
                    nextDayOfWeek = expressions[3].getNextValue(clonedCalendarDayOfWeek);
                }

                if (nextDayOfWeek != null) {
                    clonedCalendarDayOfWeek.set(expressions[3].field, nextDayOfWeek);
                    final int newDayOfMonth = clonedCalendarDayOfWeek.get(expressions[2].field);

                    if (value == null) {
                        value = newDayOfMonth;
                    } else if (clonedCalendarDayOfWeek.get(expressions[1].field) == calendar.get(expressions[1].field)) {
                        value = Math.min(value, newDayOfMonth);
                    }

                    //Next valid DayOfWeek might exist in next month.
                    if (expressions[1].getNextValue(clonedCalendarDayOfWeek) == null) {
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
                    final int parentFieldIndex = currentFieldIndex == 4 ? currentFieldIndex - 2 : currentFieldIndex - 1;
                    final int maxAffectedFieldType = updateCalendar(calendar, expressions[parentFieldIndex].field, 1);
                    currentFieldIndex = CALENDAR_FIELD_TYPE_ORDERED_INDEX_MAP.get(maxAffectedFieldType);
                    resetFields(calendar, maxAffectedFieldType, false);
                }

            } else if (value != null) {

                final int oldValue = calendar.get(expr.field);
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

        log.debug("end of getFireTimeAfter, result is:" + (calendar.before(stopCalendar) ? calendar.getTime() : null));

        return calendar.before(stopCalendar) ? calendar.getTime() : null;
    }

    /**
     * Update the value of target field by one, and return the max affected field value
     *
     * @param calendar
     * @param field
     * @return
     */
    private int updateCalendar(final Calendar calendar, final int field, final int amount) {
        final Calendar old = new GregorianCalendar(timezone);
        old.setTime(calendar.getTime());
        calendar.add(field, amount);
        for (final int fieldType : ORDERED_CALENDAR_FIELDS) {
            if (calendar.get(fieldType) != old.get(fieldType)) {
                return fieldType;
            }
        }
        //Should never get here
        return -1;
    }

    public String getRawValue() {
        return rawValue;
    }

    /**
     * reset those sub field values, we need to configure from the end to begin, as getActualMaximun consider other fields' values
     *
     * @param calendar
     * @param currentField
     * @param max
     */
    private void resetFields(final Calendar calendar, final int currentField, final boolean max) {
        for (int index = ORDERED_CALENDAR_FIELDS.length - 1; index >= 0; index--) {
            final int calendarField = ORDERED_CALENDAR_FIELDS[index];
            if (calendarField > currentField) {
                final int value = max ? calendar.getActualMaximum(calendarField) : calendar.getActualMinimum(calendarField);
                calendar.set(calendarField, value);
            } else {
                break;
            }
        }
    }

    @Override // we don't want to be a CronTrigger for persistence
    public boolean hasAdditionalProperties() {
        return true;
    }

    public static class ParseException extends Exception {

        private final Map<Integer, ParseException> children;
        private final Integer field;
        private final String value;
        private final String error;

        protected ParseException(final int field, final String value, final String message) {
            this.children = null;
            this.field = field;
            this.value = value;
            this.error = message;
        }

        protected ParseException(final Map<Integer, ParseException> children) {
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

    private abstract static class FieldExpression implements Serializable {


        protected static final Calendar CALENDAR = new GregorianCalendar(Locale.US); // For getting min/max field values


        protected static int convertValue(final String value, final int field) throws ParseException {
            // If the value begins with a digit, parse it as a number
            if (Character.isDigit(value.charAt(0))) {
                int numValue;
                try {
                    numValue = Integer.parseInt(value);
                } catch (final NumberFormatException e) {
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

        protected FieldExpression(final int field) {
            this.field = field;
        }

        protected int convertValue(final String value) throws ParseException {
            return convertValue(value, field);
        }

        protected boolean isValidResult(final Calendar calendar, final Integer result) {
            return result != null && result >= calendar.getActualMinimum(field) && result <= calendar.getActualMaximum(field);
        }

        /**
         * Returns the next allowed value in this calendar for the given
         * field.
         *
         * @param calendar a Calendar where all the more significant fields have
         *                 been filled out
         * @return the next value allowed by this expression, or
         * <code>null</code> if none further allowed values are
         * found
         */
        public abstract Integer getNextValue(Calendar calendar);

        /**
         * Returns the last allowed value in this calendar for the given field.
         *
         * @param calendar a Calendar where all the more significant fields have
         *                 been filled out
         * @return the last value allowed by this expression, or
         * <code>null</code> if none further allowed values are
         * found
         */
        public abstract Integer getPreviousValue(Calendar calendar);

    }

    private static class RangeExpression extends FieldExpression {

        private int start;
        private int end;
        private int start2 = -1;

        private String startWeekDay;
        private String endWeekDay;


        private WeekdayExpression startWeekdayExpr;
        private WeekdayExpression endWeekdayExpr;

        private DaysFromLastDayExpression startDaysFromLastDayExpr;
        private DaysFromLastDayExpression endDaysFromLastDayExpr;


        //Indicate if the range expression is for "1st mon - 2nd fri" style range of days of month.
        private boolean isDynamicRangeExpression;


        public boolean isDynamicRangeExpression() {
            return isDynamicRangeExpression;
        }

        public RangeExpression(final int field, final int start, final int end, final int start2) {
            super(field);
            this.start = start;
            this.end = end;
            this.start2 = start2;
        }

        public RangeExpression(final Matcher m, final int field) throws ParseException {

            super(field);

            startWeekDay = m.group(1);
            endWeekDay = m.group(2);


            if (field == Calendar.DAY_OF_MONTH) {

                final Matcher startWeekDayMatcher = WEEKDAY.matcher(m.group(1));
                final Matcher endWeekDayMatcher = WEEKDAY.matcher(m.group(2));

                final Matcher startDaysFromLastDayMatcher = DAYS_TO_LAST.matcher(m.group(1));
                final Matcher endDaysFromLastDayMatcher = DAYS_TO_LAST.matcher(m.group(2));

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
                    || endDaysFromLastDayExpr != null || startWeekDay.equals(LAST_IDENTIFIER) || endWeekDay.equals(LAST_IDENTIFIER)) {

                    isDynamicRangeExpression = true;
                    return;
                }

            }

            //not a dynamic range expression, go ahead to init start and end values without a calendar
            initStartEndValues(null);


        }

        private void initStartEndValues(final Calendar calendar) throws ParseException {

            int beginValue;
            int endValue;

            if (isDynamicRangeExpression) {

                if (startWeekDay.equals(LAST_IDENTIFIER)) {
                    beginValue = calendar.getActualMaximum(field);
                } else if (startWeekdayExpr != null) {
                    beginValue = startWeekdayExpr.getWeekdayInMonth(calendar);
                } else if (startDaysFromLastDayExpr != null) {
                    final Integer next = startDaysFromLastDayExpr.getNextValue(calendar);
                    beginValue = next == null ? calendar.get(field) : next;
                } else {
                    beginValue = convertValue(startWeekDay);
                }

                if (endWeekDay.equals(LAST_IDENTIFIER)) {
                    endValue = calendar.getActualMaximum(field);
                } else if (endWeekdayExpr != null) {
                    endValue = endWeekdayExpr.getWeekdayInMonth(calendar);
                } else if (endDaysFromLastDayExpr != null) {
                    final Integer next = endDaysFromLastDayExpr.getNextValue(calendar);
                    endValue = next == null ? calendar.get(field) : next;
                } else {
                    endValue = convertValue(endWeekDay);
                }

            } else {
                beginValue = convertValue(startWeekDay);
                endValue = convertValue(endWeekDay);
            }
            
            
            /*
             * handle 0-7 for day of week range.
             * 
             * both 0 and 7 represent Sun.  We need to remove one from the range.
             * 
             */
            if (field == Calendar.DAY_OF_WEEK) {

                if (beginValue == 8 && endValue == 1 || endValue == 8 && beginValue == 1) {
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
                start2 = beginValue;
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
        public Integer getNextValue(final Calendar calendar) {

            if (isDynamicRangeExpression) {

                final Integer nextStartWeekday = startWeekdayExpr == null ? start : startWeekdayExpr
                    .getWeekdayInMonth(calendar);

                final Integer nextendWeekday = endWeekdayExpr == null ? end : endWeekdayExpr.
                    getWeekdayInMonth(calendar);

                if (nextStartWeekday == null || nextendWeekday == null) {
                    return null;
                }

                try {
                    initStartEndValues(calendar);
                } catch (final ParseException e) {
                    return null;
                }
            }


            final int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return isValidResult(calendar, currValue) ? currValue : null;
                } else if (currValue > end) {
                    return isValidResult(calendar, start2) ? start2 : null;
                }
            }
            if (currValue <= start) {
                return isValidResult(calendar, start) ? start : null;
            } else if (currValue <= end) {
                return isValidResult(calendar, currValue) ? currValue : null;
            } else {
                return null;
            }
        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {

            if (isDynamicRangeExpression) {
                try {
                    initStartEndValues(calendar);
                } catch (final ParseException e) {
                    return null;
                }
            }


            final int currValue = calendar.get(field);
            if (start2 != -1) {
                if (currValue >= start2) {
                    return isValidResult(calendar, currValue) ? currValue : null;
                }
            }
            if (currValue <= start) {
                return null;
            } else if (currValue <= end) {
                return isValidResult(calendar, currValue) ? currValue : null;
            } else {
                return isValidResult(calendar, end) ? end : null;
            }
        }

        public List<Integer> getAllValuesInRange(final Calendar calendar) {

            final List<Integer> values = new ArrayList<>();

            if (isDynamicRangeExpression) {
                try {
                    initStartEndValues(calendar);
                } catch (final ParseException e) {
                    return values;
                }
            }

            if (start2 == -1) {
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

        private final Set<Integer> values = new TreeSet<>();

        private final List<RangeExpression> weekDayRangeExpressions = new ArrayList<>();

        private final List<WeekdayExpression> weekDayExpressions = new ArrayList<>();

        private final List<DaysFromLastDayExpression> daysFromLastDayExpressions = new ArrayList<>();
        ;

        public ListExpression(final Matcher m, final int field) throws ParseException {
            super(field);
            initialize(m);
        }

        private void initialize(final Matcher m) throws ParseException {

            for (final String value : m.group().split("[,]")) {

                final Matcher rangeMatcher = RANGE.matcher(value);
                final Matcher weekDayMatcher = WEEKDAY.matcher(value);
                final Matcher daysToLastMatcher = DAYS_TO_LAST.matcher(value);

                if (value.equals(LAST_IDENTIFIER)) {
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression());
                    continue;
                } else if (daysToLastMatcher.matches()) {
                    daysFromLastDayExpressions.add(new DaysFromLastDayExpression(daysToLastMatcher));
                    continue;
                } else if (weekDayMatcher.matches()) {
                    weekDayExpressions.add(new WeekdayExpression(weekDayMatcher));
                    continue;
                } else if (rangeMatcher.matches()) {

                    final RangeExpression rangeExpression = new RangeExpression(rangeMatcher, field);

                    if (rangeExpression.isDynamicRangeExpression()) {
                        weekDayRangeExpressions.add(new RangeExpression(rangeMatcher, field));
                        continue;
                    }

                    values.addAll(rangeExpression.getAllValuesInRange(null));

                } else {
                    int individualValue = convertValue(value);

                    if (field == Calendar.DAY_OF_WEEK && individualValue == 8) {
                        individualValue = 1;
                    }

                    values.add(individualValue);
                }
            }

        }

        private TreeSet<Integer> getNewValuesFromDynamicExpressions(final Calendar calendar) {

            final TreeSet<Integer> newValues = new TreeSet<>(values);

            for (final RangeExpression weekDayRangeExpression : weekDayRangeExpressions) {

                newValues.addAll(weekDayRangeExpression.getAllValuesInRange(calendar));
            }

            for (final WeekdayExpression weekdayExpression : weekDayExpressions) {
                final Integer value = weekdayExpression.getNextValue(calendar);
                if (value != null) {
                    newValues.add(value);
                }
            }

            for (final DaysFromLastDayExpression daysFromLastDayExpression : daysFromLastDayExpressions) {
                final Integer value = daysFromLastDayExpression.getNextValue(calendar);
                if (value != null) {
                    newValues.add(value);
                }
            }


            return newValues;

        }

        @Override
        public Integer getNextValue(final Calendar calendar) {

            final TreeSet<Integer> newValues = getNewValuesFromDynamicExpressions(calendar);

            final int currValue = calendar.get(field);

            final Integer result = newValues.ceiling(currValue);

            return isValidResult(calendar, result) ? result : null;

        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {

            final TreeSet<Integer> newValues = getNewValuesFromDynamicExpressions(calendar);

            final int currValue = calendar.get(field);

            final Integer result = newValues.floor(currValue);

            return isValidResult(calendar, result) ? result : null;
        }
    }

    private static class IncrementExpression extends FieldExpression {

        private final int start;
        private final int interval;

        public IncrementExpression(final Matcher m, final int field) {
            super(field);
            final int minValue = CALENDAR.getMinimum(field);
            start = m.group(1).equals("*") ? minValue : Integer.parseInt(m.group(1));
            interval = Integer.parseInt(m.group(2));
        }

        @Override
        public Integer getNextValue(final Calendar calendar) {

            final int currValue = calendar.get(field);

            if (currValue > start) {

                Integer nextValue = start + interval;

                while (isValidResult(calendar, nextValue)) {

                    if (nextValue >= currValue) {
                        return nextValue;
                    }

                    nextValue = nextValue + interval;

                }

            } else {
                return start;
            }

            return null;

        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {

            final int currValue = calendar.get(field);

            if (currValue < start) {

                Integer previousValue = start - interval;

                while (isValidResult(calendar, previousValue)) {

                    if (previousValue < currValue) {
                        return previousValue;
                    }

                    previousValue = previousValue - interval;

                }

            } else {
                return start;
            }

            return null;
        }

    }

    private static class WeekdayExpression extends FieldExpression {
        private final Integer ordinal; // null means last
        private final int weekday;

        public WeekdayExpression(final Matcher m) throws ParseException {
            super(Calendar.DAY_OF_MONTH);
            final Character firstChar = m.group(1).charAt(0);
            ordinal = Character.isDigit(firstChar) ? Integer.valueOf(firstChar.toString()) : null;
            weekday = convertValue(m.group(2), Calendar.DAY_OF_WEEK);
        }

        @Override
        public Integer getNextValue(final Calendar calendar) {
            final int currDay = calendar.get(Calendar.DAY_OF_MONTH);
            final Integer nthDay = getWeekdayInMonth(calendar);
            final Integer result = nthDay != null && nthDay >= currDay ? nthDay : null;

            return isValidResult(calendar, result) ? result : null;
        }

        public Integer getWeekdayInMonth(final Calendar calendar) {

            final int currDay = calendar.get(Calendar.DAY_OF_MONTH);
            final int currWeekday = calendar.get(Calendar.DAY_OF_WEEK);
            final int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Calculate the first day in the month whose weekday is the same as the
            // one we're looking for
            int firstWeekday = currDay % 7 - (currWeekday - weekday);

            firstWeekday = firstWeekday == 0 ? 7 : firstWeekday;

            // Then calculate how many such weekdays there is in this month
            final int numWeekdays = firstWeekday >= 0 ? (maxDay - firstWeekday) / 7 + 1 : (maxDay - firstWeekday) / 7;

            // Then calculate the Nth of those days, or the last one if ordinal is null
            final int multiplier = ordinal != null ? ordinal : numWeekdays;
            final int nthDay = firstWeekday >= 0 ? firstWeekday + (multiplier - 1) * 7 : firstWeekday + multiplier * 7;

            // Return the calculated day, or null if the day is out of range
            return nthDay <= maxDay ? nthDay : null;
        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {

            final int currDay = calendar.get(Calendar.DAY_OF_MONTH);
            final Integer nthDay = getWeekdayInMonth(calendar);
            final Integer result = nthDay != null && nthDay <= currDay ? nthDay : null;

            return isValidResult(calendar, result) ? result : null;
        }

    }

    private static class DaysFromLastDayExpression extends FieldExpression {

        private final int days;

        public DaysFromLastDayExpression(final Matcher m) {
            super(Calendar.DAY_OF_MONTH);
            days = new Integer(m.group(1));
        }

        public DaysFromLastDayExpression() {
            super(Calendar.DAY_OF_MONTH);
            this.days = 0;
        }

        @Override
        public Integer getNextValue(final Calendar calendar) {
            final int currValue = calendar.get(field);
            final int maxValue = calendar.getActualMaximum(field);
            final int value = maxValue - days;
            final Integer result = currValue <= value ? value : null;
            return isValidResult(calendar, result) ? result : null;
        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {
            final int maxValue = calendar.getActualMaximum(field);
            final Integer result = maxValue - days;
            return isValidResult(calendar, result) ? result : null;
        }

    }

    private static class AsteriskExpression extends FieldExpression {

        public AsteriskExpression(final int field) {
            super(field);
        }

        @Override
        public Integer getNextValue(final Calendar calendar) {
            return calendar.get(field);
        }

        @Override
        public Integer getPreviousValue(final Calendar calendar) {
            return calendar.get(field);
        }
    }
}

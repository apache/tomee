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

package org.apache.openejb.math.stat.descriptive.moment;

import org.apache.openejb.math.MathRuntimeException;
import org.apache.openejb.math.stat.descriptive.AbstractStorelessUnivariateStatistic;

import java.io.Serializable;


/**
 * Computes the Kurtosis of the available values.
 * <p>
 * We use the following (unbiased) formula to define kurtosis:</p>
 * <p>
 * kurtosis = { [n(n+1) / (n -1)(n - 2)(n-3)] sum[(x_i - mean)^4] / std^4 } - [3(n-1)^2 / (n-2)(n-3)]
 * </p><p>
 * where n is the number of values, mean is the {@link Mean} and std is the
 * {@link org.apache.commons.math3.stat.descriptive.moment.StandardDeviation}</p>
 * <p>
 * Note that this statistic is undefined for n < 4.  <code>Double.Nan</code>
 * is returned when there is not sufficient data to compute the statistic.</p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an instance of this class concurrently, and at least
 * one of the threads invokes the <code>increment()</code> or
 * <code>clear()</code> method, it must be synchronized externally.</p>
 *
 * @version $Revision: 811833 $ $Date: 2009-09-06 09:27:50 -0700 (Sun, 06 Sep 2009) $
 */
public class Kurtosis extends AbstractStorelessUnivariateStatistic implements Serializable {

    /**
     * Serializable version identifier
     */
    private static final long serialVersionUID = 1234465764798260919L;

    /**
     * Fourth Moment on which this statistic is based
     */
    protected FourthMoment moment;

    /**
     * Determines whether or not this statistic can be incremented or cleared.
     * <p>
     * Statistics based on (constructed from) external moments cannot
     * be incremented or cleared.</p>
     */
    protected boolean incMoment;

    /**
     * Construct a Kurtosis
     */
    public Kurtosis() {
        incMoment = true;
        moment = new FourthMoment();
    }

    /**
     * Construct a Kurtosis from an external moment
     *
     * @param m4 external Moment
     */
    public Kurtosis(final FourthMoment m4) {
        incMoment = false;
        this.moment = m4;
    }

    /**
     * Copy constructor, creates a new {@code Kurtosis} identical
     * to the {@code original}
     *
     * @param original the {@code Kurtosis} instance to copy
     */
    public Kurtosis(final Kurtosis original) {
        copy(original, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(final double d) {
        if (incMoment) {
            moment.increment(d);
        } else {
            throw MathRuntimeException.createIllegalStateException(
                "statistics constructed from external moments cannot be incremented");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getResult() {
        double kurtosis = Double.NaN;
        if (moment.getN() > 3) {
            final double variance = moment.m2 / (moment.n - 1);
            if (moment.n <= 3 || variance < 10E-20) {
                kurtosis = 0.0;
            } else {
                final double n = moment.n;
                kurtosis =
                    (n * (n + 1) * moment.m4 -
                        3 * moment.m2 * moment.m2 * (n - 1)) /
                        ((n - 1) * (n - 2) * (n - 3) * variance * variance);
            }
        }
        return kurtosis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (incMoment) {
            moment.clear();
        } else {
            throw MathRuntimeException.createIllegalStateException(
                "statistics constructed from external moments cannot be cleared");
        }
    }

    /**
     * {@inheritDoc}
     */
    public long getN() {
        return moment.getN();
    }

    /* UnvariateStatistic Approach  */

    /**
     * Returns the kurtosis of the entries in the specified portion of the
     * input array.
     * <p>
     * See {@link Kurtosis} for details on the computing algorithm.</p>
     * <p>
     * Throws <code>IllegalArgumentException</code> if the array is null.</p>
     *
     * @param values the input array
     * @param begin  index of the first array element to include
     * @param length the number of elements to include
     * @return the kurtosis of the values or Double.NaN if length is less than
     * 4
     * @throws IllegalArgumentException if the input array is null or the array
     *                                  index parameters are not valid
     */
    @Override
    public double evaluate(final double[] values, final int begin, final int length) {
        // Initialize the kurtosis
        double kurt = Double.NaN;

        if (test(values, begin, length) && length > 3) {

            // Compute the mean and standard deviation
            final Variance variance = new Variance();
            variance.incrementAll(values, begin, length);
            final double mean = variance.moment.m1;
            final double stdDev = Math.sqrt(variance.getResult());

            // Sum the ^4 of the distance from the mean divided by the
            // standard deviation
            double accum3 = 0.0;
            for (int i = begin; i < begin + length; i++) {
                accum3 += Math.pow(values[i] - mean, 4.0);
            }
            accum3 /= Math.pow(stdDev, 4.0d);

            // Get N
            final double n0 = length;

            final double coefficientOne =
                n0 * (n0 + 1) / ((n0 - 1) * (n0 - 2) * (n0 - 3));
            final double termTwo =
                3 * Math.pow(n0 - 1, 2.0) / ((n0 - 2) * (n0 - 3));

            // Calculate kurtosis
            kurt = coefficientOne * accum3 - termTwo;
        }
        return kurt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Kurtosis copy() {
        final Kurtosis result = new Kurtosis();
        copy(this, result);
        return result;
    }

    /**
     * Copies source to dest.
     * <p>Neither source nor dest can be null.</p>
     *
     * @param source Kurtosis to copy
     * @param dest   Kurtosis to copy to
     * @throws NullPointerException if either source or dest is null
     */
    public static void copy(final Kurtosis source, final Kurtosis dest) {
        dest.moment = source.moment.copy();
        dest.incMoment = source.incMoment;
    }

}

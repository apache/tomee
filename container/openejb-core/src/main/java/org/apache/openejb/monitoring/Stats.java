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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.monitoring;

import org.apache.openejb.math.stat.descriptive.SynchronizedDescriptiveStatistics;

/**
 * @version $Rev$ $Date$
 */
@Managed(append = true)
public class Stats {

    private final Event event = new Event();
    private final SynchronizedDescriptiveStatistics samples;

    public Stats() {
        this(1000);
    }

    public Stats(final int window) {
        this.samples = new SynchronizedDescriptiveStatistics(window);
    }

    @Managed
    public void setSampleSize(int i) {
        samples.setWindowSize(i);
    }

    @Managed
    public int getSampleSize() {
        return samples.getWindowSize();
    }

    @Managed
    public double getPercentile99() {
        return samples.getPercentile(99.0);
    }

    @Managed
    public double getPercentile90() {
        return samples.getPercentile(90.0);
    }

    @Managed
    public double getPercentile75() {
        return samples.getPercentile(75.0);
    }

    @Managed
    public double getPercentile50() {
        return samples.getPercentile(50.0);
    }

    @Managed
    public double getPercentile25() {
        return samples.getPercentile(25.0);
    }

    @Managed
    public double getPercentile10() {
        return samples.getPercentile(10.0);
    }

    @Managed
    public double getPercentile01() {
        return samples.getPercentile(1.0);
    }

    @Managed
    public double getStandardDeviation() {
        return samples.getStandardDeviation();
    }

    @Managed
    public double getMean() {
        return samples.getMean();
    }

    @Managed
    public double getVariance() {
        return samples.getVariance();
    }

    @Managed
    public double getGeometricMean() {
        return samples.getGeometricMean();
    }

    @Managed
    public double getSkewness() {
        return samples.getSkewness();
    }

    @Managed
    public double getKurtosis() {
        return samples.getKurtosis();
    }

    @Managed
    public double getMax() {
        return samples.getMax();
    }

    @Managed
    public double getMin() {
        return samples.getMin();
    }

    @Managed
    public double getSum() {
        return samples.getSum();
    }

    @Managed
    public double getSumsq() {
        return samples.getSumsq();
    }

    @Managed
    public double[] sortedValues() {
        return samples.getSortedValues();
    }

    @Managed
    public double[] values() {
        return samples.getValues();
    }

    public void record(long time) {
        event.record();
        samples.addValue(time);
    }

    @Managed
    public long getCount() {
        return event.get();
    }

    @Managed
    public String getLatest() {
        return event.getLatest();
    }

    @Managed
    public long getLatestTime() {
        return event.getLatestTime();
    }
}

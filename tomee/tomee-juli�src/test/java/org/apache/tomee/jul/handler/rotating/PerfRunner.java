/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.jul.handler.rotating;

import org.apache.juli.OneLineFormatter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

/*
#1 thread
Benchmark                     Mode  Cnt      Score       Error  Units
PerfRunner.bufferizedLogger  thrpt    5  70875.992 ± 14974.425  ops/s
PerfRunner.defaultLogger     thrpt    5  54832.426 ± 11709.029  ops/s

#30 threads
Benchmark                     Mode  Cnt       Score        Error  Units
PerfRunner.bufferizedLogger  thrpt    5  123684.947 ± 103294.959  ops/s
PerfRunner.defaultLogger     thrpt    5   62014.127 ±  36682.710  ops/s
 */
@State(Scope.Benchmark)
public class PerfRunner {
    private Logger defaultLogger;
    private Logger bufferizedLogger;

    @Setup
    public void setup() {
        {
            defaultLogger = Logger.getLogger("perf.logger.default");
            cleanHandlers(defaultLogger);

            final Map<String, String> config = new HashMap<>();

            // initial config
            config.put("filenamePattern", "target/PerfRunner/logs/performance.default.%s.%02d.log");
            config.put("limit", "10 Mega");
            config.put("formatter", OneLineFormatter.class.getName());
            defaultLogger.addHandler(new LocalFileHandler() {
                @Override
                protected String getProperty(final String name, final String defaultValue) {
                    final String key = name.substring(name.lastIndexOf('.') + 1);
                    return config.containsKey(key) ? config.get(key) : defaultValue;
                }
            });
        }
        {
            bufferizedLogger = Logger.getLogger("perf.logger.buffer");
            cleanHandlers(bufferizedLogger);

            final Map<String, String> config = new HashMap<>();

            // initial config
            config.put("filenamePattern", "target/PerfRunner/logs/performance.buffer.%s.%02d.log");
            config.put("limit", "10 Mega");
            config.put("bufferSize", "1 Mega");
            config.put("formatter", OneLineFormatter.class.getName());
            bufferizedLogger.addHandler(new LocalFileHandler() {
                @Override
                protected String getProperty(final String name, final String defaultValue) {
                    final String key = name.substring(name.lastIndexOf('.') + 1);
                    return config.containsKey(key) ? config.get(key) : defaultValue;
                }
            });
        }
    }

    @TearDown
    public void tearDown() {
        defaultLogger.getHandlers()[0].close();
        bufferizedLogger.getHandlers()[0].close();
    }

    private void cleanHandlers(final Logger logger) {
        for (final Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        logger.setUseParentHandlers(false);
    }

    @Benchmark
    public void defaultLogger() {
        defaultLogger.info("something happens here and nowhere else so i need to write it down");
    }


    @Benchmark
    public void bufferizedLogger() {
        bufferizedLogger.info("something happens here and nowhere else so i need to write it down");
    }

    public static void main(final String[] args) throws RunnerException {
        new Runner(new OptionsBuilder()
                .include(PerfRunner.class.getSimpleName())
                .forks(0)
                .warmupIterations(5)
                .measurementIterations(5)
                .threads(1)
                .build())
                .run();
    }
}

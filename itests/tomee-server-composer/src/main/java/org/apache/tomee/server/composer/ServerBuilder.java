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
package org.apache.tomee.server.composer;

import org.tomitribe.swizzle.stream.StreamBuilder;
import org.tomitribe.util.Duration;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * It is an intentional design of this class that the build() method be implemented by
 * the actual Builder subclass.
 *
 * It is further a design goal of this class that the build() method of the subclasses
 * be something that can be executed multiple times, allowing for clustered setups
 * to be created without the need for more than one builder instance.
 *
 * This class effectively allows "building logic" to be registered in the form of a
 * handful of java.util.function.* classes.  This allows the building logic to be
 * collected and then executed in the `build()` method, facilitating the idea that
 * the build method could be safely executed N times.
 *
 * It is an essential design choice for builders to not modify the state in this class
 * in their `build()` methods.  The result of respecting this design choice is that
 * they could do fun things like hand their `build()` method over as a `Supplier` to
 * the `java.util.Stream.generate()` method of the Java 8 Streams API.
 *
 * @param <T> Should be set to the subclasses' type
 */
public abstract class ServerBuilder<T extends ServerBuilder<T>> {

    protected final File archive;
    protected FileFilter filter = pathname -> true;
    protected Duration await = new Duration("3 minutes");
    protected boolean list = false;
    protected boolean debug = false;
    protected final ArrayList<Consumer<File>> homeConsumers = new ArrayList<>();
    protected final ArrayList<Consumer<T>> builderConsumers = new ArrayList<>();
    protected final ArrayList<Consumer<StreamBuilder>> watches = new ArrayList<>();
    protected final Map<String, String> env = new HashMap<>();
    protected final Archive modifications = Archive.archive();

    public ServerBuilder(final String coordinates) throws IOException {
        this(Mvn.mvn(coordinates));
    }
    public ServerBuilder(final File archive) throws IOException {
        this.archive = archive;
    }

    public Duration await() {
        return this.await;
    }

    public File archive() {
        return archive;
    }

    public T env(final String name, final String value) {
        env.put(name, value);
        return (T) this;
    }

    public T add(final String name, final byte[] bytes) {
        modifications.add(name, bytes);
        return (T) this;
    }

    public T add(final String name, final Supplier<byte[]> content) {
        modifications.add(name, content);
        return (T) this;
    }

    public T add(final String name, final String content) {
        modifications.add(name, content);
        return (T) this;
    }

    public T add(final String name, final File content) {
        modifications.add(name, content);
        return (T) this;
    }

    public T home(final Consumer<File> customization) {
        homeConsumers.add(customization);
        return (T) this;
    }

    public T watch(final String token, final Consumer<String> consumer) {
        this.watches.add(stream -> stream.watch(token, consumer));
        return (T) this;
    }

    public T watch(final String begin, final String end, final Consumer<String> consumer) {
        this.watches.add(stream -> stream.watch(begin, end, consumer));
        return (T) this;
    }

    public T watch(final String token, final Runnable runnable) {
        this.watches.add(stream -> stream.watch(token, s -> runnable.run()));
        return (T) this;
    }

    public T watch(final String begin, final String end, final Runnable runnable) {
        this.watches.add(stream -> stream.watch(begin, end, s -> runnable.run()));
        return (T) this;
    }

    public T and(final Consumer<T> consumer) {
        this.builderConsumers.add(consumer);
        return (T) this;
    }

    /**
     * Filters files from the Tomcat/TomEE archive that we may not want
     * By default webapps are excluded
     */
    public T filter(final FileFilter filter) {
        this.filter = filter;
        return (T) this;
    }

    public T list(final boolean list) {
        this.list = list;
        return (T) this;
    }

    /**
     * How long should we wait for the server to start?
     * Default is 30 seconds
     */
    public T await(final Duration duration) {
        this.await = duration;
        return (T) this;
    }

    /**
     * How long should we wait for the server to start?
     * Default is 30 seconds
     */
    public T await(final long time, final TimeUnit unit) {
        this.await = new Duration(time, unit);
        return (T) this;
    }

    public T debug() {
        return debug(true);
    }

    public T debug(final boolean debug) {
        this.debug = debug;
        return (T) this;
    }

    protected void applyHomeConsumers(final File home) {
        // run any customization logic that's been added
        for (final Consumer<File> customization : homeConsumers) {
            customization.accept(home);
        }
    }

    protected void applyModifications(final File home) throws IOException {
        // copy user files
        modifications.toDir(home);
    }

    protected void applyBuilderConsumers() {
        for (final Consumer<T> consumer : builderConsumers) {
            consumer.accept((T) this);
        }
    }

}

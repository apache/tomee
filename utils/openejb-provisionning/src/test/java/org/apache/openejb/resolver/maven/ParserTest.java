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
package org.apache.openejb.resolver.maven;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

/**
 * @version $Rev$ $Date$
 */
public class ParserTest {


    @Test
    public void testPaxURLs() throws Exception {

        final String groupId = "orange";
        final String artifactId = "yellow";
        final String version = "1.0-SNAPSHOT";
        final String classifier = "square";
        final String type = "zip";

        {
            final Parser parser = new Parser(String.format("%s/%s", groupId, artifactId));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals("LATEST", parser.getVersion());
            assertEquals("jar", parser.getType());
            assertEquals(null, parser.getClassifier());
        }

        {
            final Parser parser = new Parser(String.format("%s/%s/%s", groupId, artifactId, version));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals(version, parser.getVersion());
            assertEquals("jar", parser.getType());
            assertEquals(null, parser.getClassifier());
        }

        {
            final Parser parser = new Parser(String.format("%s/%s/%s/%s", groupId, artifactId, version, type));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals(version, parser.getVersion());
            assertEquals(type, parser.getType());
            assertEquals(null, parser.getClassifier());
        }

        {
            final Parser parser = new Parser(String.format("%s/%s/%s/%s/%s", groupId, artifactId, version, type, classifier));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals(version, parser.getVersion());
            assertEquals(type, parser.getType());
            assertEquals(classifier, parser.getClassifier());
        }

        { // no version
            final Parser parser = new Parser(String.format("%s/%s//%s/%s", groupId, artifactId, type, classifier));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals("LATEST", parser.getVersion());
            assertEquals(type, parser.getType());
            assertEquals(classifier, parser.getClassifier());
        }

        { // no type
            final Parser parser = new Parser(String.format("%s/%s/%s//%s", groupId, artifactId, version, classifier));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals(version, parser.getVersion());
            assertEquals("jar", parser.getType());
            assertEquals(classifier, parser.getClassifier());
        }

        { // no classifier
            final Parser parser = new Parser(String.format("%s/%s/%s/%s/", groupId, artifactId, version, type));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals(version, parser.getVersion());
            assertEquals(type, parser.getType());
            assertEquals(null, parser.getClassifier());
        }

        { // no version or type
            final Parser parser = new Parser(String.format("%s/%s///%s", groupId, artifactId, classifier));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals("LATEST", parser.getVersion());
            assertEquals("jar", parser.getType());
            assertEquals(classifier, parser.getClassifier());
        }

        { //  no version or type or classifier
            final Parser parser = new Parser(String.format("%s/%s///", groupId, artifactId));

            assertEquals(groupId, parser.getGroup());
            assertEquals(artifactId, parser.getArtifact());
            assertEquals("LATEST", parser.getVersion());
            assertEquals("jar", parser.getType());
            assertEquals(null, parser.getClassifier());
        }


        try { //  no group
            new Parser(String.format("/%s///", artifactId));

            Assert.fail("Expected MalformedURLException");
        } catch (MalformedURLException pass) {
        }

        try { //  no artifact
            new Parser(String.format("%s////", groupId));

            Assert.fail("Expected MalformedURLException");
        } catch (MalformedURLException pass) {
        }

        try { //  no artifact
            new Parser(String.format("%s//%s", groupId, version));

            Assert.fail("Expected MalformedURLException");
        } catch (MalformedURLException pass) {
        }
    }

}

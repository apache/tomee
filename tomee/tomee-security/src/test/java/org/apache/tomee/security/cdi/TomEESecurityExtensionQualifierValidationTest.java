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
package org.apache.tomee.security.cdi;

import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Focused unit tests for {@link TomEESecurityExtension#validateQualifierUniqueness}. This covers the
 * specific failure mode where two @*AuthenticationMechanismDefinition annotations share the same
 * qualifier set but carry different attribute values -- a clash that would otherwise only surface at
 * first injection as an AmbiguousResolutionException. We exercise the helper directly because
 * spinning up Arquillian (or even tomee-embedded) for a negative-deployment assertion is
 * significantly heavier than is warranted for the pure validation logic.
 */
public class TomEESecurityExtensionQualifierValidationTest {

    @Test
    public void identicalDefinitionsWithSameQualifiersProduceNoProblem() {
        final BasicAuthenticationMechanismDefinition a =
                basicDef("realm-a", BasicAuthenticationMechanism.class);
        // The caller already dedups identical annotations via LinkedHashSet (see C1.5 in the extension),
        // so this method never sees exact duplicates; passing distinct definitions that share a realm
        // still collapses on the Map-grouping side because group.size() == 1 after dedup.
        final List<String> problems = TomEESecurityExtension.validateQualifierUniqueness(
                List.of(a),
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers);
        assertTrue("single definition must never produce a problem: " + problems, problems.isEmpty());
    }

    @Test
    public void twoDefinitionsWithSameDefaultQualifiersAndDifferentRealmsProduceProblem() {
        final BasicAuthenticationMechanismDefinition a =
                basicDef("realm-a", BasicAuthenticationMechanism.class);
        final BasicAuthenticationMechanismDefinition b =
                basicDef("realm-b", BasicAuthenticationMechanism.class);

        final List<String> problems = TomEESecurityExtension.validateQualifierUniqueness(
                List.of(a, b),
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers);

        assertEquals("expected exactly one conflict to be reported: " + problems, 1, problems.size());
        final String message = problems.get(0);
        assertTrue("message should name the annotation: " + message,
                message.contains("@BasicAuthenticationMechanismDefinition"));
        assertTrue("message should include the default qualifier simple name: " + message,
                message.contains("BasicAuthenticationMechanism"));
        assertTrue("message should include the first conflicting realm: " + message,
                message.contains("realm-a"));
        assertTrue("message should include the second conflicting realm: " + message,
                message.contains("realm-b"));
        assertTrue("message should include the remediation hint: " + message,
                message.contains("declare distinct qualifiers"));
    }

    @Test
    public void differentQualifierSetsAreNotReportedAsAConflict() {
        final BasicAuthenticationMechanismDefinition a =
                basicDef("realm-a", BasicAuthenticationMechanism.class);
        final BasicAuthenticationMechanismDefinition b =
                basicDef("realm-b", MarkerQualifier.class);

        final List<String> problems = TomEESecurityExtension.validateQualifierUniqueness(
                List.of(a, b),
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers);

        assertTrue("distinct qualifier sets must not collide: " + problems, problems.isEmpty());
    }

    @Test
    public void qualifierArrayOrderDoesNotMaskAConflict() {
        final BasicAuthenticationMechanismDefinition a =
                basicDef("realm-a", BasicAuthenticationMechanism.class, MarkerQualifier.class);
        final BasicAuthenticationMechanismDefinition b =
                basicDef("realm-b", MarkerQualifier.class, BasicAuthenticationMechanism.class);

        final List<String> problems = TomEESecurityExtension.validateQualifierUniqueness(
                List.of(a, b),
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers);

        assertEquals("different qualifier ordering still resolves to the same key set: " + problems,
                1, problems.size());
    }

    @Test
    public void emptyInputProducesNoProblem() {
        assertFalse(TomEESecurityExtension.validateQualifierUniqueness(
                List.of(),
                BasicAuthenticationMechanismDefinition.class,
                BasicAuthenticationMechanismDefinition::qualifiers)
                .iterator().hasNext());
    }

    // --- helpers ---

    /**
     * Marker used only to exercise the "different qualifier set" path. Its contents are irrelevant
     * to the validator -- only Class identity and Arrays.asList equality matter.
     */
    private @interface MarkerQualifier {
    }

    private static BasicAuthenticationMechanismDefinition basicDef(final String realmName,
                                                                   final Class<?>... qualifiers) {
        return new BasicAuthenticationMechanismDefinition() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return BasicAuthenticationMechanismDefinition.class;
            }

            @Override
            public String realmName() {
                return realmName;
            }

            @Override
            public Class<?>[] qualifiers() {
                return qualifiers;
            }

            @Override
            public String toString() {
                return "@BasicAuthenticationMechanismDefinition(realmName=\"" + realmName
                        + "\", qualifiers=" + Arrays.toString(qualifiers) + ")";
            }
        };
    }
}

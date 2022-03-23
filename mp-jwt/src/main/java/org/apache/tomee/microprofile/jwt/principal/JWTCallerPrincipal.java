/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.principal;

import org.apache.openejb.spi.CallerPrincipal;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import javax.security.auth.Subject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A default implementation of JWTCallerPrincipal using jose4j
 * Another implementation could use nimbus and another plain JSON-P
 */
@CallerPrincipal
public class JWTCallerPrincipal implements JsonWebToken {

    private static final Logger logger = Logger.getLogger(JWTCallerPrincipal.class.getName());
    private final String jwt;
    private final String type;
    private final JwtClaims claimsSet;
    private final String name;

    /**
     * Create the DefaultJWTCallerPrincipal from the parsed JWT token and the extracted principal name
     *
     * @param jwt  - the parsed JWT token representation
     * @param name - the extracted unqiue name to use as the principal name; from "upn", "preferred_username" or "sub" claim
     */
    public JWTCallerPrincipal(final String jwt, final String type, final JwtClaims claimsSet, final String name) {
        this.name = name;
        this.jwt = jwt;
        this.type = type;
        this.claimsSet = claimsSet;
        fixJoseTypes();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> Optional<T> claim(final String claimName) {
        final T claim = (T) getClaim(claimName);
        return Optional.ofNullable(claim);
    }

    @Override
    public Set<String> getAudience() {
        final Set<String> audSet = new HashSet<>();
        try {
            final List<String> audList = claimsSet.getStringListClaimValue("aud");
            if (audList != null) {
                audSet.addAll(audList);
            }

        } catch (final MalformedClaimException e) {
            try {
                final String aud = claimsSet.getStringClaimValue("aud");
                audSet.add(aud);
            } catch (final MalformedClaimException e1) {
                logger.log(Level.FINEST, "Can't retrieve malformed 'aud' claim.", e);
            }
        }
        return audSet.isEmpty() ? null : audSet;
    }

    @Override
    public Set<String> getGroups() {
        final HashSet<String> groups = new HashSet<>();
        try {
            final List<String> globalGroups = claimsSet.getStringListClaimValue("groups");
            if (globalGroups != null) {
                groups.addAll(globalGroups);
            }

        } catch (final MalformedClaimException e) {
            logger.log(Level.FINEST, "Can't retrieve malformed 'groups' claim.", e);
        }
        return groups;
    }


    @Override
    public Set<String> getClaimNames() {
        return new HashSet<>(claimsSet.getClaimNames());
    }

    public String getRawToken() {
        return jwt;
    }

    @Override
    public Object getClaim(final String claimName) {
        Claims claimType = Claims.UNKNOWN;
        Object claim = null;
        try {
            claimType = Claims.valueOf(claimName);
        } catch (IllegalArgumentException e) {
        }
        // Handle the jose4j NumericDate types and
        switch (claimType) {
            case auth_time:
            case updated_at:
                try {
                    claim = claimsSet.getClaimValue(claimType.name(), Long.class);
                    if (claim == null) {
                        claim = 0L;
                    }
                } catch (final MalformedClaimException e) {
                    logger.log(Level.FINEST, "Can't retrieve 'updated_at' a malformed claim.", e);
                }
                break;
            case groups:
                claim = getGroups();
                break;
            case aud:
                claim = getAudience();
                break;
            case UNKNOWN:
                claim = claimsSet.getClaimValue(claimName);
                break;
            default:
                claim = claimsSet.getClaimValue(claimType.name());
        }
        return claim;
    }

    @Override
    public boolean implies(final Subject subject) {
        return false;
    }

    public String toString() {
        return toString(false);
    }

    /**
     * TODO: showAll is ignored and currently assumed true
     *
     * @param showAll - should all claims associated with the JWT be displayed or should only those defined in the
     *                JsonWebToken interface be displayed.
     * @return JWTCallerPrincipal string view
     */
    public String toString(boolean showAll) {
        String toString = "DefaultJWTCallerPrincipal{" +
                "id='" + getTokenID() + '\'' +
                ", name='" + getName() + '\'' +
                ", expiration=" + getExpirationTime() +
                ", notBefore=" + getClaim(Claims.nbf.name()) +
                ", issuedAt=" + getIssuedAtTime() +
                ", issuer='" + getIssuer() + '\'' +
                ", audience=" + getAudience() +
                ", subject='" + getSubject() + '\'' +
                ", type='" + type + '\'' +
                ", issuedFor='" + getClaim("azp") + '\'' +
                ", authTime=" + getClaim("auth_time") +
                ", givenName='" + getClaim("given_name") + '\'' +
                ", familyName='" + getClaim("family_name") + '\'' +
                ", middleName='" + getClaim("middle_name") + '\'' +
                ", nickName='" + getClaim("nickname") + '\'' +
                ", preferredUsername='" + getClaim("preferred_username") + '\'' +
                ", email='" + getClaim("email") + '\'' +
                ", emailVerified=" + getClaim(Claims.email_verified.name()) +
                ", allowedOrigins=" + getClaim("allowedOrigins") +
                ", updatedAt=" + getClaim("updated_at") +
                ", acr='" + getClaim("acr") + '\'';

        final StringBuilder tmp = new StringBuilder(toString);
        tmp.append(", groups=[");
        for (String group : getGroups()) {
            tmp.append(group);
            tmp.append(',');
        }
        tmp.setLength(tmp.length() - 1);
        tmp.append("]}");
        return tmp.toString();
    }

    /**
     * Convert the types jose4j uses for address, sub_jwk, and jwk
     */
    private void fixJoseTypes() {
        if (claimsSet.hasClaim(Claims.address.name())) {
            replaceMap(Claims.address.name());
        }
        if (claimsSet.hasClaim(Claims.jwk.name())) {
            replaceMap(Claims.jwk.name());
        }
        if (claimsSet.hasClaim(Claims.sub_jwk.name())) {
            replaceMap(Claims.sub_jwk.name());
        }

        // Handle custom claims
        final Set<String> customClaimNames = filterCustomClaimNames(claimsSet.getClaimNames());
        for (String name : customClaimNames) {
            final Object claimValue = claimsSet.getClaimValue(name);
            if (claimValue instanceof List) {
                replaceList(name);

            } else if (claimValue instanceof Map) {
                replaceMap(name);

            } else if (claimValue instanceof Number) {
                replaceNumber(name);
            }
        }
    }

    /**
     * Determine the custom claims in the set
     *
     * @param claimNames - the current set of claim names in this token
     * @return the possibly empty set of names for non-Claims claims
     */
    private Set<String> filterCustomClaimNames(final Collection<String> claimNames) {
        final HashSet<String> customNames = new HashSet<>(claimNames);
        for (Claims claim : Claims.values()) {
            customNames.remove(claim.name());
        }
        return customNames;
    }

    /**
     * Replace the jose4j Map<String,Object> with a JsonObject
     *
     * @param name - claim name
     */
    private void replaceMap(final String name) {
        try {
            final Map<String, Object> map = claimsSet.getClaimValue(name, Map.class);
            final JsonObject jsonObject = replaceMap(map);
            claimsSet.setClaim(name, jsonObject);

        } catch (final MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceMap failure for: " + name, e);
        }
    }

    private JsonObject replaceMap(final Map<String, Object> map) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                final JsonObject entryJsonObject = replaceMap((Map<String, Object>) entryValue);
                builder.add(entry.getKey(), entryJsonObject);

            } else if (entryValue instanceof List) {
                final JsonArray array = (JsonArray) wrapValue(entryValue);
                builder.add(entry.getKey(), array);

            } else if (entryValue instanceof Long || entryValue instanceof Integer) {
                final long lvalue = ((Number) entryValue).longValue();
                builder.add(entry.getKey(), lvalue);

            } else if (entryValue instanceof Double || entryValue instanceof Float) {
                final double value = ((Number) entryValue).doubleValue();
                builder.add(entry.getKey(), value);

            } else if (entryValue instanceof Boolean) {
                final boolean flag = ((Boolean) entryValue).booleanValue();
                builder.add(entry.getKey(), flag);

            } else if (entryValue instanceof String) {
                builder.add(entry.getKey(), entryValue.toString());
            }
        }
        return builder.build();
    }

    private JsonValue wrapValue(final Object value) {
        JsonValue jsonValue = null;
        if (value instanceof Number) {
            final Number number = (Number) value;
            if ((number instanceof Long) || (number instanceof Integer)) {
                jsonValue = Json.createObjectBuilder()
                        .add("tmp", number.longValue())
                        .build()
                        .getJsonNumber("tmp");

            } else {
                jsonValue = Json.createObjectBuilder()
                        .add("tmp", number.doubleValue())
                        .build()
                        .getJsonNumber("tmp");
            }

        } else if (value instanceof Boolean) {
            final Boolean flag = (Boolean) value;
            jsonValue = flag ? JsonValue.TRUE : JsonValue.FALSE;

        } else if (value instanceof List) {
            final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            final List list = (List) value;
            for (Object element : list) {
                if (element instanceof String) {
                    arrayBuilder.add(element.toString());

                } else {
                    JsonValue jvalue = wrapValue(element);
                    arrayBuilder.add(jvalue);
                }

            }
            jsonValue = arrayBuilder.build();

        }
        return jsonValue;
    }


    /**
     * Replace the jose4j List<?> with a JsonArray
     *
     * @param name - claim name
     */
    private void replaceList(final String name) {
        try {
            final List list = claimsSet.getClaimValue(name, List.class);
            final JsonArray array = (JsonArray) wrapValue(list);
            claimsSet.setClaim(name, array);

        } catch (final MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceList failure for: " + name, e);
        }
    }

    private void replaceNumber(final String name) {
        try {
            final Number number = claimsSet.getClaimValue(name, Number.class);
            final JsonNumber jsonNumber = (JsonNumber) wrapValue(number);
            claimsSet.setClaim(name, jsonNumber);

        } catch (final MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceNumber failure for: " + name, e);
        }
    }

}
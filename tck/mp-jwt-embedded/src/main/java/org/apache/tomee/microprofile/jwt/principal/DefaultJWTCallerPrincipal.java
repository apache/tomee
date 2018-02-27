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

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.security.auth.Subject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A default implementation of JWTCallerPrincipal using jose4j
 * Another implementation could use nimbus and another plain JSON-P
 */
public class DefaultJWTCallerPrincipal extends JWTCallerPrincipal {

    private static Logger logger = Logger.getLogger(DefaultJWTCallerPrincipal.class.getName());
    private String jwt;
    private String type;
    private JwtClaims claimsSet;

    /**
     * Create the DefaultJWTCallerPrincipal from the parsed JWT token and the extracted principal name
     *
     * @param jwt  - the parsed JWT token representation
     * @param name - the extracted unqiue name to use as the principal name; from "upn", "preferred_username" or "sub" claim
     */
    public DefaultJWTCallerPrincipal(final String jwt, final String type, final JwtClaims claimsSet, final String name) {
        super(name);
        this.jwt = jwt;
        this.type = type;
        this.claimsSet = claimsSet;
        fixJoseTypes();
    }

    @Override
    public Set<String> getAudience() {
        Set<String> audSet = new HashSet<>();
        try {
            List<String> audList = claimsSet.getStringListClaimValue("aud");
            if (audList != null) {
                audSet.addAll(audList);
            }
        } catch (MalformedClaimException e) {
            try {
                String aud = claimsSet.getStringClaimValue("aud");
                audSet.add(aud);
            } catch (MalformedClaimException e1) {
            }
        }
        return audSet.isEmpty() ? null : audSet;
    }

    @Override
    public Set<String> getGroups() {
        HashSet<String> groups = new HashSet<>();
        try {
            List<String> globalGroups = claimsSet.getStringListClaimValue("groups");
            if (globalGroups != null) {
                groups.addAll(globalGroups);
            }
        } catch (MalformedClaimException e) {
            e.printStackTrace();
        }
        return groups;
    }


    @Override
    public Set<String> getClaimNames() {
        return new HashSet<>(claimsSet.getClaimNames());
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
            case exp:
            case iat:
            case auth_time:
            case nbf:
            case updated_at:
                try {
                    claim = claimsSet.getClaimValue(claimType.name(), Long.class);
                    if (claim == null) {
                        claim = new Long(0);
                    }
                } catch (MalformedClaimException e) {
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
    @Override
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
        StringBuilder tmp = new StringBuilder(toString);
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
        Set<String> customClaimNames = filterCustomClaimNames(claimsSet.getClaimNames());
        for (String name : customClaimNames) {
            Object claimValue = claimsSet.getClaimValue(name);
            Class claimType = claimValue.getClass();
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
    private Set<String> filterCustomClaimNames(Collection<String> claimNames) {
        HashSet<String> customNames = new HashSet<>(claimNames);
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
    private void replaceMap(String name) {
        try {
            Map<String, Object> map = claimsSet.getClaimValue(name, Map.class);
            JsonObject jsonObject = replaceMap(map);
            claimsSet.setClaim(name, jsonObject);
        } catch (MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceMap failure for: " + name, e);
        }
    }

    private JsonObject replaceMap(Map<String, Object> map) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                JsonObject entryJsonObject = replaceMap((Map<String, Object>) entryValue);
                builder.add(entry.getKey(), entryJsonObject);
            } else if (entryValue instanceof List) {
                JsonArray array = (JsonArray) wrapValue(entryValue);
                builder.add(entry.getKey(), array);
            } else if (entryValue instanceof Long || entryValue instanceof Integer) {
                long lvalue = ((Number) entryValue).longValue();
                builder.add(entry.getKey(), lvalue);
            } else if (entryValue instanceof Double || entryValue instanceof Float) {
                double dvalue = ((Number) entryValue).doubleValue();
                builder.add(entry.getKey(), dvalue);
            } else if (entryValue instanceof Boolean) {
                boolean flag = ((Boolean) entryValue).booleanValue();
                builder.add(entry.getKey(), flag);
            } else if (entryValue instanceof String) {
                builder.add(entry.getKey(), entryValue.toString());
            }
        }
        return builder.build();
    }

    private JsonValue wrapValue(Object value) {
        JsonValue jsonValue = null;
        if (value instanceof Number) {
            Number number = (Number) value;
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
            Boolean flag = (Boolean) value;
            jsonValue = flag ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof List) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            List list = (List) value;
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
    private void replaceList(String name) {
        try {
            List list = claimsSet.getClaimValue(name, List.class);
            JsonArray array = (JsonArray) wrapValue(list);
            claimsSet.setClaim(name, array);
        } catch (MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceList failure for: " + name, e);
        }
    }

    private void replaceNumber(String name) {
        try {
            Number number = claimsSet.getClaimValue(name, Number.class);
            JsonNumber jsonNumber = (JsonNumber) wrapValue(number);
            claimsSet.setClaim(name, jsonNumber);
        } catch (MalformedClaimException e) {
            logger.log(Level.WARNING, "replaceNumber failure for: " + name, e);
        }
    }

}
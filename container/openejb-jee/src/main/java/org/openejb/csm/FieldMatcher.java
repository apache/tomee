/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.csm;

import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.lang.reflect.Field;

/**
 * @version $Revision$ $Date$
 */
public class FieldMatcher {

    private SchemaParticle particle;
    private final SchemaType schemaType;
    private final Class clazz;
    private final Map<String, Field> fields;
    private final Map<String, SchemaParticle> particles;
    private final Map<String, Field> unmatchedFields;
    private final Map<String, SchemaParticle> unmatchedParticles;
    private List<MappedField> matched;

    public FieldMatcher(SchemaParticle particle, Class clazz) {
        this.particle = particle;
        this.schemaType = particle.getType();
        this.clazz = clazz;
        this.fields = collectChildren(clazz);
        this.particles = collectChildren(schemaType);
        this.unmatchedFields = new HashMap<String, Field>(fields);
        this.unmatchedParticles = new HashMap<String, SchemaParticle>(particles);
    }

    public SchemaParticle getParticle() {
        return particle;
    }

    public Map<String, Field> getFields() {
        return new HashMap(fields);
    }

    public Map<String, SchemaParticle> getParticles() {
        return new LinkedHashMap(particles);
    }

    private static Map<String,Field> collectChildren(Class clazz) {
        Map unmatchedFields = new HashMap();

        Class type = clazz;
        while (type != null && type != Object.class) {
            Field[] fieldsArray = type.getDeclaredFields();
            for (int i = 0; i < fieldsArray.length; i++) {
                Field field1 = fieldsArray[i];
                unmatchedFields.put(field1.getName(), field1);
            }
            type = type.getSuperclass();
        }
        return unmatchedFields;
    }

    public static Map<String,SchemaParticle> collectChildren(SchemaType type) {
        Map fieldSchemas = new LinkedHashMap<String, SchemaParticle>();
        try {
            if (type == null || type.getContentModel() == null) {
                return fieldSchemas;
            }
            
            SchemaParticle contentModel = type.getContentModel();
            if (contentModel.getParticleType() == SchemaParticle.SEQUENCE) {
                SchemaParticle[] children = contentModel.getParticleChildren();

                for (int i = 0; i < children.length; i++) {
                    SchemaParticle child = children[i];
                    if (child.getParticleType() == SchemaParticle.ELEMENT) {
                        fieldSchemas.put(child.getName().getLocalPart(), child);
                    } else if (child.getParticleType() == SchemaParticle.CHOICE) {
                        for (int j = 0; j < child.getParticleChildren().length; j++) {
                            SchemaParticle choice = child.getParticleChildren()[j];
                            fieldSchemas.put(choice.getName().getLocalPart(), choice);
                        }
                    }
                }
            } else if (contentModel.getParticleType() == SchemaParticle.ELEMENT) {
                fieldSchemas.put(contentModel.getName().getLocalPart(), contentModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fieldSchemas;
    }

    public List<MappedField> getMatched() {
        return matched;
    }

    public MatchSet match() {
        Map matched = new HashMap();

        // Match explicitly by name
        for (Iterator iterator11 = new HashMap(unmatchedParticles).entrySet().iterator(); iterator11.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator11.next();
            String name = (String) entry1.getKey();
            SchemaParticle particle1 = (SchemaParticle) entry1.getValue();
            Field field1 = (Field) unmatchedFields.get(name);
            if (field1 != null) {
                matched.put(particle1, field1);
                unmatchedFields.remove(field1.getName());
                (unmatchedParticles).remove(particle1.getName().getLocalPart());
            }
        }

        // Match explicitly by java name
        for (Iterator iterator12 = new HashMap(unmatchedParticles).entrySet().iterator(); iterator12.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator12.next();
            String name = (String) entry1.getKey();
            SchemaParticle particle1 = (SchemaParticle) entry1.getValue();
            String javaName = NameConverter.getJavaFieldName(name);
            Field field1 = (Field) unmatchedFields.get(javaName);
            if (field1 != null) {
                matched.put(particle1, field1);
                unmatchedFields.remove(field1.getName());
                (unmatchedParticles).remove(particle1.getName().getLocalPart());
            }
        }

        // Try plural name for collections
        for (Iterator iterator13 = new HashMap(unmatchedParticles).entrySet().iterator(); iterator13.hasNext();) {
            Map.Entry entry1 = (Map.Entry) iterator13.next();
            String name = (String) entry1.getKey();
            SchemaParticle particle1 = (SchemaParticle) entry1.getValue();
            String javaName = NameConverter.getJavaFieldName(name);
            if (particle1.getIntMaxOccurs() > 1) {
                if (unmatchedFields.containsKey(javaName + "s")) {
                    javaName += "s";
                } else if (unmatchedFields.containsKey(javaName.replaceFirst("y$", "ies"))) {
                    javaName = javaName.replaceFirst("y$", "ies");
                }
            }
            Field field1 = (Field) unmatchedFields.get(javaName);
            if (field1 != null) {
                matched.put(particle1, field1);
                unmatchedFields.remove(field1.getName());
                (unmatchedParticles).remove(particle1.getName().getLocalPart());
            }
        }

        // Matching words -- 10pts for exact, 3pts for startsWith
        for (Iterator iterator14 = new HashMap(unmatchedParticles).entrySet().iterator(); iterator14.hasNext();) {
            Map.Entry entry11 = (Map.Entry) iterator14.next();
            String name = (String) entry11.getKey();
            SchemaParticle particle1 = (SchemaParticle) entry11.getValue();

            Field closest = null;
            int highest = 0;

            String[] words = name.split("-");
            for (Iterator iterator1 = unmatchedFields.entrySet().iterator(); iterator1.hasNext();) {
                Map.Entry entry1 = (Map.Entry) iterator1.next();
                String fieldName = (String) entry1.getKey();
                Field field1 = (Field) entry1.getValue();

                List fieldWords = new ArrayList(Arrays.asList(NameConverter.getXmlName(fieldName).split("-")));
                List elementWords = new ArrayList(Arrays.asList(words));
                int hits = 0;
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (fieldWords.contains(word)) {
                        fieldWords.remove(word);
                        elementWords.remove(word);
                        hits += 10;
                    }
                }
                ArrayList unmatchedElementWords = new ArrayList(elementWords);
                for (int j = 0; j < unmatchedElementWords.size(); j++) {
                    String elementWord = (String) unmatchedElementWords.get(j);
                    ArrayList unmatchedFieldWords = new ArrayList(fieldWords);
                    for (int i = 0; i < unmatchedFieldWords.size(); i++) {
                        String fieldWord = (String) unmatchedFieldWords.get(i);
                        if (fieldWord.startsWith(elementWord) || elementWord.startsWith(fieldWord)) {
                            hits += 3;
                            fieldWords.remove(fieldWord);
                            elementWords.remove(elementWord);
                        }
                    }
                }
                if (hits > highest) {
                    highest = hits;
                    closest = field1;
                }
            }

            Field field11 = closest;
            if (field11 != null) {
                matched.put(particle1, field11);
                unmatchedFields.remove(field11.getName());
                (unmatchedParticles).remove(particle1.getName().getLocalPart());
            }
        }

        List<MappedField> fields1 = new ArrayList<MappedField>();
        for (Iterator iterator16 = particles.values().iterator(); iterator16.hasNext();) {
            SchemaParticle particle1 = (SchemaParticle) iterator16.next();
            Field field1 = (Field) matched.get(particle1);
            if (field1 != null) {
                MappedField afield = new MappedField(particle1, field1);
                fields1.add(afield);
            }
        }

        validateMatches(fields1);

        MatchSet matchSet = new MatchSet(particle, clazz, unmatchedFields, unmatchedParticles, fields1);
        return matchSet;
    }

    private void validateMatches(List<MappedField> fields) {
        for (int i = 0; i < fields.size(); i++) {
            MappedField mappedField = fields.get(i);
            if (mappedField.isJavaCollection() && !mappedField.isXmlCollection()){
                SchemaParticle particle = mappedField.getParticle();
                SchemaParticle model = particle.getType().getContentModel();
                if (particle.getIntMaxOccurs() == 1 && model == null){
                    System.out.println("        // not assignable to a list "+ mappedField);
                } else if (particle.getIntMaxOccurs() == 1 && model.getParticleType() == SchemaParticle.SEQUENCE){
                    System.out.println("        // vague list match "+ mappedField);
                } else {
                    System.out.println("        // Haze: Java collection found and no clear xml collection: "+mappedField);
                }
            }
        }
    }

}

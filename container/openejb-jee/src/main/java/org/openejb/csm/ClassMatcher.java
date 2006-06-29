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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @version $Revision$ $Date$
 */
public class ClassMatcher {

    public static class Entry {
        private final String key;
        private final Object value;

        public Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    public Entry match(String token, Map map, String suffix){

        // Match explicitly by name
        Object object = map.get(token + suffix);
        if (object != null){
            return new Entry(token + suffix, object);
        }

        Entry closest = null;

        int highest = 0;

        List tokenWords = new ArrayList(Arrays.asList(NameConverter.getXmlName(token).split("-")));

        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            List unmatchedTokenWords = new ArrayList(tokenWords);
            List unmatchedItemWords = new ArrayList(Arrays.asList(NameConverter.getXmlName(key).split("-")));

            int hits = 0;

            // Match words litterally -- 10 pts
            for (int i = 0; i < unmatchedTokenWords.size(); i++) {
                String word = (String) unmatchedTokenWords.get(i);
                if (unmatchedItemWords.contains(word)) {
                    unmatchedItemWords.remove(word);
                    unmatchedTokenWords.remove(word);
                    hits += 10;
                }
            }

            // Match words with haze -- 3 pts
            for (int i = 0; i < unmatchedTokenWords.size(); i++) {
                String tokenWord = (String) unmatchedTokenWords.get(i);
                for (int j = 0; j < unmatchedItemWords.size(); j++) {
                    String itemWord = (String) unmatchedItemWords.get(j);
                    if (itemWord.startsWith(tokenWord) || tokenWord.startsWith(itemWord)){
                        hits += 3;
                        unmatchedItemWords.remove(itemWord);
                        unmatchedTokenWords.remove(tokenWord);
                    }
                }
            }

            if (hits > highest) {
                highest = hits;
                closest = new Entry(key, value);
            }

        }
        return closest;
    }
}

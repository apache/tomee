/*
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package jpa.tools.swing;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;

/**
 * Displays Properties.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public class ConfigurationViewer extends JTextPane {
    private static final char SPACE = ' ';

    public ConfigurationViewer(String title, Map<String, Object> config) {
        super();
        setBorder(BorderFactory.createTitledBorder(title));
        TreeSet<String> sortedKeys = new TreeSet<String>(config.keySet());
        int L = getMaxLength(sortedKeys);
        for (String key : sortedKeys) {
            setCaretPosition(getDocument().getLength());
            setCharacterAttributes(TextStyles.KEYS, true);
            replaceSelection(key + pad(L - key.length()) + ":");
            setCaretPosition(getDocument().getLength());
            setCharacterAttributes(TextStyles.VALUES, true);
            replaceSelection(toString(config.get(key)) + "\r\n");
        }
    }
    
    String toString(Object value) {
        if (value == null)
            return "null";
        if (value.getClass().isArray()) {
            return Arrays.toString((Object[])value);
        }
        return value.toString();
    }

    private int getMaxLength(Set<String> keys) {
        int len = 1;
        for (String s : keys) {
            len = Math.max(s.length(), len);
        }
        return len;
    }

    private String pad(int n) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < n; i++)
            buf.append(SPACE);
        return buf.toString();
    }

}

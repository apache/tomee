/*
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

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class TextStyles {
    public static AttributeSet KEYS, VALUES;
    static {
        StyleContext ctx = StyleContext.getDefaultStyleContext();
        KEYS = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE);
        KEYS = ctx.addAttribute(KEYS, StyleConstants.Bold, true);
        KEYS = ctx.addAttribute(KEYS, StyleConstants.FontSize, 14);
        KEYS = ctx.addAttribute(KEYS, StyleConstants.FontFamily, "Courier");
        
        Color indianRed = new Color(205, 92, 92);
        VALUES = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, indianRed);
        VALUES = ctx.addAttribute(VALUES, StyleConstants.Bold, true);
        VALUES = ctx.addAttribute(VALUES, StyleConstants.FontFamily, "Courier");
        VALUES = ctx.addAttribute(VALUES, StyleConstants.FontSize, 14);
    }

}

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
package openbook.client;

import java.awt.Color;
import java.io.PrintStream;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import jpa.tools.swing.GraphicOutputStream;



import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;

/**
 * Logs SQL statement to a graphic console.
 * 
 * @author Pinaki Poddar
 *
 */
public class SQLLogger extends AbstractJDBCListener {
    private PrintStream out = null;
    private static AttributeSet red, green, blue, magenta;
    static {
        StyleContext ctx = StyleContext.getDefaultStyleContext();
        red = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
        green = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.GREEN);
        blue = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE);
        magenta = ctx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.MAGENTA);
    }
    
    public void setOutput(GraphicOutputStream o) {
        out = new PrintStream(o, true);
        o.registerStyle("INSERT", green);
        o.registerStyle("SELECT", blue);
        o.registerStyle("UPDATE", magenta);
        o.registerStyle("DELETE", red);
    }
    
    @Override
    public void beforeExecuteStatement(final JDBCEvent event) {
        if (out == null)
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                out.println(event.getSQL());
            }
        });
    }
}

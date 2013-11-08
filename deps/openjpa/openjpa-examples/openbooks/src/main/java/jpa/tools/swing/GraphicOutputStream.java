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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;

/**
 * An output stream that uses a {@link ScrollingTextPane} as its sink.
 * Flushes the buffer at line end.
 * 
 * @author Pinaki Poddar
 *
 */
public class GraphicOutputStream extends OutputStream {
    ScrollingTextPane _sink;
    private char[] buffer = new char[1024];
    private int count;
    private Map<String, AttributeSet> _styles = new HashMap<String, AttributeSet>();
    private static AttributeSet _defaultStyle = StyleContext.getDefaultStyleContext()
                                                            .getStyle(StyleContext.DEFAULT_STYLE);
    
    public GraphicOutputStream(ScrollingTextPane delegate) {
        _sink = delegate;
    }
    
    public void registerStyle(String pattern, AttributeSet style) {
        _styles.put(pattern, style);
    }
    
    @Override
    public void write(int b) throws IOException {
        buffer[count++] = (char)b;
        if (count > buffer.length || b == '\r' || b == '\n') {
            flushBuffer();
        }
    }
    
    private void flushBuffer() {
        String txt = new String(buffer, 0, count);
        count = 0;
        AttributeSet style = getStyle(txt);
        _sink.setCharacterAttributes(style, true);
        _sink.appendText(txt);
    }
    
    AttributeSet getStyle(String txt) {
        if (txt == null || txt.isEmpty())
            return _defaultStyle;
        for (String pattern : _styles.keySet()) {
            if (txt.startsWith(pattern))
                return _styles.get(pattern);
        }
        return _defaultStyle;
    }

}

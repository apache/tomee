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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.Document;

/**
 * A TextPane where text that scrolls as new text is appended.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class ScrollingTextPane extends JTextPane {
    
    public void appendText(String text) {
        if (text == null)
            return;
        try {
            Document doc = getDocument();
            setCaretPosition(doc.getLength());
            replaceSelection(text);
            Rectangle r = modelToView(doc.getLength());
            if (r != null) {
                scrollRectToVisible(r);
            }
        } catch (Exception e) {
            
        }
    }
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final ScrollingTextPane test = new ScrollingTextPane();
        f.getContentPane().add(new JScrollPane(test));
        f.pack();
        f.setSize(600,450);
        f.setVisible(true);
        
        Timer timer = new Timer(1000, new ActionListener() {
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
            @Override
            public void actionPerformed(ActionEvent e) {
                test.appendText(fmt.format(new Date()) + "\r\n");
            }
        });
        timer.start();
    }

}

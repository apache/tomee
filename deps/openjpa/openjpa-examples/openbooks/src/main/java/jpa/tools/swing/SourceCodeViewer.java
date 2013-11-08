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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * An internal viewer for HTML formatted source code.
 * The input to this viewer is a root URL.
 * The viewer shows the anchors in a combo-box and displays the
 * corresponding HTML in the main editor. 
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class SourceCodeViewer extends JPanel {
    private final JEditorPane _editor;
    private final JComboBox   _bookmarks;
    private IndexedMap<String, URI> _anchors = new IndexedMap<String, URI>();
    private LinkedList<String> _visited = new LinkedList<String>();
    
    /**
     * Create a Source Code Browser.
     */
    public SourceCodeViewer() {
        super(true);
        setLayout(new BorderLayout());
        
        _editor = new JEditorPane();
        _editor.setContentType("text/html");
        _editor.setEditable(false);
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        _bookmarks = new JComboBox(model);
        _bookmarks.setEditable(false);
        
        _bookmarks.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showAnchor((String)_bookmarks.getSelectedItem());
            }
        });
        
        JPanel topPanel = new JPanel();
        ((FlowLayout)topPanel.getLayout()).setAlignment(FlowLayout.LEADING);
        topPanel.add(new JLabel("Go to "));
        topPanel.add(_bookmarks);
        topPanel.add(Box.createHorizontalGlue());
        
        
        add(new JScrollPane(_editor,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), 
                BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Add a page to this browser.
     * 
     * @param anchor a user visible description to identify the page
     * @param uri the unique resource location
     */
    public void addPage(String anchor, URI url) {
        _anchors.put(anchor, url);
        ((DefaultComboBoxModel)_bookmarks.getModel()).addElement(anchor);
    }
    
    /**
     * Shows the page identified by the given anchor.
     * 
     * @param anchor an anchor added a priori.
     */
    public void showAnchor(String anchor) {
        int i = _anchors.indexOf(anchor);
        if (i == -1)
            return;
        showPage(anchor, _anchors.get(anchor));
    }
   
    /**
     * Shows the given URI.
     * @param anchor an anchor added a priori or a new one.
     * @param uri the URI of the anchor
     */
    public void showPage(String anchor, URI uri) {
        if (anchor == null || uri == null) 
            return;
        try {
            URL url = uri.toURL();
            _editor.setPage(url);
            repaint();
            _visited.add(anchor);
            _anchors.put(anchor, uri);
        } catch (Exception ex) {
            System.err.println("Anchor = " + anchor + " URI " + uri);
            ex.printStackTrace();
        }
    }
}

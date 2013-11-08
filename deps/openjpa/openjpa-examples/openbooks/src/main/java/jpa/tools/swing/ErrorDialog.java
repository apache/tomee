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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * A dialog to display runtime error.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class ErrorDialog extends JDialog {
    private static List<String> filters = Arrays.asList(
            "java.awt.", 
            "javax.swing.", 
            "sun.reflect.",
            "java.util.concurrent.");
    private static Dimension MESSAGE_SIZE    = new Dimension(600,200);
    private static Dimension STACKTRACE_SIZE = new Dimension(600,300);
    private static Dimension TOTAL_SIZE      = new Dimension(600,500);
    
    
    static String NEWLINE = "\r\n";
    static String INDENT  = "    ";
    
    private boolean      _showingDetails;
    private boolean      _isFiltering = true;
    private JComponent _message;
    private JComponent   _main;
    private JScrollPane  _details;
    private JTextPane    _stacktrace;
    private final Throwable _error;
    
    /**
     * Creates a modal dialog to display the given exception message.
     * 
     * @param t the exception to display
     */
    public ErrorDialog(Throwable t) {
        this(null, null, t);
    }

    public ErrorDialog(JComponent owner, Throwable t) {
        this(owner, null, t);
    }
    
    /**
     * Creates a modal dialog to display the given exception message.
     * 
     * @param owner if non-null, then the dialog is positioned (centered) w.r.t. this component
     * @param t the exception to display
     */
    public ErrorDialog(JComponent owner, Icon icon, Throwable t) {
        super();
        setTitle(t.getClass().getName());
        setModal(true);
        if (icon != null && icon instanceof ImageIcon) 
            setIconImage(((ImageIcon)icon).getImage());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        _error = t;
        _message = createErrorMessage(_error);
        _main    = createContent();
        getContentPane().add(_main);

        pack();
        SwingHelper.position(this, owner);
    }
    
    /**
     * Creates the display with the top-level exception message 
     * followed by a pane (that toggles) for detailed stack traces.
     *  
     * @param t a non-null exception
     */
    JComponent createContent() {
        final JButton showDetails = new JButton("Show Details >>");
        showDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_showingDetails) {
                     _main.remove(_details);
                     _main.validate();
                     _main.setPreferredSize(MESSAGE_SIZE);
                } else {
                    if (_details == null) {
                        _details = createDetailedMessage(_error);
                        StringBuilder buffer = new StringBuilder();
                        _stacktrace.setText(generateStackTrace(_error, buffer).toString());
                        _stacktrace.setBackground(_main.getBackground());
                        _stacktrace.setPreferredSize(STACKTRACE_SIZE);
                    }
                    _main.add(_details, BorderLayout.CENTER);
                    _main.validate();
                    _main.setPreferredSize(TOTAL_SIZE);
                }
                _showingDetails = !_showingDetails;
                showDetails.setText(_showingDetails ? "<< Hide Details" : "Show Details >>");
                ErrorDialog.this.pack();
            }
        });
        JPanel messagePanel = new JPanel();
      
        final JCheckBox filter = new JCheckBox("Filter stack traces");
        filter.setSelected(_isFiltering);
        filter.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                _isFiltering = filter.isSelected();
                StringBuilder buffer = new StringBuilder();
                _stacktrace.setText(generateStackTrace(_error, buffer).toString());
                _stacktrace.repaint();
            }
        });
        _message.setBackground(messagePanel.getBackground());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(showDetails);
        buttonPanel.add(filter);
        buttonPanel.add(Box.createHorizontalGlue());
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messagePanel.add(_message, BorderLayout.CENTER);
        messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        messagePanel.setPreferredSize(MESSAGE_SIZE);
        
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        main.add(messagePanel, BorderLayout.NORTH);
        return main;
    }
    
    /**
     * Creates a non-editable widget to display the error message.
     * 
     */
    JComponent createErrorMessage(Throwable t) {
        String txt = t.getLocalizedMessage();
        JEditorPane message = new JEditorPane();
        message.setContentType("text/plain");
        message.setEditable(false);
        message.setText(txt);
        return message;
    }
    
    /**
     * Creates a non-editable widget to display the detailed stack trace.
     */
    JScrollPane createDetailedMessage(Throwable t) {
        _stacktrace = new JTextPane();
        _stacktrace.setEditable(false);
        JScrollPane pane = new JScrollPane(_stacktrace, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        return pane;
    }
    
    /**
     * Recursively print the stack trace on the given buffer.
     */    
    StringBuilder generateStackTrace(Throwable t, StringBuilder buffer) {
        buffer.append(t.getClass().getName() + ": " + t.getMessage() + NEWLINE);
        buffer.append(toString(t.getStackTrace()));
        Throwable cause = t.getCause();
        if (cause !=null && cause != t) {
            generateStackTrace(cause, buffer);
        }
        return buffer;
    }
    
    StringBuilder toString(StackTraceElement[] traces) {
        StringBuilder error = new StringBuilder();
        for (StackTraceElement e : traces) {
            if (!_isFiltering || !isSuppressed(e.getClassName())) {
                String str = e.toString();
                error.append(INDENT).append(str).append(NEWLINE);
            }
        }
        return error;
    }
    
    /**
     * Affirms if the error messages from the given class name is to be suppressed.
     */
    private boolean isSuppressed(String className) {
        for (String s : filters) {
            if (className.startsWith(s))
                return true;
        }
        return false;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        String m1 = "This is test error with very very very very very long line of error message that " 
            + " should not be in a single line. Another message string that shoul dbe split across word." +
            "The quick brown fox jumpled over the lazy dog";
        String m2 = "This is another test error with very long line of error message that " 
            + " should not be in a single line";
        Throwable nested = new NumberFormatException(m2);
        Throwable top = new IllegalArgumentException(m1, nested);
        new ErrorDialog(top).setVisible(true);
    }

}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import openbook.client.Images;

/**
 * Displays and navigates PowerPoint slides.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class PowerPointViewer extends JPanel {
    private List<WeakReference<ImageIcon>> _slides = new ArrayList<WeakReference<ImageIcon>>();
    private int _total;      // Total number of slides in the deck.
    private String _dir;
    private List<URL> _slideURLs;
    private JButton _prev, _next;
    private JSpinner _goto;
    private JButton[] _navButtons;
    private int MAX_BUTTONS = 10; // Total number of navigation buttons excluding the PREVIOUS and NEXT button.
    
    // The slide
    private JLabel _view;
    
    // The key for client property in the navigation buttons denoting the 0-based index of the slides. 
    private static final String SLIDE_INDEX = "slide.index";
    
    /**
     * Create a viewer with slides in the specified directory.
     * 
     * @param dir path to a directory containing PowerPoint slides.
     * @param slides name of the slides
     */
    public PowerPointViewer(String dir, List<String> slides) {
        super(true);
        _dir = dir.endsWith("/") ? dir : dir + "/";
        _slideURLs = validateSlides(_dir, slides);
        _total  = _slideURLs.size();
        for (int i = 0; i < _total; i++) {
            _slides.add(null);
        }
        
        setLayout(new BorderLayout());
        _view = new JLabel(getSlideAt(0));
        add(new JScrollPane(_view), BorderLayout.CENTER);
        add(createNavigationButtons(), BorderLayout.SOUTH);
    }
    
    /**
     * Create buttons to navigate the slides.
     * 
     * @return a panel containing the navigation buttons.
     */
    private JPanel createNavigationButtons() {
        JPanel buttons = new JPanel();
        _navButtons = new JButton[Math.min(MAX_BUTTONS, _total)]; 
        _prev = new RoundButton(Images.BACK);
        buttons.add(_prev);
        _prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prevPage();
            }
        });
        buttons.add(Box.createHorizontalGlue());
        for (int i = 0; i < _navButtons.length; i++) {
            if (i == _navButtons.length/2) {
                JLabel gotoLabel = new JLabel("Go to ");
                _goto = new JSpinner(new SpinnerNumberModel(1,1,_total,1));
                buttons.add(gotoLabel);
                buttons.add(_goto);
                _goto.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        Integer page = (Integer)_goto.getValue();
                        showPage(page-1);
                    }
                });
            }
            int slideIndex = i + 2;
            _navButtons[i] = new JButton(String.format("%02d", slideIndex));
            buttons.add(_navButtons[i]);
            _navButtons[i].putClientProperty(SLIDE_INDEX, i+1);
            _navButtons[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton)e.getSource();
                    int index = (Integer)button.getClientProperty(SLIDE_INDEX);
                    showPage(index);
                }
            });
        }
        _next = new RoundButton(Images.NEXT);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(_next);
        _next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });
        return buttons;
        
    }
    
    /**
     * Show the next page.
     */
    private void nextPage() {
        int current = getCurrentPageIndex();
        if (current >= _total-1)
            return;
        current += 1;
        showPage(current);
    }
    
    private void prevPage() {
        int current = getCurrentPageIndex();
        if (current <= 0)
            return;
        current -= 1;
        showPage(current);
    }
    
    int getCurrentPageIndex() {
        return (Integer)_goto.getValue()-1;
    }
    
    /**
     * Shows the slide at the given index.
     * 
     * @param index 0-based index of the slides.
     */
    private void showPage(int index) {
        _view.setIcon(getSlideAt(index));
        updateButtons(index);
    }
    
    /**
     * Updates the buttons.
     * 
     * @param current 0-based index of the currently displayed slide.
     */
    private void updateButtons(int index) {
        _goto.setValue(index+1);
        
        int last  = index + _navButtons.length;
        if (last >= _total)
            return;
        
        for (int i = 0; i < _navButtons.length; i++) {
            int slideIndex = index+i+2;
            _navButtons[i].setText(String.format("%02d", slideIndex));
            _navButtons[i].putClientProperty(SLIDE_INDEX, (index+i+1));
        }
    }
    
    public int getSlideCount() {
        return _total;
    }
       
    public ImageIcon getSlideAt(int index) {
        WeakReference<ImageIcon> weak = _slides.get(index);
        if (weak == null) {
            return loadSlide(index);
        }
        return (weak.get() == null) ? loadSlide(index) : weak.get();
    }
    
    ImageIcon loadSlide(int index) {
        URL imgURL = _slideURLs.get(index);
        ImageIcon icon = new ImageIcon(imgURL);
        _slides.add(index, new WeakReference<ImageIcon>(icon));
        return icon;
    }
    
    List<URL> validateSlides(String dir, List<String> slides) {
        List<URL> valid = new ArrayList<URL>();
        for (String slide : slides) {
            URL url = findResource(dir + slide);
            if (url != null) {
                valid.add(url);
            }
        }
        return valid;
    }
    
    private URL findResource(String path) {
        if (path == null)
            return null;
        URL imgURL = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imgURL == null) {
            imgURL = getClass().getResource(path);
            if (imgURL == null) {
                System.err.println("Couldn't find file: " + path);
                return null;
            }
        }
        return imgURL;
    }
    
}

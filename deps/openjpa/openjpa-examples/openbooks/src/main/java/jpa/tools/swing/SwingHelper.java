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

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;


/**
 * Assorted utility methods.
 *  
 * @author Pinaki Poddar
 *
 */
public class SwingHelper {
    /**
     * Position the given component at the center of the given parent component or physical screen.
     * 
     * @param c the component to be positioned
     * @param parent the component whose center will match the center of the given component.
     * If null, the given component will match the screen center.
     * 
     */
    public static void position(Component c, Component parent) {
        Dimension d = c.getPreferredSize();
        if (parent == null) {
            Dimension s =  Toolkit.getDefaultToolkit().getScreenSize();
            c.setLocation(s.width/2 - d.width/2,  s.height/2 - d.height/2);
        } else {
            Point p = parent.getLocationOnScreen();
            int pw = parent.getWidth();
            int ph = parent.getHeight();
            c.setLocation(p.x + pw/2 - d.width/2, p.y + ph/2 - d.height/2);
        }
    }
    
    private static int[] factors = {1000, 1000, 1000, 1000, 60, 60, 24};
    
    public static String getTimeString(long value, TimeUnit unit) {
        if (value <= 0)
            return "";
        int i = unit.ordinal();
        TimeUnit[] units = TimeUnit.values();
        TimeUnit next = null;
        int factor = -1;
        if (i < factors.length -1) {
            next = units[i+1];
            factor = factors[i+1];
            long nextValue = value/factor;
            if (nextValue > 0) 
                return getTimeString(value/factor, next) + " " + getTimeString(value%factor, unit); 
        } 
        
        return value + toString(unit);
    }
    
    public static String toString(TimeUnit unit) {
        switch (unit) {
        case HOURS:
        case DAYS:
        case MINUTES:
        case SECONDS:
            return unit.toString().substring(0,1).toLowerCase();
        case MILLISECONDS:
            return "ms";
        case MICROSECONDS:
            return "micros";
        case NANOSECONDS:
            return "ns";
        }
        return "";
    }
    
    public static void print(Component c, String format, File output) {
        try {
            Robot robot = new Robot();
            Point root = c.getLocationOnScreen();
            BufferedImage shot = robot.createScreenCapture(new Rectangle(root.x, root.y, c.getWidth(), c.getHeight()));
            ImageIO.write(shot, format, output);
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static AbstractButton getSelectedButton(ButtonGroup group) {
        Enumeration<AbstractButton> buttons = group.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton b = buttons.nextElement();
            if (b.isSelected()) {
                return b;
            }
        }
        return null;
    }
    
    public static void setLookAndFeel(int fontSize) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            if ((key instanceof String) && (((String) key).endsWith(".font"))) {
                FontUIResource font = (FontUIResource) UIManager.get(key);
                defaults.put (key, new FontUIResource(font.getFontName(), font.getStyle(), fontSize));
            }
        }
     }
    


}

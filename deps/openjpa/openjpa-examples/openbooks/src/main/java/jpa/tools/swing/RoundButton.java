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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A Circular Swing Button.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class RoundButton extends JButton {
    protected Shape shape, base;
    
    public RoundButton() {
        this(null, null);
    }
    
    public RoundButton(Icon icon) {
        this(null, icon);
    }
    
    public RoundButton(String text) {
        this(text, null);
    }
    
    public RoundButton(Action a) {
        this();
        setAction(a);
    }
    
    public RoundButton(String text, Icon icon) {
        setModel(new DefaultButtonModel());
        init(text, icon);
        if(icon==null) {
            return;
        }
        int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
        int sw = 1;
        setBorder(BorderFactory.createEmptyBorder(sw,sw,sw,sw));
        Dimension dim = new Dimension(iw+sw+sw, iw+sw+sw);
        setPreferredSize(dim);
        setMaximumSize(dim);
        setMinimumSize(dim);
        setBackground(Color.BLACK);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setAlignmentY(Component.TOP_ALIGNMENT);
        initShape();
    }
    
    protected void initShape() {
        if(!getBounds().equals(base)) {
            Dimension s = getPreferredSize();
            base = getBounds();
            shape = new Ellipse2D.Float(0, 0, s.width-1, s.height-1);
        }
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        initShape();
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.draw(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    @Override
    public boolean contains(int x, int y) {
        initShape();
        return shape.contains(x, y);
    }
}

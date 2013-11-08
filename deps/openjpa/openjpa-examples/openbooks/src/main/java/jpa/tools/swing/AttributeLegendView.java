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
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.persistence.metamodel.Attribute;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays color codes of each attribute type.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class AttributeLegendView extends JPanel {
    
    public AttributeLegendView() {
        super(true);
        setBorder(BorderFactory.createTitledBorder("Attribute Legends"));
        setLayout(new GridLayout(0,3));
        add(createColoredLabel("IDENTITY", Color.RED));
        add(createColoredLabel("VERSION", Color.DARK_GRAY));
        for (Attribute.PersistentAttributeType type : Attribute.PersistentAttributeType.values()) {
          add(createColoredLabel(type.toString().replace('_', ' '), MetamodelHelper.getColor(type)));
      }        
    }
    
    JComponent createColoredLabel(String text, Color c) {
        int width  = 40;
        int height = 20;
        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                bimage.setRGB(i, j, c.getRGB());
        JLabel label = new JLabel(text, new ImageIcon(bimage), JLabel.LEADING);
        return label;
    }

}

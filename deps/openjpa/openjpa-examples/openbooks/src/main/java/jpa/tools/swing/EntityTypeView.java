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
import java.awt.GridLayout;
import java.awt.Point;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * View of a persistent entity type as a JPanel.
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class EntityTypeView<T> extends JPanel {
    final EntityType<T> type;
    final int hgap = 4;
    final int vgap = 4;
    
    public EntityTypeView(EntityType<T> type) {
        this.type = type;
        GridLayout layout = new GridLayout(0,1);
        setLayout(layout);
        layout.setVgap(vgap);
        layout.setHgap(hgap);
        
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(MetamodelHelper.getDisplayName(type)));

        int w = 0;
        int h = 0;
        List<Attribute<? super T,?>> attrs = MetamodelHelper.getAttributes(type);
        for (Attribute<? super T,?> attr : attrs) {
            JComponent c = new AttributeView(attr);
            add(c);
            w  = Math.max(w, c.getPreferredSize().width);
            h += c.getPreferredSize().height + 2*vgap;
        }
//        setPreferredSize(new Dimension(w,h));
    }
    
    public EntityType<T> getEntityType() {
        return type;
    }
    
    
    /**
     * Gets the top left position of the attribute label relative to this entity view.
     * @param attr
     * @return
     */
    public Point getPosition(Attribute<?,?> attr) {
        for (Component c : getComponents()) {
            if (c instanceof AttributeView) {
                if (((AttributeView)c).getAttribute().equals(attr)) {
                    return new Point(c.getLocation().x, c.getLocation().y + c.getHeight()/2);
                }
            }
        }
        return new Point(0,0);
    }
}



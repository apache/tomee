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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.swing.JPanel;


/**
 * Graphical View of a JPA 2.0 Metamodel.
 * The view is isomorphic to the {@link Metamodel meta-model} defined by JPA 2.0 specification.
 * Hence the view is organized in terms of corresponding views of {@link EntityTypeView entity}
 * and their {@link AttributeView attributes}.
 * <br>
 * This view also draws linkage with A*-algorithm between the derived primary key attributes that 
 * reference other entity types.   
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class MetamodelView extends JPanel implements Maze {
    
    private static final int GRID = 8;
    int hgap = 40;
    int vgap = 40;
    
    /**
     * Creates a panel where each {@link EntityTypeView} is placed in a FlowLayout.
     * Only the entities of the model are ordered based on their primary key 
     * dependencies, if any.
     * 
     * @param model
     */
    @SuppressWarnings("unchecked")
    public MetamodelView(Metamodel model) {
        super(true);
        FlowLayout layout = (FlowLayout)getLayout();
        layout.setHgap(80);
        layout.setVgap(80);
//        getInsets(new Insets(100,100,100,100));
        Set<EntityType<?>> types = model.getEntities();
        List<EntityType<?>> sorted = new ArrayList<EntityType<?>>(types);
        Collections.sort(sorted, new MetamodelHelper.EntityComparator());
        for (EntityType type : sorted) {
            EntityTypeView view = new EntityTypeView(type);
            add(view);
        }
    }
    
    EntityTypeView<?> findViewByType(EntityType<?> type) {
        if (type == null)
            return null;
        for (Component c : getComponents()) {
            if (c instanceof EntityTypeView) {
                EntityTypeView<?> view = (EntityTypeView<?>)c;
                if (view.getEntityType().equals(type)) {
                    return view;
                }
            }
        }
        return null;
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Component c : getComponents()) {
            if (c instanceof EntityTypeView == false) {
                continue;
            }
            EntityTypeView<?> view = (EntityTypeView<?>)c;
            for (SingularAttribute<?,?> id : MetamodelHelper.getIdAttributes(view.getEntityType())) {
                EntityTypeView<?> target = findViewByType(MetamodelHelper.getParentType(id));
                if (target == null)
                    continue;
                
                PathFinder runner = new PathFinder(this);
                Point start = getConnectLocation(view, id);
                Point finish = target.getLocation();
                List<Point> path = runner.findPath(start.x/GRID, start.y/GRID, 
                        finish.x/GRID, finish.y/GRID);
                if (path.isEmpty())
                    continue;
                Point p1 = path.get(0);
                Point p2 = null;
                for (int i = 1; i < path.size(); i++) {
                    p2 = path.get(i);
                    g.drawLine(p1.x*GRID, p1.y*GRID, p2.x*GRID, p2.y*GRID);
                    p1 = p2;
                }
                g.setColor(Color.BLACK);
                int r = 4;
                g.fillOval(p1.x*GRID -r, p1.y*GRID - r, 2*r, 2*r);
                
            }
        }
    }
    
    /**
     * Gets the position of the attribute in the entity view relative to this panel. 
     * @param a
     * @param attr
     * @return
     */
    Point getConnectLocation(EntityTypeView<?> a, Attribute<?,?> attr) {
        Point p1 = a.getLocation();
        Point p2 = a.getPosition(attr);
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }
    
    // contract for the maze
    @Override
    public boolean isReachable(int x, int y) {
        for (Component view : getComponents()) {
            Rectangle r = view.getBounds();
            int px = x*GRID;
            int py = y*GRID;
            if (r.contains(px, py)) 
                return false;
        }
        return true;
    }
}

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jpa.tools.swing.EntityTable.InstanceCellRenderer;


/**
 * An entity table view consists of a JTable and optionally another table for many-valued associations.
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class EntityTableView<T> extends JPanel implements ListSelectionListener {
    private EntityTable<T> _table;
    private JTextArea _details;
    
    public EntityTableView(Class<T> cls, int styleBits, EntityManagerFactory unit) {
        this(cls, (List<T>)Collections.EMPTY_LIST, styleBits, unit);
    }
    
    public EntityTableView(Class<T> cls, List<T> data, int styleBits, EntityManagerFactory unit) {
        super(true);
        _table   = new EntityTable<T>(cls, data, styleBits, unit);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        if ((styleBits & EntityDataModel.PLURAL_ATTR) != 0) {
            _table.getSelectionModel().addListSelectionListener(this);
            _details = new JTextArea("Click many-valued columns for display");
            _details.setForeground(Color.LIGHT_GRAY);
            _details.setBorder(BorderFactory.createTitledBorder("Many-valued association"));
            _details.setEditable(false);
        }
        setBorder(BorderFactory.createTitledBorder(_table.getModel().getRowCount() + " " +
                unit.getMetamodel().entity(cls).getName()));
        
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(new JScrollPane(_table, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
        if (_details != null)
            add(_details);
    }
    
    public EntityTable<T> getTable() {
        return _table;
    }
    
    public EntityDataModel<T> getDataModel() {
        return (EntityDataModel<T>)_table.getModel();
    }
    
    public void updateTitle(String txt) {
        Border border = getBorder();
        if (border instanceof TitledBorder) {
            ((TitledBorder)border).setTitle(txt);
            repaint();
        }
    }
    /**
     * Notified when a cell in the table is selected.
     * If the selected cell is a many-valued attribute then displays its details in the details pane.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        int row = _table.getSelectedRow();
        if (row == -1)
            return;
        int col = _table.getSelectedColumn();
        if (col == -1)
            return;
        EntityDataModel<?> model = (EntityDataModel<?>)_table.getModel();
        Attribute<?,?> attr = model.getAttribute(col);
        if (attr.isCollection()) {
            Object val = model.getValueAt(row, col);
            showDetails(attr, val);
        }
    }
    
    private void showDetails(Attribute<?,?> attr, Object val) {
        _details.setText(null);
        ManagedType<?> owner = attr.getDeclaringType();
        String title = (owner instanceof EntityType) 
                     ? ((EntityType<?>)owner).getName() + "." + attr.getName()
                     : owner.getJavaType().getSimpleName() + "." + attr.getName(); 
        TitledBorder border = (TitledBorder)_details.getBorder();
        
        if (val == null) {
            border.setTitle(title + " (null)");
        } else {
            Collection<?> coll = (Collection<?>)val;
            if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                border.setTitle(title + " (" + coll.size() + " values)");
                _details.setForeground(Color.BLACK);
                for (Object e : coll) {
                    _details.append(_table.getInstanceRenderer().renderAsString(e) + "\r\n");
                    _details.append(e + "\r\n");
                }
            } else {
                border.setTitle(title + " (" + coll.size() + " instances)");
                _details.setForeground(Color.BLUE);
                InstanceCellRenderer renderer = _table.getInstanceRenderer();
                for (Object e : coll) {
                    _details.append(renderer.renderAsString(e) + "\r\n");
                }
            }
        }
    }
}

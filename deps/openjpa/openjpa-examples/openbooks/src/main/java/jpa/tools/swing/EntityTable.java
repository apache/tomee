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
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.Attribute;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;


/**
 * A specialized Swing table to display a list of persistent entity instances.
 * The data for the table is supplied via a specialized {@linkplain EntityDataModel data model}
 * that is supplied at construction.
 * <br>
 * The table view uses specialized cell renderer to display single-valued and multi-valued
 * association.  
 * 
 * 
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class EntityTable<T> extends JTable {
    private InstanceCellRenderer instanceCellRenderer;
    private CollectionCellRenderer collectionCellRenderer;
    
    public EntityTable(Class<T> cls, List<T> data, int styleBits, EntityManagerFactory unit) {
        super();
        instanceCellRenderer   = new InstanceCellRenderer(unit.getPersistenceUnitUtil());
        collectionCellRenderer = new CollectionCellRenderer();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setModel(new EntityDataModel<T>(cls, data,  unit.getMetamodel(), styleBits));
        getModel().addTableModelListener(this);
        initColumnSizes();
        setFillsViewportHeight(true);
    }
    
    
    public InstanceCellRenderer getInstanceRenderer() {
        return instanceCellRenderer;
    }
    
    /**
     * Gets the special renderer for single- and multi-valued association.
     * Otherwise uses the super classes' renderer defined by the field type.    
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        Attribute<?,?> attr = ((EntityDataModel)getModel()).getAttribute(column);
        TableCellRenderer renderer = null;
        if (attr == null) {
            renderer = super.getCellRenderer(row, column);
        } else if (attr.isAssociation()) {
            renderer = instanceCellRenderer;
        } else if (attr.isCollection()) {
            renderer = collectionCellRenderer;
        } else {
            renderer = super.getCellRenderer(row, column);
        }
        if (renderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer)renderer).setHorizontalAlignment(JLabel.CENTER);
        }
        
        return renderer;
    }
    
    public TableCellEditor getCellEditor(int row, int column) {
        Attribute<?,?> attr = ((EntityDataModel)getModel()).getAttribute(column);
        if (attr == null)
            return super.getCellEditor(row, column);
        if (attr.isCollection())
            return new DefaultCellEditor((JComboBox)getCellRenderer(row, column));
        return super.getCellEditor(row, column);
    }
    
    /**
     * Picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes() {
        TableModel model   = getModel();
        TableColumn column = null;
        Component comp     = null;
        int headerWidth    = 0;
        
        TableCellRenderer headerRenderer =  getTableHeader().getDefaultRenderer();

        for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
            int cellWidth = 0;
            column = getColumnModel().getColumn(columnIndex);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            TableCellRenderer renderer = getCellRenderer(0, columnIndex);
            int rowCount = Math.min(model.getRowCount(), 10);
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                Object value = model.getValueAt(rowIndex, columnIndex); 
                comp = renderer.getTableCellRendererComponent(
                                 this, value,
                                 false, false, rowIndex, columnIndex);
                cellWidth = Math.max(comp.getPreferredSize().width, cellWidth);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    /**
     * Renders the value of a persistent entity in a table column as the persistent identifier.
     * The persistent identifier is extracted by the new {@link PersistenceUnitUtil utility} feature
     * of JPA 2.0 API.
     * 
     * @author Pinaki Poddar
     *
     */
    public class InstanceCellRenderer extends DefaultTableCellRenderer {
        private final PersistenceUnitUtil util;
        
        public InstanceCellRenderer(PersistenceUnitUtil util) {
            super();
            this.util = util;
        }
        
        /**
         * Gets the stringified persistence identifier of the given instance.
         * 
         */
        public String renderAsString(Object instance) {
            if (instance == null) {
                return "null";
            }
            Object id = util.getIdentifier(instance);
            if (id != null)
                return id.toString();
            if (instance instanceof PersistenceCapable) {
                PersistenceCapable pc = (PersistenceCapable)instance;
                StateManager sm = pc.pcGetStateManager();
                id = sm == null ? null : sm.fetchObjectId();
            }
            return id == null ? "null" : id.toString();
        }

        public void setValue(Object instance) {
            setForeground(Color.BLUE);
            setText(renderAsString(instance));
        }

    }

    
    /**
     * Renders a many-valued attribute as simply three dots.
     * 
     * @author Pinaki Poddar
     *
     */
    public class CollectionCellRenderer extends DefaultTableCellRenderer {
        public CollectionCellRenderer() {
            setPreferredSize(new Dimension(10,20));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table, value == null ? null : "...", 
                    isSelected, hasFocus, row, column);
        }

    }



    
}

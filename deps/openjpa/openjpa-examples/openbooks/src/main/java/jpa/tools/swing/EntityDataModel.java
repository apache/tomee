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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A data model for a tabular view of a list of persistent entities.
 * The data supplied by this model can be filtered to display field values of
 * basic type or single-valued or multi-valued relationships.  
 * <br>
 * The meta-information about the attributes of the entity are supplied by 
 * newly defined {@link Metamodel meta-model API} of JPA 2.0 specification.
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class EntityDataModel<T> extends AbstractTableModel implements TableModel, Iterable<T> {
    /**
     * Constant designates to include non-relation fields.
     */
    public static int BASIC_ATTR       = 1 << 0;
    /**
     * Constant designates to include single-valued relation fields.
     */
    public static int ASSOCIATION_ATTR = 1 << 1;
    /**
     * Constant designates to include multi-valued relation fields.
     */
    public static int PLURAL_ATTR      = 1 << 2;
    /**
     * Constant designates to include all fields.
     */
    public static int ALL_ATTR         = BASIC_ATTR | ASSOCIATION_ATTR | PLURAL_ATTR;
    /**
     * Constant designates to show a row count field at the first column.
     */
    public static int ROW_COUNT        = 1 << 3;

    private List<String>   columnNames;
    private List<Class<?>> columnClasses;
    private List<Method>   methods;
    private List<T>        data;
    private List<Attribute<? super T,?>> attributes; 
    private static Object[] EMPTY_ARGS = null;
    private static Class<?>[] EMPTY_CLASSES = null;
    
    private boolean showsRowCount;
    private boolean showsBasicAttr;
    private boolean showsSingularAttr;
    private boolean showsPluralAttr;
    
    /**
     * Attributes of the entity are reordered with basic attributes, followed by singular
     * association followed by the many-valued attributes.
     *  
     * @param cls
     * @param data
     * @param meta
     */
    public EntityDataModel(Class<T> cls, List<T> data, Metamodel meta, int styleBits) {
        super();
        this.data = data;
        EntityType<T> entityType = meta.entity(cls);
        
        columnNames   = new ArrayList<String>();
        columnClasses = new ArrayList<Class<?>>();
        attributes    = new ArrayList<Attribute<? super T,?>>();
        methods       = new ArrayList<Method>();
        
        showsRowCount     = (styleBits & ROW_COUNT) != 0;
        showsBasicAttr    = (styleBits & BASIC_ATTR) != 0;
        showsSingularAttr = (styleBits & ASSOCIATION_ATTR) != 0;
        showsPluralAttr   = (styleBits & PLURAL_ATTR) != 0;
        
        Set<SingularAttribute<? super T, ?>> sAttrs = entityType.getSingularAttributes();
        if (showsBasicAttr) {
            for (SingularAttribute<? super T, ?> attr : sAttrs) {
                if (!attr.isAssociation()) {
                    attributes.add(attr);
                }
            }
        }
        if (showsSingularAttr) {
            for (SingularAttribute<? super T, ?> attr : sAttrs) {
                if (attr.isAssociation()) {
                    attributes.add(attr);
                }
            }
        }
        if (showsPluralAttr) {
            Set<PluralAttribute<? super T, ?, ?>> pAttrs = entityType.getPluralAttributes();
            for (PluralAttribute<? super T, ?, ?> attr : pAttrs) {
                attributes.add(attr);
            }
        }
        Collections.sort(attributes, new MetamodelHelper.AttributeComparator());
        for (Attribute<? super T, ?> attr : attributes) {
            populate(cls, attr);
        }
        if (showsRowCount) {
            columnNames.add(0,"Row");
            columnClasses.add(0, Long.class);
            attributes.add(0, null);
            methods.add(0, null);
        }
    }
    
    private void populate(Class<T> cls, Attribute<?,?> attr) {
        columnNames.add(attr.getName());
        columnClasses.add(wrap(attr.getJavaType()));
        methods.add(getMethod(cls, attr.getName()));
    }

    /**
     * Gets the attribute at a given column index.
     * Can be null for 0-th index if row count is being shown. 
     */
    public Attribute<?,?> getAttribute(int columnIndex) { 
        return attributes.get(columnIndex);
    }
    
    /**
     * Gets the entity represented in the given row.
     */
    public T getRow(int row) {
        return data.get(row);
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses.get(columnIndex);
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Method method = methods.get(columnIndex);
        if (method == null) {
            return columnIndex == 0 && showsRowCount ? rowIndex+1 :  "?";
        }
        Object row = data.get(rowIndex);
        Object val = getValueByReflection(rowIndex, row, columnIndex, method);
        return val; 
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // should never be called
    }

    /**
     * Gets the value by reflection.
     * @param o
     * @param p
     * @return
     */
    Object getValueByReflection(int rowIndex, Object o, int columnIndex, Method m) {
        if (o == null) {
            System.err.println("Row " + rowIndex + " is null");
            return null;
        }
        if (m == null) {
            System.err.println("Column " + columnIndex + ":" + getColumnName(columnIndex) + " method is null");
            return null;
        }
        try {
            return m.invoke(o, EMPTY_ARGS);
        } catch (Exception e) {
            System.err.println("Can not extract value at [" + rowIndex + "," + columnIndex + "] due to " + e);
            e.printStackTrace();
        }
        return null;
    }
    
    Class<?> wrap(Class<?> c) {
        if (c == null || c.isInterface() || c.isArray())
            return Object.class;
        if (c.isPrimitive()) {
            if (c == boolean.class) return Boolean.class;
            if (c == byte.class) return Byte.class;
            if (c == char.class) return Character.class;
            if (c == double.class) return Double.class;
            if (c == float.class) return Float.class;
            if (c == int.class) return Integer.class;
            if (c == long.class) return Long.class;
            if (c == short.class) return Short.class;
        }
        return c;
    }
    
    private Method getMethod(Class<T> type, String p) {
        try {
            String getter = "get" + Character.toUpperCase(p.charAt(0))+p.substring(1);
            return type.getMethod(getter, EMPTY_CLASSES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }
    
    public void updateData(List<T> newData) {
        data = newData;
        fireTableDataChanged();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
    
}

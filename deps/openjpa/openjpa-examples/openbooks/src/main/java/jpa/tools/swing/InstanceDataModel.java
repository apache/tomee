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

import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class InstanceDataModel<T> extends AbstractTableModel {
    List<Attribute<? super T, ?>> attributes;
    private T _instance;

    public InstanceDataModel(EntityType<T> type, T instance) {
        super();
        attributes = MetamodelHelper.getAttributes(type);
        this._instance = instance;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Object.class;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Properties" : "Value";
    }

    @Override
    public int getRowCount() {
        return attributes.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Attribute<? super T, ?> attr = attributes.get(rowIndex);
        return columnIndex == 0 ?  attr.getName() : MetamodelHelper.getValue(attr, _instance);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

}

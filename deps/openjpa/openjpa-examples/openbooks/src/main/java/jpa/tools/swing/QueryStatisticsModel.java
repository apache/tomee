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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.openjpa.kernel.QueryStatistics;

@SuppressWarnings("serial")
class QueryStatisticsModel extends AbstractTableModel {
    QueryStatistics<String> _stats;
    private List<String> _keys = new ArrayList<String>();
    
    QueryStatisticsModel(QueryStatistics<String> stats) {
        _stats = stats;
        sortKeys(stats);
    }
    
    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        sortKeys(_stats);
        return _keys.size();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case 0: return "Total";
        case 1: return "Hit";
        case 2: return "Query";
        default : return "null";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String query = _keys.get(rowIndex);
        switch (columnIndex) {
        case 0: return _stats.getExecutionCount(query);
        case 1: return _stats.getHitCount(query);
        case 2: return query;
        default : return null;
        }
    }
    
    void sortKeys(QueryStatistics<String> stats) {
        if (_stats.keys().size() != _keys.size()) {
            _keys = new ArrayList<String>(_stats.keys());
            if (_keys.size() > 1) {
                Collections.sort(_keys);
            }
            fireTableDataChanged();
        }
    }

}

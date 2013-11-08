/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.trader.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A composite widget combines a data table, a table header, a scrollbar, 
 * and a caption and a {@link HelpLink help anchor}. 
 * <br>
 * Each row of the data table displays an instance of type T. How the instance
 * is displayed in controlled by a set of {@link GridCellRenderer renderers}
 * attached to each column.
 * <br>
 * The styles used
 * <LI>table-caption : for the caption
 * <LI>column-header : for the column headers
 * <LI>row-odd       : for odd numbered rows
 * <LI>row-even      : for even numbered rows
 * 
 * 
 * @author Pinaki Poddar
 *
 * @param <T> the type of data being displayed.
 */
public class ScrollableTable<T> extends Composite  {
    private FlexTable _main;
    private Grid _caption;
    private FlexTable _header;
    private ScrollPanel _scroll;
    private List<GridCellRenderer<T>> _renderers;
    private List<T> _rows;
    private boolean _stripeRows;
    
    private static final String STYLE_CAPTION = "table-caption";
    private static final String STYLE_HEADER  = "column-header";
    private static final String STYLE_MAIN    = "table";
    private static final String ROW_EVEN      = "row-even";
    private static final String ROW_ODD       = "row-odd";
    
    /**
     * Create a scrollable table with the given caption and given pixel dimension.
     * The table will not be backed by a data  storage model.
     * 
     * @param caption of the table
     * @param w width in pixel
     * @param h height in pixel
     */
    public ScrollableTable(String caption, int w, int h) {
        this(caption, w+"px", h+"px", false);
    }
    
    /**
     * Create a scrollable table with the given caption and given pixel dimension.
     * The table will not be backed by a data  storage model.
     * 
     * @param caption of the table
     * @param w width in pixel
     * @param h height in pixel
     * @param updatable whether the table data will be backed by a storage such
     * that row can be updated rather than always inserted
     */
    public ScrollableTable(String caption, int w, int h, boolean updatable) {
        this(caption, w+"px", h+"px", updatable);
    }
    
    /**
     * Create a scrollable table with the given caption and given dimension.
     * 
     * @param caption of the table
     * @param w width in given unit
     * @param h height in given unit
     * @param updatable whether the table data will be backed by a storage such
     * that row can be updated rather than always inserted
     */
    public ScrollableTable(String caption, String w, String h, boolean updatable) {
        super();
        _renderers = new ArrayList<GridCellRenderer<T>>();
        
        VerticalPanel vert = new VerticalPanel();
        vert.setSpacing(0);
        
        _caption  = new Grid(1,2);
        _caption.setWidth("100%");
        setCaption(caption);
        vert.add(_caption);
        
        
        _header = new FlexTable();
        _header.addStyleName(STYLE_HEADER);
        _header.setWidth("100%");
        
        _main = new FlexTable();
        _main.addStyleName(STYLE_MAIN);
        
        _main.setWidth("100%");
        _main.setBorderWidth(0);
        _scroll = new ScrollPanel();
        _scroll.setSize(w, h);
        _scroll.setAlwaysShowScrollBars(true);
        _scroll.add(_main);
        
        
        vert.add(_header);
        vert.add(_scroll);
        if (updatable) {
            _rows = new ArrayList<T>();
        }
        
        initWidget(vert);
    }
    
    @SuppressWarnings("unchecked")
    public List<T> getModel() {
        return _rows == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(_rows);
    }
    
    public void setCaption(String str) {
        HTML caption = new HTML(str);
        caption.addStyleName(STYLE_CAPTION);
        _caption.setWidget(0, 0, caption);
    }
    
    public void setSize(int w, int h) {
        int dh = 0;
        if (_caption != null) {
           dh += _caption.getOffsetHeight();   
        }
        dh += _header.getOffsetHeight();
        _scroll.setPixelSize(w, h-dh);
    }
    
    
    public void setRenderer(int column, GridCellRenderer<T> r) {
        if (column > _renderers.size()) {
            for (int i = _renderers.size(); i <= column; i++) {
                _renderers.add(null);
            }
            _renderers.add(r);
        } else if (column == _renderers.size()) {
            _renderers.add(r);
        } else {
            _renderers.set(column, r);
        }
    }
    
    public void setColumnHeader(int column, String txt, String width) {
        HTML header = new HTML(txt);
        header.addStyleName(STYLE_HEADER);
        _header.setWidget(0, column, header);
        _main.getColumnFormatter().setWidth(column, width);
    }
    
    public void setStripeRows(boolean stripe) {
        _stripeRows = stripe;
    }
    
    public boolean isStripeRows() {
        return _stripeRows;
    }
     
    public void insert(T data) {
        setRow(_main.getRowCount(), data, null);
    }
    
    public void remove(T data) {
        int i = findRow(data);
        if (i != -1) {
            _main.removeRow(i);
            _rows.remove(i);
        }
    }
    
    /**
     * Update entire row.
     */
    public void update(T data, Integer[] columns) {
        int i = findRow(data);
        if (i != -1) {
            setRow(i, data, columns);
        } else {
            insert(data);
        }
    }
    
    public void updateCell(int row, int column, Widget widget, boolean animate) {
        if (animate) {
            FadeEffect fadeOut = new FadeEffect(_main.getWidget(row, column), false);
            fadeOut.scheduleRepeating(10);
            DOM.setElementAttribute(widget.getElement(), "opacity", "0.0");
            _main.setWidget(row, column, widget);
            FadeEffect fadeIn = new FadeEffect(_main.getWidget(row, column), true);
            fadeIn.scheduleRepeating(10);
        } else {
            _main.setWidget(row, column, widget);
        }
    }
    
    public int getRowCount() {
        return _main.getRowCount();
    }
    
    /**
     * Sets the cells of an existing row.
     * Calls each renderer. 
     * @param row
     * @param data
     */
    private void setRow(int row, T data, Integer[] columns) {
        if (_rows != null) {
            if (row < 0 || row >= _rows.size())
                _rows.add(data);
            else 
                _rows.set(row, data);
        }
        for (int i = 0; i < _renderers.size(); i++) {
            GridCellRenderer<T> r = _renderers.get(i);
            if (r == null)
                continue;
            if (containsColumn(columns, i)) {
                Widget widget = r.render(data);
                if (widget != null)
                    _main.setWidget(row, i, widget);
            }
        }
        if (isStripeRows()) {
            _main.getRowFormatter().setStylePrimaryName(row, row%2 == 0? ROW_EVEN : ROW_ODD);
        }
        _scroll.scrollToBottom();
    }
    
    public int findRow(T data) {
        
        if (_rows == null || data == null)
            return -1;
        return _rows.indexOf(data);
    }
    
    private boolean containsColumn(Integer[] columns, int i) {
        if (columns == null)
            return true;
        for (int j = 0; j < columns.length; j++) {
            if (columns[j] == i)
                return true;
        }
        return false;
    }
    
    public T get(int i) {
        if (_rows == null || i < 0 || i >= _rows.size())
            return null;
        return _rows.get(i);
    }
    
    public void scrollToBottom() {
        _scroll.scrollToBottom();
    }
    
    public void addHelp(final String url) {
        if (_caption == null) {
            return;
        }
        _caption.getColumnFormatter().setWidth(0, "95%");
        HelpLink help = new HelpLink(url);
        _caption.setWidget(0, 1, help);
        _caption.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    }
    
    
}

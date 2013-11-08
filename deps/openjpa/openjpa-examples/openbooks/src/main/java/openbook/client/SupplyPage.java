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
package openbook.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import jpa.tools.swing.EntityDataModel;
import jpa.tools.swing.EntityTableView;
import jpa.tools.swing.ErrorDialog;
import openbook.domain.Book;
import openbook.domain.Inventory;
import openbook.server.OpenBookService;

/**
 * A page to view and supply low inventory items.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public class SupplyPage extends JPanel  {
    private final OpenBookService _service;

    private final EntityTableView<Inventory> _lowInventories;
    
    private final JButton _supply;
    private final JButton _view;
    private final JLabel  _title;
    
    private static int REORDER_LIMIT    = 10;
    private static int REORDER_QUANTITY = 40;

    public SupplyPage(final OpenBookService service) {
        setLayout(new BorderLayout());
        
        _service = service;
        
        _title  = new JLabel(REORDER_LIMIT + " lowest inventory items");
        _view   = new JButton("Show " + REORDER_LIMIT + " lowest inventory items");
        _supply = new JButton("Supply " + REORDER_QUANTITY + " to each item");
        
        List<Inventory> orders = getInventory(REORDER_LIMIT);
        _lowInventories = new EntityTableView<Inventory>(Inventory.class, 
                orders, 
                EntityDataModel.BASIC_ATTR | EntityDataModel.ASSOCIATION_ATTR, 
                service.getUnit());
        
        add(_title, BorderLayout.NORTH);
        add(_lowInventories, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        buttons.add(_view);
        buttons.add(_supply);
        add(buttons, BorderLayout.SOUTH);
        
        
        _view.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _lowInventories.getDataModel().updateData(getInventory(REORDER_LIMIT));
            }
        });
        
        /**
         * Supplies each inventory displayed.
         */
        _supply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SwingWorker<List<Inventory>, Void>() {
                    @Override
                    protected List<Inventory> doInBackground() throws Exception {
                        EntityDataModel<Inventory> invs = _lowInventories.getDataModel();
                        List<Inventory> updated = new ArrayList<Inventory>();
                        for (Inventory inv : invs) {
                            Book supplied = _service.supply(inv.getBook(), REORDER_QUANTITY);
                            updated.add(supplied.getInventory());
                        }
                        return updated;
                    }
                    
                    public void done() {
                        try {
                            _lowInventories.getDataModel().updateData(get(1, TimeUnit.SECONDS));
                        } catch (Exception e) {
                            new ErrorDialog(e).setVisible(true);
                        }
                    }
                }.execute();
                
            }
            
        });
    }
    
    /**
     * Gets the orders in a background (i.e. not AWT event dispatch thread) thread.
     * <br>
     * But blocks painting anyway, because that is what is intended.
     * 
     */
    private List<Inventory> getInventory(final Integer limit) {
        SwingWorker<List<Inventory>, Void> worker = new SwingWorker<List<Inventory>, Void>() {
            @Override
            protected List<Inventory> doInBackground() throws Exception {
                return _service.getReorderableBooks(REORDER_LIMIT);         
            }
        };
        worker.execute();
        try {
            return worker.get();
        } catch (Exception e) {
            new ErrorDialog(e).setVisible(true);
        }
        return Collections.emptyList();
    }
}

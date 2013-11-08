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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;

import jpa.tools.swing.EntityDataModel;
import jpa.tools.swing.EntityTableView;
import jpa.tools.swing.ErrorDialog;
import openbook.domain.PurchaseOrder;
import openbook.server.OpenBookService;

/**
 * A page to control delivery of pending orders.
 * 
 * @author Pinaki Poddar
 * 
 */
@SuppressWarnings("serial")
public class DeliveryPage extends JPanel {
    private final JButton _deliver;
    private EntityTableView<PurchaseOrder> _orders;
    private final OpenBookService _service;
    private final JLabel _title;
    private final JRadioButton _showPending;
    private final JRadioButton _showDelivered;
    private final JRadioButton _showBoth;

    public DeliveryPage(final OpenBookService service) {
        setLayout(new BorderLayout());

        _service = service;

        _orders = new EntityTableView<PurchaseOrder>(PurchaseOrder.class, getOrders(PurchaseOrder.Status.PENDING),
                EntityDataModel.ALL_ATTR, service.getUnit());

        _title = new JLabel(_orders.getDataModel().getRowCount() + " " + PurchaseOrder.Status.PENDING
                + " PurchaseOrder");

        _deliver = new JButton("Deliver Pending Orders");

        JPanel statusPanel = new JPanel();
        ButtonGroup statusSelection = new ButtonGroup();
        _showPending = new JRadioButton("Pending");
        _showDelivered = new JRadioButton("Delivered");
        _showBoth = new JRadioButton("All");
        _showPending.setSelected(true);

        statusSelection.add(_showPending);
        statusSelection.add(_showDelivered);
        statusSelection.add(_showBoth);
        statusPanel.add(_showBoth);
        statusPanel.add(_showPending);
        statusPanel.add(_showDelivered);

        JPanel actionPanel = new JPanel();
        actionPanel.add(_deliver);

        add(statusPanel, BorderLayout.NORTH);
        add(_orders, BorderLayout.CENTER);

        add(actionPanel, BorderLayout.SOUTH);

        _showBoth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<PurchaseOrder> selected = getOrders(null);
                _orders.getDataModel().updateData(selected);
                _title.setText(selected.size() + " PurchaseOrder");
            }
        });
        _showPending.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<PurchaseOrder> selected = getOrders(PurchaseOrder.Status.PENDING);
                _orders.getDataModel().updateData(selected);
                _title.setText(selected.size() + " Pending PurchaseOrder");
            }
        });
        _showDelivered.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<PurchaseOrder> selected = getOrders(PurchaseOrder.Status.DELIVERED);
                _orders.getDataModel().updateData(selected);
                _title.setText(selected.size() + " Delivered PurchaseOrder");
            }
        });

        _deliver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SwingWorker<List<PurchaseOrder>, Void>() {
                    @Override
                    protected List<PurchaseOrder> doInBackground() throws Exception {
                        List<PurchaseOrder> updated = new ArrayList<PurchaseOrder>();
                        EntityDataModel<PurchaseOrder> orders = _orders.getDataModel();
                        int n = orders.getRowCount();
                        for (int i = 0; i < n; i++) {
                            PurchaseOrder order = orders.getRow(i);
                            if (order.getStatus() == PurchaseOrder.Status.PENDING) {
                                updated.add(_service.deliver(order));
                            }
                        }
                        return updated;
                    }

                    @Override
                    public void done() {
                        try {
                            List<PurchaseOrder> updated = get(10, TimeUnit.SECONDS);
                            _orders.getDataModel().updateData(updated);
                            _title.setText(updated.size() + "" + " Updated PurchaseOrder");
                        } catch (Exception e) {
                            new ErrorDialog(e).setVisible(true);
                        }
                    }
                }.execute();
            }
        });
    }

    /**
     * Gets the orders in a background (i.e. not AWT event dispatch thread)
     * thread. <br>
     * But blocks painting anyway, because that is what is intended.
     * 
     */
    private List<PurchaseOrder> getOrders(final PurchaseOrder.Status status) {
        SwingWorker<List<PurchaseOrder>, Void> worker = new SwingWorker<List<PurchaseOrder>, Void>() {
            @Override
            protected List<PurchaseOrder> doInBackground() throws Exception {
                return _service.getOrders(status, null);
            }
        };
        worker.execute();
        try {
            return worker.get();
        } catch (Exception e) {
            new ErrorDialog(e).setVisible(true);
            return Collections.emptyList();
        }
    }
}

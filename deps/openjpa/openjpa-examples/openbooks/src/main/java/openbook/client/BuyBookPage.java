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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jpa.tools.swing.EntityDataModel;
import jpa.tools.swing.EntityTable;
import jpa.tools.swing.EntityTableView;
import jpa.tools.swing.ErrorDialog;
import openbook.client.Demo.ShowCodeAction;
import openbook.domain.Author;
import openbook.domain.Book;
import openbook.domain.Customer;
import openbook.domain.PurchaseOrder;
import openbook.domain.ShoppingCart;
import openbook.server.OpenBookService;
import openbook.server.QueryDecorator;

import org.apache.openjpa.lib.jdbc.SQLFormatter;

/**
 * A visual page coordinates the following functions of {@link OpenBookService} : 
 * <li>query for books
 * <li>choose one or more of the selected books
 * <li>add them to Shopping Cart
 * <li>place a purchase order of the books in the shopping cart.
 * <p>
 * Each interaction with the underlying service occurs in a background i.e. 
 * a <em>non</em>-AWT event dispatching thread. The background threads are
 * used via {@link SwingWorker} and hence each persistence operation is
 * can be potentially handled by a different JPA persistence context.
 * This threading model not only adheres to the good practice of responsive graphical 
 * user interface design, it exercises the <em>remote</em> nature of JPA service 
 * (even within this single process Swing application) where every operation
 * on a persistence context results into a set of <em>detached</em> instances
 * to the remote client.  
 * 
 * @author Pinaki Poddar
 */
@SuppressWarnings("serial")
public final class BuyBookPage extends JPanel {
    private final OpenBookService   _service;
    private final Customer          _customer;
    private final SearchPanel       _searchPanel;
    private final BuyPanel          _buyPanel;
    private final SelectBookPanel   _selectPanel;
    private final ShoppingCartPanel _cartPanel;
    
    /**
     * A Page with 2x2 Grid of panels each for one of the specific action.
     * 
     * @param service the OpenBooks service handle.
     */
    public BuyBookPage(OpenBookService service, Customer customer) {
        super(true);
        _service = service;
        _customer = customer;

        GridLayout gridLayout = new GridLayout(2, 2);
        this.setLayout(gridLayout);

        _searchPanel = new SearchPanel("Step 1: Search for Books");
        _selectPanel = new SelectBookPanel("Step 2: Select Books to view details");
        _buyPanel    = new BuyPanel("Step 3: Add Book to Shopping Cart");
        _cartPanel   = new ShoppingCartPanel("Step 4: Purchase Books in the Shopping Cart");

        add(_searchPanel);
        add(_cartPanel);
        add(_selectPanel);
        add(_buyPanel);
        
        _selectPanel._selectedBooks.getTable()
            .getSelectionModel()
            .addListSelectionListener(_buyPanel);
    }

    /**
     * A form like panel displays the different fields to search for books.
     * Zero or more form fields can be filled in. Though the service level
     * contract does not mandate how to form the exact query from the form
     * field values, the actual implementation demonstrates how dynamic 
     * query construction introduced via Criteria Query API in JPA 2.0
     * can aid in such common user scenarios.
     * <br>
     * The object level query is displayed to demonstrate the ability of
     * OpenJPA to express a dynamic Criteria Query is a human-readable,
     * JPQL-like query string.
     * 
     * @author Pinaki Poddar
     * 
     */
    class SearchPanel extends JPanel implements ActionListener {
        private final JTextField _title       = new JTextField("", 20);
        private final JTextField _author      = new JTextField("", 20);
        private final JTextField _maxPrice    = new JTextField("", 6);
        private final JTextField _minPrice    = new JTextField("", 6);
        private final JTextArea  _queryView   = new JTextArea();
        private final SQLFormatter _formatter = new SQLFormatter();
        
        SearchPanel(String title) {
            super(true);
            setBorder(BorderFactory.createTitledBorder(title));

            JLabel titleLabel  = new JLabel("Title :", SwingConstants.RIGHT);
            JLabel authorLabel = new JLabel("Author:", SwingConstants.RIGHT);
            JLabel priceLabel  = new JLabel("Price :", SwingConstants.RIGHT);
            JLabel fromLabel   = new JLabel("from ", SwingConstants.RIGHT);
            JLabel toLabel     = new JLabel("to ", SwingConstants.RIGHT);

            JPanel panel = new JPanel();
            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(true);
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup()
                                  .addComponent(titleLabel)
                                  .addComponent(authorLabel)
                                  .addComponent(priceLabel));
            hGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                  .addComponent(_title)
                                  .addComponent(_author)
                                  .addGroup(layout.createSequentialGroup()
                                          .addComponent(fromLabel)
                                          .addComponent(_minPrice)
                                          .addComponent(toLabel)
                                          .addComponent(_maxPrice)));
            
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(titleLabel)
                    .addComponent(_title));
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(authorLabel)
                    .addComponent(_author));
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(priceLabel)
                    .addComponent(fromLabel)
                    .addComponent(_minPrice)
                    .addComponent(toLabel)
                    .addComponent(_maxPrice));
            
            layout.setHorizontalGroup(hGroup);
            layout.setVerticalGroup(vGroup);

            JButton searchButton = new JButton("Search", Images.SEARCH);
            searchButton.setHorizontalTextPosition(SwingConstants.LEADING);
            ShowCodeAction showCode = Demo.getInstance().new ShowCodeAction();
            showCode.setPage("Dynamic Query", "server/OpenBookServiceImpl.java.html#buildQuery");
            JButton viewCodeButton = new JButton(showCode);
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(searchButton);
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(viewCodeButton);
            
            BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(box);
            add(panel);
            add(Box.createVerticalGlue());
            add(buttonPanel);
            
            _queryView.setBorder(BorderFactory.createTitledBorder("Criteria Query as CQL"));
            _queryView.setWrapStyleWord(true);
            _queryView.setEditable(false);
            _queryView.setBackground(getBackground());
            add(_queryView);
            searchButton.addActionListener(this);
        }
        
        /**
         * Execute a query and displays the result onto {@linkplain SelectBookPanel}.
         * 
         * The query is executed in a background, non-AWT thread.
         */
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<List<Book>, Void>() {
                private String queryString;
                @Override
                protected List<Book> doInBackground() throws Exception {
                    queryString = _service.getQuery(_title.getText(), 
                            asDouble(_minPrice), asDouble(_maxPrice), 
                            _author.getText());
                    return _service.select(_title.getText(), 
                            asDouble(_minPrice), asDouble(_maxPrice), 
                            _author.getText(), 
                            (QueryDecorator[])null);
                }

                @Override
                protected void done() {
                    try {
                        _queryView.setText(_formatter.prettyPrint(queryString).toString());
                        List<Book> selectedBooks = get(1, TimeUnit.SECONDS);
                        _selectPanel.updateDataModel(selectedBooks);
                    } catch (Exception e) {
                        new ErrorDialog(e).setVisible(true);
                    }
                }
            }.execute();
        }

        boolean isEmpty(JTextField text) {
            if (text == null)
                return true;
            String s = text.getText();
            return s == null || s.isEmpty() || s.trim().isEmpty();
        }

        Double asDouble(JTextField field) {
            if (isEmpty(field)) return null;
            try {
                return Double.parseDouble(field.getText());
            } catch (NumberFormatException e) {
                System.err.println("Can not convert [" + field.getText() + "] to a double");
            }
            return null;
        }
    }

    /**
     * A panel to display the selected books in a tabular format.
     * 
     * @author Pinaki Poddar
     * 
     */
    class SelectBookPanel extends JPanel {
        private final JLabel _bookCount;
        private final EntityTableView<Book> _selectedBooks;
        
        SelectBookPanel(String title) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder(title));

            _selectedBooks = new EntityTableView<Book>(Book.class,
                    EntityDataModel.BASIC_ATTR | EntityDataModel.ROW_COUNT, 
                    _service.getUnit());
            _bookCount = new JLabel();
    
            add(_bookCount, BorderLayout.NORTH);
            add(_selectedBooks, BorderLayout.CENTER);
        }
        
        void updateDataModel(List<Book> books) {
            _bookCount.setText(books.size() + " Book selected");
            _selectedBooks.getDataModel().updateData(books);
        }
    }

    /**
     * A panel to display the details of a single book and a button to add the
     * book to cart. Listens to the selection in the selected books.
     * 
     * @author Pinaki Poddar
     * 
     */
    class BuyPanel extends JPanel implements ListSelectionListener {
        JLabel _bookTitle;
        JLabel _bookAuthors;
        JLabel _bookPrice;
        JLabel _bookISBN;
        JButton _addToCart;
        JSpinner _quantity;
        JPanel _quantityPanel;
        
        public BuyPanel(String title) {
            super(true);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder(title));
            
            JPanel descPanel = new JPanel();
            descPanel.setLayout(new BoxLayout(descPanel, BoxLayout.Y_AXIS));
            _bookTitle   = new JLabel();
            _bookAuthors = new JLabel();
            _bookPrice   = new JLabel();
            _bookISBN    = new JLabel();
            descPanel.add(_bookTitle);
            descPanel.add(_bookAuthors);
            descPanel.add(_bookPrice);
            descPanel.add(_bookISBN);
            add(descPanel);

            _quantityPanel = new JPanel();
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
            _quantity = new JSpinner(spinnerModel);
            _quantityPanel.add(new JLabel("Quantity:"));
            _quantity.setEnabled(false);
            _quantityPanel.add(_quantity);
            _quantityPanel.setVisible(false);
            add(_quantityPanel);
            
            add(Box.createVerticalGlue());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(Box.createHorizontalGlue());
            _addToCart = new JButton("Add to Cart", Images.CART);
            _addToCart.setEnabled(false);
            buttonPanel.add(_addToCart);
            buttonPanel.add(Box.createHorizontalGlue());
            add(buttonPanel);

            _addToCart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _cartPanel.addBook((Book)_addToCart.getClientProperty("Book"), 
                            (Integer)_quantity.getValue());
                }
            });
        }

        void showBookDetails(Book book) {
            _bookTitle.setText(book.getTitle());
            List<Author> authors = book.getAuthors();
            if (authors != null && !authors.isEmpty()) {
                StringBuilder names = new StringBuilder();
                for (Author author : authors) {
                    if (names.length() != 0) names.append(", ");
                    names.append(author.getName());
                }
                _bookAuthors.setText("by " + names);
            }
            _bookPrice.setText("Price:" + book.getPrice());
            _bookISBN.setText("ISBN: " + book.getISBN());
            _addToCart.setEnabled(true);
            _quantity.setEnabled(true);
            _quantityPanel.setVisible(true);
            _addToCart.putClientProperty("Book", book);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            EntityTable<Book> table = _selectPanel._selectedBooks.getTable();
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            int col = table.getSelectedColumn();
            if (col == -1)
                return;
            EntityDataModel<Book> model = (EntityDataModel<Book>) table.getModel();
            Book book = model.getRow(row);
            showBookDetails(book);
        }
    }

    /**
     * A panel to display the shopping cart.
     * 
     * @author Pinaki Poddar
     * 
     */
    class ShoppingCartPanel extends JPanel implements ActionListener {
        private static final int MAX_ITEMS = 10;
        private final ShoppingCart _cart;
        private final JButton _placeOrder;
        private final JLabel[] _items = new JLabel[MAX_ITEMS];
        
        public ShoppingCartPanel(String title) {
            _cart = _customer.newCart();
            setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder(title));
            _placeOrder = new JButton("Place Order", Images.START);
            _placeOrder.setHorizontalTextPosition(SwingConstants.LEADING);
            
            _placeOrder.setEnabled(false);
            for (int i = 0; i < MAX_ITEMS; i++) {
                _items[i] = new JLabel("");
                add(_items[i]);
            }
            add(Box.createVerticalGlue());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(_placeOrder);
            buttonPanel.add(Box.createHorizontalGlue());
            add(buttonPanel);
            _placeOrder.addActionListener(this);
        }
        
        /**
         * Add the given book to the cart. Updates the display.
         */
        public void addBook(Book book, int quantity) {
            _cart.addItem(book, quantity);
            updateDisplay();
        }
        
        void updateDisplay() {
            Map<Book, Integer> items = _cart.getItems();
            int i = 0;
            for (Map.Entry<Book, Integer> entry : items.entrySet()) {
                JLabel item = _items[i++];
                int quantity = entry.getValue();
                Book book = entry.getKey();
                item.setText(quantity + (quantity == 1 ? " copy of " : " copies of ") + book.getTitle());
                item.setIcon(Images.DONE);
            }
            _placeOrder.setEnabled(items.size()>0);
            super.repaint();
        }       

        @Override
        public void actionPerformed(ActionEvent e) {
            new SwingWorker<PurchaseOrder, Void>() {
                @Override
                protected PurchaseOrder doInBackground() throws Exception {
                    return _service.placeOrder(_cart);
                }

                @Override
                protected void done() {
                    try {
                        get(1, TimeUnit.SECONDS);
                        _cart.clear();
                        for (int i = 0; i < MAX_ITEMS; i++) {
                            _items[i].setText("");
                            _items[i].setIcon(null);
                        }
                        _placeOrder.setEnabled(false);
                    } catch (Exception e) {
                        new ErrorDialog(e).setVisible(true);
                    }
                }
            }.execute();
        }
    }
}


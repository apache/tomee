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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import jpa.tools.swing.AttributeLegendView;
import jpa.tools.swing.ConfigurationViewer;
import jpa.tools.swing.ErrorDialog;
import jpa.tools.swing.GraphicOutputStream;
import jpa.tools.swing.MetamodelView;
import jpa.tools.swing.PowerPointViewer;
import jpa.tools.swing.PreparedQueryViewer;
import jpa.tools.swing.ScrollingTextPane;
import jpa.tools.swing.StatusBar;
import jpa.tools.swing.SwingHelper;
import openbook.domain.Customer;
import openbook.server.OpenBookService;
import openbook.server.ServiceFactory;
import openbook.util.PropertyHelper;

import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/**
 * A graphical user interface based client of OpenBooks for demonstration.
 *  
 * @author Pinaki Poddar
 *
 */
@SuppressWarnings("serial")
public class Demo extends JFrame {
    private static Dimension TAB_VIEW = new Dimension(800,600);
    private static Dimension OUT_VIEW = new Dimension(800,200);
    private static Dimension NAV_VIEW = new Dimension(400,800);

    /**
     * The actions invoked by this sample demonstration.
    */
    private Action _root;
    private Action _about;
    private Action _buyBook;
    private Action _deliver;
    private Action _supply;       
    private Action _viewConfig;  
    private Action _viewDomain; 
    private Action _viewSource;
    private Action _viewQuery;
    
    /**
     * The primary graphic widgets used to invoke and display the results of the actions.
     */
    private JToolBar    _toolBar;
    private JTree       _navigator;
    private JTabbedPane _tabbedPane;
    private JTabbedPane _outputPane;
    private StatusBar   _statusBar;
    private ScrollingTextPane   _sqlLog;
    private SQLLogger _sqlListener;
    private SourceCodeBrowser _sourceBrowser;
    private static Demo _instance;
    private static final String SRC_ROOT = "source/";
    
    /**
     * The handle to the service.
     */
    private OpenBookService     _service;
    private Customer            _customer;
    private Map<String, Object> _config;
    
    /**
     * Runs the demo.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SwingHelper.setLookAndFeel(14);
        adjustWidgetSize();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Demo demo = Demo.getInstance();
                demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                demo.pack();
                SwingHelper.position(demo, null);
                demo.setVisible(true);
            }
        });
    }
    
    public synchronized static Demo getInstance() {
        if (_instance == null) {
            _instance = new Demo();
        }
        return _instance;
    }
    
    static void adjustWidgetSize() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int sw = (int)(95*screen.getWidth()/100);
        int sh = (int)(80*screen.getHeight()/100);
        NAV_VIEW = new Dimension(25*sw/100, sh);
        TAB_VIEW = new Dimension(75*sw/100, 65*sh/100);
        OUT_VIEW = new Dimension(75*sw/100, 35*sh/100);
    }

    
    private Demo() {
        Thread.currentThread().setUncaughtExceptionHandler(new ErrorHandler());
        _config = PropertyHelper.load(System.getProperty("openbook.client.config", "demo.properties"));
        setIconImage(((ImageIcon)Images.LOGO_OPENBOOKS).getImage());
        setTitle("OpenBooks: A Sample JPA 2.0 Application");
        
        _root         = new WelcomeAction("OpenBooks", Images.LOGO_OPENBOOKS, "OpenBooks");
        _about        = new AboutAction("About OpenBooks", Images.LOGO_OPENBOOKS, "About OpenBooks");
        _buyBook      = new BuyBookAction("Buy", Images.BUY, "Browse and Buy Books");
        _deliver      = new DeliveryAction("Deliver", Images.DELIVERY, "Deliver Pending Orders");
        _supply       = new SupplyAction("Supply", Images.SUPPLY, "Supply Books");
        _viewConfig   = new ViewConfigAction("Unit", Images.VIEW_UNIT, "View Configuration");
        _viewDomain   = new ViewDomainAction("Domain", Images.VIEW_DOMAIN, "View Domain Model");
        _viewSource   = new ViewSourceAction("Source", Images.VIEW_CODE, "View Source Code");
        _viewQuery    = new ViewQueryCacheAction("Query", Images.VIEW_QUERY, "View Queries");
        
        _toolBar    = createToolBar();
        _navigator  = createNavigator();
        _tabbedPane = createTabbedView();
        _outputPane = createOutputView();
        _statusBar  = createStatusBar();
        
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setContinuousLayout(true);
        horizontalSplitPane.setDividerSize(5);
        JScrollPane scrollPane = new JScrollPane(_navigator);
        scrollPane.setMinimumSize(new Dimension(NAV_VIEW.width/4, NAV_VIEW.height));
        scrollPane.setPreferredSize(NAV_VIEW);
        horizontalSplitPane.add(scrollPane);
        
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setContinuousLayout(true);
        verticalSplitPane.setDividerSize(5);
        verticalSplitPane.add(_tabbedPane);
        verticalSplitPane.add(_outputPane);
        horizontalSplitPane.add(verticalSplitPane);
        
        Container content = getContentPane();
        content.add(_toolBar, BorderLayout.PAGE_START);
        content.add(horizontalSplitPane, BorderLayout.CENTER);
        content.add(_statusBar, BorderLayout.SOUTH);
        
        _root.actionPerformed(null);
    }
    
    /**
     * Gets the handle to OpenBooks service. 
     */
    public OpenBookService getService() {
        if (_service == null) {
            final String unitName = getConfiguration("openbook.unit", OpenBookService.DEFAULT_UNIT_NAME);
            
            SwingWorker<OpenBookService, Void> getService = new SwingWorker<OpenBookService, Void> () {
                @Override
                protected OpenBookService doInBackground() throws Exception {
                    Map<String, Object> runtimeConfig = new HashMap<String, Object>();
                    runtimeConfig.put("openjpa.jdbc.JDBCListeners", new JDBCListener[]{_sqlListener});
                    OpenBookService service = ServiceFactory.getService(unitName, runtimeConfig);
                    service.initialize(null);
                    return service;
                }
                
            };
            getService.execute();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                _service = getService.get(10, TimeUnit.SECONDS);
            } catch (Exception t) {
                new ErrorDialog(t).setVisible(true);
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        return _service;
    }
    
    public Customer getCustomer() {
        if (_customer == null) {
            SwingWorker<Customer, Void> task = new SwingWorker<Customer, Void> () {
                @Override
                protected Customer doInBackground() throws Exception {
                    return getService().login("guest");
                }
                
            };
            task.execute();
            try {
                _customer = task.get(1, TimeUnit.SECONDS);
            } catch (Exception t) {
                new ErrorDialog(t).setVisible(true);
            }
        }
        return _customer;
    }
    
    private JToolBar  createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(_buyBook);
        toolBar.add(_deliver);
        toolBar.add(_supply);
        Dimension d = new Dimension(40, 32);
        toolBar.addSeparator(d);
        
        toolBar.add(_viewConfig);
        toolBar.add(_viewDomain);
        toolBar.add(_viewSource);
        toolBar.add(_viewQuery);
        
        toolBar.addSeparator(d);
        
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(_about);
        toolBar.add(Box.createHorizontalStrut(2));
        return toolBar;
    }
    
    private StatusBar createStatusBar() {
        return new StatusBar();
    }
    
    public String getConfiguration(String key, String def) {
        return PropertyHelper.getString(_config, key, def);
    }
    
    private SourceCodeBrowser getSourceCodeBrowser() {
        if (_sourceBrowser == null) {
            String root = getConfiguration("openbook.source.root",  SRC_ROOT);
            boolean external = "true".equalsIgnoreCase(
                    getConfiguration("openbook.source.browser.external", "false"))
                    && Desktop.isDesktopSupported();
            _sourceBrowser = new SourceCodeBrowser(root, false);
            if (!external) {
                Map<String,String> initialPages = PropertyHelper.getMap(_config, "openbook.source.list");
                for (Map.Entry<String, String> entry : initialPages.entrySet()) {
                    _sourceBrowser.addPage(entry.getKey(), entry.getValue());
                }
                showTab(_tabbedPane, "Source Code", _sourceBrowser.getViewer());
            }
        }
        return _sourceBrowser;
    }

    /**
     * Abstract root of all Action objects helps to locate/configure visual action parameters such as
     * tooltip text or image.
     * 
     * @author Pinaki Poddar
     *
     */
    public abstract class OpenBookAction extends AbstractAction {
        public OpenBookAction(String name, Icon icon, String tooltip) {
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, tooltip);
            putValue(Action.SMALL_ICON, icon);
        }
    }
    
    public class BuyBookAction extends OpenBookAction {
        BuyBookPage         _buyBookPage;
        public BuyBookAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (_buyBookPage == null) {
                _buyBookPage = new BuyBookPage(getService(), getCustomer());
            }
            showTab(_tabbedPane, "Buy Books", _buyBookPage);
            switchTab(_outputPane, _sqlLog);
        }
        
    }
    public class DeliveryAction extends OpenBookAction {
        DeliveryPage        _deliveryPage;
        public DeliveryAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            if (_deliveryPage == null) {
                _deliveryPage = new DeliveryPage(getService());
            }
            showTab(_tabbedPane, "Deliver Books", _deliveryPage);
            switchTab(_outputPane, _sqlLog);
        }
        
    }
    
    public class SupplyAction extends OpenBookAction {
        SupplyPage          _supplyPage;
        public SupplyAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            if (_supplyPage == null) {
                _supplyPage = new SupplyPage(getService());
            }
            showTab(_tabbedPane, "Supply Books", _supplyPage);
            switchTab(_outputPane, _sqlLog);
        }
        
    }
    
    public class ViewConfigAction extends OpenBookAction {
        ConfigurationViewer _configView;
        public ViewConfigAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            if (_configView == null) {
                _configView = new ConfigurationViewer("Unit Configuration", getService().getUnit().getProperties());
                showTab(_tabbedPane, "Configuration", new JScrollPane(_configView));
            } else {
                showTab(_tabbedPane, "Configuration", _configView);
            }
        }
        
    }
    
    public class ViewDomainAction extends OpenBookAction {
        MetamodelView       _domainView;
        AttributeLegendView _legends;
        public ViewDomainAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            if (_domainView == null) {
                _domainView = new MetamodelView(getService().getUnit().getMetamodel());
                _legends = new AttributeLegendView();
                showTab(_outputPane, "Legends", new JScrollPane(_legends));
            }
            showTab(_tabbedPane, "Domain Model", _domainView);
        }
        
    }

    public class ViewDataAction extends OpenBookAction {
        public ViewDataAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            showTab(_tabbedPane, "Buy Books", null);
        }
        
    }
    
    public class ViewQueryCacheAction extends OpenBookAction {
        PreparedQueryViewer _queryView;
        public ViewQueryCacheAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        public void actionPerformed(ActionEvent e) {
            if (_queryView == null) {
                _queryView = new PreparedQueryViewer(OpenJPAPersistence.cast(getService().getUnit()));
                showTab(_tabbedPane, "JPQL Query", new JScrollPane(_queryView));
            }
            showTab(_tabbedPane, "JPQL Queries", _queryView);
        }
        
    }
    
    public class ViewSourceAction extends OpenBookAction {
        public ViewSourceAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        
        public void actionPerformed(ActionEvent e) {
            getSourceCodeBrowser();
        }
    }
    
    /**
     * An action to show a piece of code in an internal or external browser.
     *
     */
    public class ShowCodeAction extends OpenBookAction {
        private String _key;
        private String _page;
        
        public ShowCodeAction() {
            super("View Code", Images.VIEW_CODE, "View Java Source Code");
        }
        
        public void setPage(String key, String page) {
            _key  = key;
            _page = page;
        }
        
        public void actionPerformed(ActionEvent e) {
           getSourceCodeBrowser().showPage(_key, _page);
        }
    }
    
    /**
     * Displays the "welcome" page.
     *  
     * @author Pinaki Poddar
     *
     */
    public class WelcomeAction extends OpenBookAction {
        PowerPointViewer    _powerpoint;
        JLabel              _logoLabel = new JLabel(Images.LOGO_OPENBOOKS);
        boolean _showPresentation = true;
        
        public WelcomeAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (_powerpoint == null && _showPresentation) {
                String dir = getConfiguration("openbook.slides.dir", "slides/");
                String[] defaultSlides = { 
                                    "Slide1.JPG",
                                    "Slide2.JPG",
                                    "Slide3.JPG",
                                    "Slide4.JPG",
                                    "Slide5.JPG",
                                    "Slide6.JPG",
                                    "Slide7.JPG",
                                    "Slide8.JPG",
                                    "Slide9.JPG",
                                    "Slide10.JPG",
                                    "Slide11.JPG",
                                    "Slide12.JPG",
                                    "Slide13.JPG",
                                    "Slide14.JPG",
                                    "Slide15.JPG"};
                List<String> slides = PropertyHelper.getStringList(_config, "openbook.slides.list",
                        Arrays.asList(defaultSlides));
                try {
                    _powerpoint = new PowerPointViewer(dir, slides);
                } catch (Exception e1) {
                    _showPresentation = false;
                    System.err.println("Error while opening slide deck at " + dir + ". \r\n"+ e1);
                }
            } 
            showTab(_tabbedPane, "Home", _powerpoint != null ? _powerpoint : _logoLabel);
        }
        
    }
    
    public class AboutAction extends OpenBookAction {
        AboutDialog _dialog;
        
        public AboutAction(String name, Icon icon, String tooltip) {
            super(name, icon, tooltip);
        }
        
        public void actionPerformed(ActionEvent e) {
            if (_dialog == null) {
                _dialog = new AboutDialog(Images.LOGO_OPENBOOKS);
                SwingHelper.position(_dialog, Demo.this);
            }
            _dialog.setVisible(true);
        }
    }
    
    /**
     * Show the given tab in the given pane.
     * @param pane the tabbed pane
     * @param title title of the tab component
     * @param tab the component to show
     */
    void showTab(JTabbedPane pane, String title, Component tab) {
        if (tab == null)
            return;
        Component c = locateTab(pane, tab);
        if (c == null) {
            pane.addTab(title, tab);
            pane.setSelectedComponent(tab);
        } else {
            pane.setSelectedComponent(c);
        }
    }
    
    void switchTab(JTabbedPane pane, Component tab) {
        if (tab == null)
            return;
        Component c = locateTab(pane, tab);
        if (c == null) {
            pane.setSelectedComponent(c);
        } 
    }
    
    Component locateTab(JTabbedPane pane, Component tab) {
        int index = pane.indexOfComponent(tab);
        if (index == -1) {
            Component[] components = pane.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JScrollPane 
                && (((JScrollPane)components[i]).getViewport().getView() == tab)) {
                    return components[i];
                }
            }
        } else {
            return pane.getComponentAt(index);
        }
        return null;
    }
    
    
    private JTabbedPane createTabbedView() {
        JTabbedPane pane = new JTabbedPane();
        pane.setPreferredSize(TAB_VIEW);
        pane.setMinimumSize(new Dimension(TAB_VIEW.width, TAB_VIEW.height));
        return pane;
    }
    
    private JTabbedPane createOutputView() {
        JTabbedPane pane = new JTabbedPane();
        pane.setPreferredSize(OUT_VIEW);
        
        _sqlListener = new SQLLogger();
        _sqlLog = new ScrollingTextPane();
        
        GraphicOutputStream stream = new GraphicOutputStream(_sqlLog);
        _sqlLog.setPreferredSize(TAB_VIEW);
        _sqlListener.setOutput(stream);
        pane.addTab("SQL Log", new JScrollPane(_sqlLog));
        
        ScrollingTextPane consoleLog = new ScrollingTextPane();
        GraphicOutputStream console = new GraphicOutputStream(consoleLog);
        System.setErr(new PrintStream(console, true));
        pane.addTab("Console", new JScrollPane(consoleLog));
        
        return pane;
    }
    
    /**
     * Creates the navigation tree and adds the tree nodes. Each tree node is attached with an action
     * that fires when the node is selected.  
     */
    private JTree createNavigator() {
        ActionTreeNode root = new ActionTreeNode(_root);
        DefaultMutableTreeNode app   = new DefaultMutableTreeNode("WorkFlows");
        DefaultMutableTreeNode views = new DefaultMutableTreeNode("Views");
        root.add(app);
        root.add(views);
        
        
        app.add(new ActionTreeNode(_buyBook));
        app.add(new ActionTreeNode(_deliver));
        app.add(new ActionTreeNode(_supply));
        
        views.add(new ActionTreeNode(_viewConfig));
        views.add(new ActionTreeNode(_viewDomain));
        views.add(new ActionTreeNode(_viewQuery));
        views.add(new ActionTreeNode(_viewSource));
        
        
        JTree tree = new JTree(root);
        tree.setShowsRootHandles(true);
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                Object treeNode = _navigator.getLastSelectedPathComponent();
                if (treeNode instanceof ActionTreeNode) {
                    ((ActionTreeNode)treeNode)._action.actionPerformed(null);
                }
            }
        });
        tree.setCellRenderer(new TypedTreeCellRenderer());
        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        
        return tree;
    }
    
    /**
     * A tree node which may have an associated action.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class ActionTreeNode extends DefaultMutableTreeNode {
        private final Action _action;
        public ActionTreeNode(Action action) {
            _action = action;
        }
        
        public String toString() {
            return _action.getValue(Action.SHORT_DESCRIPTION).toString();
        }
        
    }
    
    public class TypedTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, 
                boolean leaf, int row, boolean hasFocus) { 
            return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
    }
    
    public static class AboutDialog extends JDialog {
        public AboutDialog(Icon logo) {
            setModal(true);
            setLayout(new BorderLayout());
            JButton button = new JButton("<html>" 
                    + "<b>OpenBooks</b> " 
                    + "<p>"
                    + "<br> by JPA Team, SWG" 
                    + "<br> IBM Corporation" 
                    + "<p>"
                    + "</html>");
            button.setIcon(logo);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setIconTextGap(4);
            button.setEnabled(true);
            button.setBorderPainted(false);
            JLabel openJPALogo = new JLabel(Images.LOGO_OPENJPA);
            String version = "Version " + OpenJPAVersion.MAJOR_RELEASE + "." + OpenJPAVersion.MINOR_RELEASE;
            openJPALogo.setBorder(BorderFactory.createTitledBorder(version));
            openJPALogo.setBackground(Color.BLACK);

            add(button, BorderLayout.CENTER);
            add(openJPALogo, BorderLayout.SOUTH);

            setTitle("About OpenBooks");
            setAlwaysOnTop(true);
            setResizable(false);
            pack();
        }
    }

}

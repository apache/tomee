package org.superbiz.moviefun;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

public class NotificationMonitor {
    private static TrayIcon trayIcon;

    public static void main(String[] args) throws NamingException, InterruptedException, AWTException, MalformedURLException {
        addSystemTrayIcon();

        // Boot the embedded EJB Container 
        new InitialContext();

        System.out.println("Starting monitor...");
    }

    private static void addSystemTrayIcon() throws AWTException, MalformedURLException {
        SystemTray tray = SystemTray.getSystemTray();

        URL moviepng = NotificationMonitor.class.getClassLoader().getResource("movie.png");
        Image image = Toolkit.getDefaultToolkit().getImage(moviepng);

        ActionListener exitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Exiting monitor...");
                System.exit(0);
            }
        };

        PopupMenu popup = new PopupMenu();
        MenuItem defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);

        trayIcon = new TrayIcon(image, "Notification Monitor", popup);
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);


    }

    public static void showAlert(String message) {
        synchronized (trayIcon) {
            trayIcon.displayMessage("Alert received", message, TrayIcon.MessageType.WARNING);
        }
    }
}
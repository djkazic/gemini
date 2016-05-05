package gui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

public class TrayHandler {
	
	public static void init() {
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
		    System.out.println("SystemTray is not supported");
		    return;
		}
		
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon((new ImageIcon(TrayHandler.class.getClassLoader().getResource("res/imgres/tray.png"))).getImage());
		final SystemTray tray = SystemTray.getSystemTray();
		   
		// Create a pop-up menu components
		MenuItem exitItem = new MenuItem("Exit");
		
		// Add components to pop-up menu
        popup.add(exitItem);
        
        // Add exit listener to exitItem
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
        
        // Set pop-up menu to trigger on icon
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.println("TrayIcon could not be added.");
            ex.printStackTrace();
        }
	}

}

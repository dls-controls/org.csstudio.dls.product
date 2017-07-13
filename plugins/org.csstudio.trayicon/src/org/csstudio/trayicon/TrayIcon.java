package org.csstudio.trayicon;

import org.csstudio.utility.product.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class TrayIcon {

    private static final String IMAGE_FILE = "icons/css.ico";
    private static final Image IMAGE = Activator.getImageDescriptor(IMAGE_FILE).createImage();
    private TrayItem trayItem;
    private Menu menu;
    private MenuItem openMenuItem;
    private MenuItem exitMenuItem;
    private IWorkbenchWindow window;

    private boolean minimized;

    private void removeFromTray() {
        trayItem.dispose();
        openMenuItem.dispose();
        exitMenuItem.dispose();
        menu.dispose();
    }

    private void raiseWindow(Shell shell) {
        shell.setVisible(true);
        shell.setActive();
        shell.setFocus();
        shell.setMinimized(false);
        shell.layout();
    }

    public void minimize() {
        trayItem = new TrayItem(Display.getCurrent().getSystemTray(), SWT.NONE);
        trayItem.setImage(IMAGE);
        trayItem.setToolTipText(Messages.TrayIcon_tooltip);
        // There should be exactly one workbench window when this is being called.
        window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        window.getShell().setVisible(false);
        trayItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                raiseWindow(window.getShell());
                trayItem.dispose();
                minimized = false;
            }
        });
        trayItem.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });
        // Create a Menu
        menu = new Menu(window.getShell(), SWT.POP_UP);
        // Create the open menu item.
        openMenuItem = new MenuItem(menu, SWT.PUSH);
        openMenuItem.setText(Messages.TrayIcon_open);
        openMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                unminimize();
            }
        });

        // Create the exit menu item.
        exitMenuItem = new MenuItem(menu, SWT.PUSH);
        exitMenuItem.setText(Messages.TrayIcon_exit);
        exitMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                removeFromTray();
                PlatformUI.getWorkbench().close();
            }
        });
        minimized = true;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void unminimize() {
        raiseWindow(window.getShell());
        removeFromTray();
        minimized = false;
    }

}

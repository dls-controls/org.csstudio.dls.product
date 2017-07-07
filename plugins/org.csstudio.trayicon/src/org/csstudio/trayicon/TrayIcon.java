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

    public static String TOOLTIP = "CS-Studio";
    public static String IMAGE = "icons/css.ico";
    private TrayItem item;
    final private Image image = Activator.getImageDescriptor(IMAGE).createImage();
    private Menu menu;
    private MenuItem open;
    private MenuItem exit;
    IWorkbenchWindow w;

    private boolean minimized;

    private void dispose() {
        item.dispose();
        open.dispose();
        exit.dispose();
        menu.dispose();
    }

    public void raiseWindow(Shell shell) {
        shell.setVisible(true);
        shell.setActive();
        shell.setFocus();
        shell.setMinimized(false);
    }

    public void minimize() {
        item = new TrayItem(Display.getCurrent().getSystemTray(), SWT.NONE);
        item.setImage(image);
        item.setToolTipText(TOOLTIP);
        // There should be exactly one workbench window when this is being called.
        w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            w.getShell().setVisible(false);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                raiseWindow(w.getShell());
                item.dispose();
                minimized = false;
            }
        });
        // Create a Menu
        menu = new Menu(w.getShell(), SWT.POP_UP);
        // Create the exit menu item.
        exit = new MenuItem(menu, SWT.PUSH);
        exit.setText("Exit");
        // Create the open menu item.
        open = new MenuItem(menu, SWT.PUSH);
        open.setText("Open");
        open.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                unminimize();
            }
        });
        item.addListener(SWT.MenuDetect, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });

        exit.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                dispose();
                PlatformUI.getWorkbench().close();
            }
        });
        minimized = true;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void unminimize() {
        raiseWindow(w.getShell());
        dispose();
        minimized = false;
    }

}

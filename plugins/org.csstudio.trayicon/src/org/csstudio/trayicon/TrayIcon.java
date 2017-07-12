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

    public static final String IMAGE = "icons/css.ico";
    private TrayItem trayItem;
    final private Image image = Activator.getImageDescriptor(IMAGE).createImage();
    private Menu menu;
    private MenuItem open;
    private MenuItem exit;
    private IWorkbenchWindow window;

    private boolean minimized;

    private void dispose() {
        trayItem.dispose();
        open.dispose();
        exit.dispose();
        menu.dispose();
    }

    public void raiseWindow(Shell shell) {
        shell.setVisible(true);
        shell.setActive();
        shell.setFocus();
        shell.setMinimized(false);
        shell.layout();
    }

    public void minimize() {
        trayItem = new TrayItem(Display.getCurrent().getSystemTray(), SWT.NONE);
        trayItem.setImage(image);
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
        open = new MenuItem(menu, SWT.PUSH);
        open.setText(Messages.TrayIcon_open);
        open.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                unminimize();
            }
        });

        // Create the exit menu item.
        exit = new MenuItem(menu, SWT.PUSH);
        exit.setText(Messages.TrayIcon_exit);
        exit.addListener(SWT.Selection, new Listener() {
            @Override
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
        raiseWindow(window.getShell());
        dispose();
        minimized = false;
    }

}

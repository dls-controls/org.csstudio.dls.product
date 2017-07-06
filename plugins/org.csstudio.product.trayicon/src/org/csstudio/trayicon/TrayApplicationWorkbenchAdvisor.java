package org.csstudio.trayicon;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.utility.product.Activator;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
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

public class TrayApplicationWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {
	
    private boolean minimizedToTray = false;
	
    public TrayApplicationWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
    	super(openDocProcessor);
    }

    private void minimizeToTray() {
        final TrayItem item = new TrayItem(Display.getCurrent().getSystemTray(),
                SWT.NONE);
        final Image image = Activator.getImageDescriptor("icons/css.ico")
                .createImage();
        item.setImage(image);
        item.setToolTipText("RCPMail - Tray Icon");
        // There should be exactly one workbench window when this is being called.
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            w.getShell().setVisible(false);
        item.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Shell workbenchWindowShell = w.getShell();
                workbenchWindowShell.setVisible(true);
                workbenchWindowShell.setActive();
                workbenchWindowShell.setFocus();
                workbenchWindowShell.setMinimized(false);
                image.dispose();
                item.dispose();
                minimizedToTray = false;
            }
        });
        // Create a Menu
        final Menu menu = new Menu(w.getShell(), SWT.POP_UP);
        // Create the exit menu item.
        final MenuItem exit = new MenuItem(menu, SWT.PUSH);
        exit.setText("Exit");
        // Create the open menu item.
        final MenuItem open = new MenuItem(menu, SWT.PUSH);
        open.setText("Open");
        // make the workbench visible in the event handler for exit menu item.
        open.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Shell workbenchWindowShell = w.getShell();
                workbenchWindowShell.setVisible(true);
                workbenchWindowShell.setActive();
                workbenchWindowShell.setFocus();
                workbenchWindowShell.setMinimized(false);
                image.dispose();
                item.dispose();
                open.dispose();
                exit.dispose();
                menu.dispose();
                minimizedToTray = false;
            }
        });
        item.addListener(SWT.MenuDetect, new Listener() {
            public void handleEvent(Event event) {
                menu.setVisible(true);
            }
        });

        // Do a workbench close in the event handler for exit menu item.
        exit.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                image.dispose();
                item.dispose();
                open.dispose();
                exit.dispose();
                menu.dispose();
                PlatformUI.getWorkbench().close();
            }
        });
    }

    @Override
    public boolean preShutdown() {
        if (minimizedToTray) {
        	return super.preShutdown();
        } else {
            minimizeToTray();
            minimizedToTray = true;
            return false;
        }
    }

}

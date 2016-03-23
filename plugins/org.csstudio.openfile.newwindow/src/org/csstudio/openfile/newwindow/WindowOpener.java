package org.csstudio.openfile.newwindow;

import java.util.logging.Level;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class WindowOpener {

    private WindowSpec windowSpec;

    public WindowOpener(WindowSpec windowSpec) {
        this.windowSpec = windowSpec;
    }

    public void raiseOrOpenWindow() throws WindowManagementException {
        boolean found = false;
        for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            String windowId = w.getActivePage().getPerspective().getId();
            if (windowId.equals(windowSpec.getPerspectiveId())) {
                System.out.println("Found!");
                forceActive(w.getShell());
                found = true;
            } else {
                System.out.println("Other perspective " + windowId);
            }
        }
        if (!found) {
            try {
                PlatformUI.getWorkbench().openWorkbenchWindow(windowSpec.getPerspectiveId(), null);
            } catch (WorkbenchException e) {
                Plugin.getLogger().log(Level.WARNING, "Failed to open new workbench window", e);
            }
        }
    }

    public void forceActive(Shell shell) {
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                //if (!shell.getMaximized()) {
  //              shell.setVisible(false);
                    shell.setMaximized(true);
                //} else if (shell.getMinimized()) {
                    shell.setMinimized(false);
                    shell.setMaximized(false);
//                shell.forceFocus();
//                shell.forceActive();
//                shell.setFocus();
//                shell.setActive();
             //  }
                shell.forceActive();
            }
        });
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //
                }

//                shell.setActive();
//                shell.forceActive();
//
//                //shell.setEnabled(false);
//                shell.setVisible(true);
                //shell.setEnabled(true);
                shell.open();
            }
        });

      }


}

package org.csstudio.openfile.newwindow;

import java.util.logging.Level;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class WindowOpener {

    private WindowSpec windowSpec;

    public WindowOpener(WindowSpec windowSpec) {
        this.windowSpec = windowSpec;
    }

    public void openWindow() throws WindowManagementException {
        try {
            PlatformUI.getWorkbench().openWorkbenchWindow(windowSpec.getPerspectiveId(), null);
        } catch (WorkbenchException e) {
            Plugin.getLogger().log(Level.WARNING, "Failed to open new workbench window", e);
        }
    }

    public void changeActivePerspective() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveDescriptor descriptor = window.getWorkbench()
                .getPerspectiveRegistry()
                .findPerspectiveWithId(windowSpec.getPerspectiveId());
        window.getActivePage().setPerspective(descriptor);
        window.getActivePage().resetPerspective();
    }

}

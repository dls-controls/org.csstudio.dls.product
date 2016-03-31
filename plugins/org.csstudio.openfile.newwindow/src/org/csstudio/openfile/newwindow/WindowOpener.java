package org.csstudio.openfile.newwindow;

import java.util.logging.Level;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Utility for opening Eclipse workbench windows in specific perspectives.
 */
public class WindowOpener {

    private WindowSpec windowSpec;

    public WindowOpener(WindowSpec windowSpec) {
        this.windowSpec = windowSpec;
    }

    /**
     * Open a new workbench window in the perspective supplied by the WindowSpec object.
     * @throws WindowManagementException if opening the window fails.
     */
    public void openNewWindow() throws WindowManagementException {
        try {
            PlatformUI.getWorkbench().openWorkbenchWindow(windowSpec.getPerspectiveId(), null);
        } catch (WorkbenchException e) {
            Plugin.getLogger().log(Level.WARNING, Messages.WindowOpener_openFailed, e);
        }
    }

    /**
     * Change the perspective in the active workbench window to the one supplied by the
     * WindowSpec object.
     */
    public void changeActivePerspective() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IPerspectiveDescriptor descriptor = window.getWorkbench()
                .getPerspectiveRegistry()
                .findPerspectiveWithId(windowSpec.getPerspectiveId());
        window.getActivePage().setPerspective(descriptor);
        window.getActivePage().resetPerspective();
    }

}

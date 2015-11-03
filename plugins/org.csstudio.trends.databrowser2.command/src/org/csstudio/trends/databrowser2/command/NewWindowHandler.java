package org.csstudio.trends.databrowser2.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.trends.databrowser2.editor.DataBrowserEditor;
import org.csstudio.utility.singlesource.PathEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class NewWindowHandler extends AbstractHandler {

    public static final String PLOTFILE_PARAM = "plotfile";

    public final static String DATABROWSER_PERSPECTVE = "org.csstudio.trends.databrowser.Perspective";

    private static final Logger LOGGER = Logger.getLogger(NewWindowHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IPath path = new Path(event.getParameter(PLOTFILE_PARAM));
        path = ResourceUtil.workspacePathToSysPath(path);
        if (!ResourceUtil.isExistingLocalFile(path)) {
            LOGGER.warning("Databrowser plot file " + path + " does not exist.");
            return null;
        }

        IWorkbenchPage dbpage = null;
        try {
            // Find a window in the databrowser perspective.
            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (IWorkbenchWindow window : windows) {
                IWorkbenchPage page = window.getActivePage();
                IPerspectiveDescriptor ipd = page.getPerspective();
                if (ipd.getId().equals(DATABROWSER_PERSPECTVE)) {
                    dbpage = page;
                    IWorkbench wb = PlatformUI.getWorkbench();
                    wb.showPerspective(DATABROWSER_PERSPECTVE, window);
                    break;
                }
            }
            // Open a new window in the databrowser perspective.
            if (dbpage == null) {
                final IWorkbenchWindow window = PlatformUI.getWorkbench().openWorkbenchWindow(DATABROWSER_PERSPECTVE, null);
                dbpage = window.getActivePage();
            }
            final Shell shell = dbpage.getWorkbenchWindow().getShell();
            if (shell.getMinimized())
                shell.setMinimized(false);
            shell.forceActive();
            shell.forceFocus();
            final IEditorInput input = new PathEditorInput(path);
            DataBrowserEditor.createInstance(input);
            shell.moveAbove(null);
        } catch (WorkbenchException e) {
            LOGGER.log(Level.WARNING, "Failed to create databrowser window", e);
        }
        return null;
    }

}

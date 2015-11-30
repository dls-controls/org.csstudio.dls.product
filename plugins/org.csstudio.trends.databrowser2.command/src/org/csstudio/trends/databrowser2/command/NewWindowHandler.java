package org.csstudio.trends.databrowser2.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.trends.databrowser2.editor.DataBrowserEditor;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.model.PVItem;
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
    public static final String PVNAME_PARAM = "pv";

    public final static String DATABROWSER_PERSPECTVE = "org.csstudio.trends.databrowser.Perspective";

    private static final Logger LOGGER = Logger.getLogger(NewWindowHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String plotfile = event.getParameter(PLOTFILE_PARAM);
        IPath path = null;
        if (plotfile != null) {
            path = ResourceUtil.workspacePathToSysPath(new Path(plotfile));
            if (!ResourceUtil.isExistingLocalFile(path)) {
                LOGGER.warning("Databrowser plot file " + path + " does not exist.");
            }
        }

        // TODO:this is a single PV name; allowing multiple PVs is expansion
        String pvname = event.getParameter(PVNAME_PARAM);
        if (pvname != null) {
            LOGGER.info("Found PV: " + pvname);
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

            DataBrowserEditor editor = createEditor(path);

            if (pvname != null) {
                try {
                    PVItem newPv = new PVItem(pvname, 0.0); // monitor the PV, don't scan it
                    newPv.useDefaultArchiveDataSources();

                    Model model = editor.getModel();
                    // ensure an axis exists.
                    // if no plt file is specified this is always required
                    if (model.getAxisCount() == 0) {
                        model.addAxis();
                    }

                    model.addItem(newPv);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to add PV to DataBrowser", e);
                }
            }

            shell.moveAbove(null);
        } catch (WorkbenchException e) {
            LOGGER.log(Level.WARNING, "Failed to create databrowser window", e);
        }
        return null;
    }

    private DataBrowserEditor createEditor(IPath path) {
        DataBrowserEditor editor = null;
        if (path != null) {
            final IEditorInput input = new PathEditorInput(path);
            editor = DataBrowserEditor.createInstance(input);
        } else {
            editor = DataBrowserEditor.createInstance();
        }
        return editor;
    }

}

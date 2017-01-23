package org.csstudio.trends.databrowser2.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.trends.databrowser2.editor.DataBrowserEditor;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.model.PVItem;
import org.csstudio.utility.singlesource.PathEditorInput;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
    public static final String PVNAMES_PARAM = "pvnames";

    private static final String PV_NAME_SEPARATOR = ",";

    public final static String DATABROWSER_PERSPECTVE = "org.csstudio.trends.databrowser.Perspective";

    private static final Logger LOGGER = Logger.getLogger(NewWindowHandler.class.getName());

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final Shell shell = getDatabrowserShell();
        if (shell == null) {
            LOGGER.log(Level.SEVERE, "Failed to find or create databrowser perspective");
            return null;
        }

        if (shell.getMinimized())
            shell.setMinimized(false);
        shell.forceActive();
        shell.forceFocus();
        shell.moveAbove(null);

        IPath path = parsePlotfile(event.getParameter(PLOTFILE_PARAM));
        DataBrowserEditor editor = createEditor(path);

        List<String> pvNames = parsePvNames(event.getParameter(PVNAMES_PARAM));
        addNamedPVs(editor, pvNames);

        return null;
    }

    /**
     * Inject named PVs into a Databrowser.
     *
     * All PVs are added to the same, default axis. If no axis exists
     * in the model one is added.
     *
     * @param editor DataBrowser to use
     * @param pvNames List of PV names to add
     */
    private void addNamedPVs(DataBrowserEditor editor, List<String> pvNames) {
        if (!pvNames.isEmpty()) {
            Model model = editor.getModel();

            // ensure an axis exists.
            // if no plt file is specified this will be required
            if (model.getAxisCount() == 0) {
                model.addAxis();
            }

            for (String pvName : pvNames) {
                try {
                    PVItem newPv = new PVItem(pvName, 0.0); // monitor the PV, don't scan it
                    newPv.useDefaultArchiveDataSources();
                    LOGGER.log(Level.INFO, "Added " + pvName + " to databrowser plot");
                    model.addItem(newPv);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to add PV " + pvName + " to the databrowser", e);
                }
            }
        }
    }

    /**
     * Identify an open CSStudio windows containing the DatabrowserPerspective.
     *
     * If an existing window can be found, switch to it otherwise open a new
     * CSStudio window and open the databrowser perspective
     *
     * @return Shell containing the databrowser perspective or Null
     */
    private Shell getDatabrowserShell() {
        Shell shell = null;

        try {
            IWorkbenchPage dbpage = null;
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

            // grab the window shell to use as a handle
            if (dbpage != null) {
                shell = dbpage.getWorkbenchWindow().getShell();
            }
        } catch (WorkbenchException e) {
            LOGGER.log(Level.SEVERE, "Failed to create databrowser window", e);
        }
        return shell;
    }

    /**
     * Parse the 'plotfile' parameter.
     * These should be workspace paths.
     *
     * @param plotfileParam Path to plotfile
     * @return workspace IPath or null if file does not exist
     */
    private IPath parsePlotfile(String plotfileParam) {
        IPath plotPath = null;
        if (plotfileParam != null) {
            IPath path = new Path(plotfileParam);
            if (ResourceUtil.isExistingWorkspaceFile(path)) {
                plotPath = path;
            } else {
                // Try refreshing in case the file has been externally added to the workspace.
                refreshWorkspace();
                if (ResourceUtil.isExistingWorkspaceFile(path)) {
                    plotPath = path;
                } else {
                    LOGGER.log(Level.WARNING, "Databrowser plot file " + path + " does not exist.");
                }
            }
        }
        return plotPath;
    }

    /**
     * Parse the 'pv' parameter. This is a comma separated
     * list of PV names. Trailing comma and whitespace are ignored.
     *
     * @param pvNamesParam Formatted PV names string
     * @return List of PVNames, empty if none specified
     */
    private List<String> parsePvNames(String pvNamesParam) {
        List<String> pvNames = new ArrayList<String>();

        if (pvNamesParam != null) {
            Collections.addAll(pvNames,
                    StringUtils.split(pvNamesParam.replace(" ",  ""), PV_NAME_SEPARATOR));
        }
        return pvNames;
    }

    /**
     * Create a new instance of the DataBrowserEditor
     *
     * If 'path' argument is non-Null the instance points to the
     * specified plotfile, otherwise a new empty browser is created
     *
     * @param path Plotfile path
     * @return
     */
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

    /**
     * Attempt to refresh the Eclipse workspace.  If it fails, log
     * an error.
     */
    private void refreshWorkspace() {
        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            root.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            LOGGER.log(Level.WARNING, "Workspace refresh failed unexpectedly.");
        }
    }

}

package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.openfile.IOpenDisplayAction;
import org.eclipse.osgi.util.NLS;

/**
 * Use the contents of the XML file specified on the command line to manage
 * opening of new Eclipse workbench windows in particular perspectives.
 */
public class OpenNewWindow implements IOpenDisplayAction {

    private static boolean firstRun = true;
    private static final String LAUNCH_FILE_PROPERTY = "launch_file";

    /**
     * Parse the specified XML file and act on its contents.
     * @param path file to parse
     * @param data extra data; ignored
     */
    @Override
    public void openDisplay(String path, String data) {
        Plugin.getLogger().config(NLS.bind(Messages.OpenNewWindow_openLog, path, data));
        Path p = Paths.get(path);
        if (Files.exists(p)) {
            try (InputStream stream = Files.newInputStream(p)) {
                WindowSpecParser parser = new WindowSpecParser(stream);
                WindowSpec windowSpec = parser.parse();
                if (!windowSpec.getLinks().isEmpty()) {
                    updateLinks(windowSpec.getLinks());
                }
                if (!(windowSpec.getPerspectiveFile() == null)) {
                    PerspectiveHelper perspectiveHelper = new PerspectiveHelper(windowSpec);
                    perspectiveHelper.loadPerspective();
                }
                WindowOpener windowOpener = new WindowOpener(windowSpec);
                // If CS-Studio is opening for the first time, the first window should open
                // in the specified perspective.  Otherwise, a new window should open in the
                // specified perspective.
                if (newlyStarted()) {
                    windowOpener.changeActivePerspective();
                } else {
                    windowOpener.openNewWindow();
                }
            } catch (IOException | WindowManagementException e) {
                Plugin.getLogger().log(Level.WARNING, NLS.bind(Messages.OpenNewWindow_loadFailed, path), e);
            }
        } else {
            Plugin.getLogger().info(NLS.bind(Messages.OpenNewWindow_fileNotFound, path));
        }
    }

    /**
     * Update the Eclipse links according to the contents of the map.
     * @param links the map containing the required links: filesystem path -> eclipse path
     */
    private void updateLinks(Map<String, String> links) {
        LinkUpdater linkUpdater = new LinkUpdater(links);
        try {
            linkUpdater.update();
        } catch (InterruptedException e) {
            Plugin.getLogger().log(Level.WARNING, Messages.OpenNewWindow_failedUpdatingLinks, e);
        }
    }

    /**
     * Return whether this command was run as part of CS-Studio launching for the first time.
     * @return whether CS-Studio has just started
     */
    private boolean newlyStarted() {
        if (firstRun) {
            // This is very awkward.  We rely on the launch script passing this property.
            String launchFile = System.getProperty(LAUNCH_FILE_PROPERTY);
            // We are only trying to catch the first run of CS-Studio, and only then if the
            // launched file has a .nws extension.  If the first run of CS-Studio was not
            // a .nws file, we won't get the property and this will always return false.
            // If it was, set firstRun to false so that this returns true only once.
            if (launchFile != null && launchFile.endsWith(Plugin.EXT)) {
                firstRun = false;
                return true;
            }
        }
        return false;
    }

}

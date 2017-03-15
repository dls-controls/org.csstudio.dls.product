package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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

    private static final int STARTUP_TIME_MILLIS = 30000;

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
                PerspectiveHelper perspectiveHelper = new PerspectiveHelper(windowSpec);
                perspectiveHelper.loadPerspective();
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
     * Return whether this is a new instance of CS-Studio.
     *
     * This is a hack.  It checks if the JVM is more than STARTUP_TIME_MILLIS old.
     * If (for example) CS-Studio prompts for a workspace, this check could easily
     * return the wrong answer.
     * @return whether CS-Studio has just started
     */
    private boolean newlyStarted() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        return runtimeBean.getUptime() < STARTUP_TIME_MILLIS;
    }

}

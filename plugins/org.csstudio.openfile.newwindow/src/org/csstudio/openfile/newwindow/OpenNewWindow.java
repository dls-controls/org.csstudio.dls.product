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

public class OpenNewWindow implements IOpenDisplayAction {

    private static final int STARTUP_TIME_MILLIS = 15000;

    @Override
    public void openDisplay(String path, String data) {
        Plugin.getLogger().config("Opening " + path + " with data " + data);
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
                    windowOpener.changePerspective();
                } else {
                    windowOpener.openWindow();
                }
            } catch (IOException | WindowManagementException e) {
                Plugin.getLogger().log(Level.WARNING, "Failed to load window spec file", e);
            }
        }
    }

    private void updateLinks(Map<String, String> links) {
        LinkUpdater linkUpdater = new LinkUpdater(links);
        try {
            linkUpdater.update();
        } catch (InterruptedException e) {
            Plugin.getLogger().log(Level.WARNING, "Failed to update links", e);
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

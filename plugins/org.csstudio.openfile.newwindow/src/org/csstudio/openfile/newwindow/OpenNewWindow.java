package org.csstudio.openfile.newwindow;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import org.csstudio.openfile.IOpenDisplayAction;

public class OpenNewWindow implements IOpenDisplayAction {

    @Override
    public void openDisplay(String path, String data) {
        Plugin.getLogger().config("Opening " + path + " with data " + data);
        Path p = Paths.get(path);
        if (Files.exists(p)) {
            try (InputStream stream = Files.newInputStream(p)) {
                WindowSpecParser parser = new WindowSpecParser(stream);
                WindowSpec windowSpec = parser.get();
                if (!windowSpec.getLinks().isEmpty()) {
                    updateLinks(windowSpec.getLinks());
                }
                PerspectiveHelper perspectiveHelper = new PerspectiveHelper(windowSpec);
                perspectiveHelper.checkAndLoad();
                WindowOpener windowOpener = new WindowOpener(windowSpec);
                windowOpener.raiseOrOpenWindow();
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

}

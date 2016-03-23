package org.csstudio.openfile.newwindow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.csstudio.perspectives.FileUtils;
import org.csstudio.perspectives.IFileUtils;
import org.csstudio.perspectives.PerspectiveLoader;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;

public class PerspectiveHelper {

    private WindowSpec windowSpec;

    public PerspectiveHelper(WindowSpec windowSpec) {
        this.windowSpec = windowSpec;
    }

    public void checkAndLoad() throws WindowManagementException {
        if (!perspectiveExists(windowSpec.getPerspectiveId())) {
            if (windowSpec.getPerspectiveFile() != null) {
                loadPerspective(windowSpec.getPerspectiveFile());
            }
            if (!perspectiveExists(windowSpec.getPerspectiveId())) {
                throw new WindowManagementException("Perspective neither found nor successfully loaded from " + windowSpec.getPerspectiveFile());
            }
        }
    }

    public void loadPerspective(String perspectiveFile) throws WindowManagementException {
        FileUtils fu = new FileUtils();
        IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class);
        context.set(IFileUtils.class.getCanonicalName(), fu);
        PerspectiveLoader loader = ContextInjectionFactory.make(PerspectiveLoader.class, context);
        Path perspectiveFilePath = Paths.get(perspectiveFile);
        URI fileUri = fu.pathToEmfUri(Paths.get(perspectiveFile));
        if (!Files.exists(perspectiveFilePath) || fileUri == null) {
            throw new WindowManagementException("Perspective file " + perspectiveFile + " not found.");
        }
        loader.loadPerspective(fileUri);
    }

    public boolean perspectiveExists(String perspectiveId) {
        IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
        for (IPerspectiveDescriptor perspective : perspectiveRegistry.getPerspectives()) {
            System.out.println("Found perspective " + perspective.getId());
            if (perspective.getId().equals(perspectiveId)) {
                 return true;
            }
        }
        return false;
    }

}

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class to work with perspectives.
 * @author hgs15624
 *
 */
public class PerspectiveHelper {

    private WindowSpec windowSpec;

    public PerspectiveHelper(WindowSpec windowSpec) {
        this.windowSpec = windowSpec;
    }

    /**
     * Load the perspective file specified in the WindowSpec object.
     * @throws WindowManagementException if the file does not exist
     */
    public void loadPerspective() throws WindowManagementException {
        Path perspectiveFilePath = Paths.get(windowSpec.getPerspectiveFile());
        if (!Files.exists(perspectiveFilePath)) {
            throw new WindowManagementException(NLS.bind(Messages.PerspectiveHelper_fileNotFound, windowSpec.getPerspectiveFile()));
        }
        FileUtils fu = new FileUtils();
        IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class);
        context.set(IFileUtils.class.getCanonicalName(), fu);
        PerspectiveLoader loader = ContextInjectionFactory.make(PerspectiveLoader.class, context);
        URI fileUri = fu.pathToEmfUri(perspectiveFilePath);
        loader.loadPerspective(fileUri);
    }

}

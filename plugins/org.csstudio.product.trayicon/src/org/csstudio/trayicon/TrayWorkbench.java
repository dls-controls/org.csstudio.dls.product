package org.csstudio.trayicon;

import java.util.Map;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.startup.module.WorkbenchExtPoint;
import org.csstudio.utility.product.Workbench;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class TrayWorkbench extends Workbench implements WorkbenchExtPoint {

    /**
     * Creates a workbench advisor to be used by the created workbench. This methods allows
     * to provide your own advisor and keep the rest of the logic intact.
     *
     * @param parameters the parameters that may give hints on how to create the advisor
     * @return a new advisor instance
     */
	@Override
    protected WorkbenchAdvisor createWorkbenchAdvisor(final Map<String, Object> parameters) {
        final OpenDocumentEventProcessor openDocProcessor =
                  (OpenDocumentEventProcessor) parameters.get(
                          OpenDocumentEventProcessor.OPEN_DOC_PROCESSOR);
        return new TrayApplicationWorkbenchAdvisor(openDocProcessor);
    }

}

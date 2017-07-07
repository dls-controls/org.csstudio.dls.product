package org.csstudio.trayicon;

import org.csstudio.startup.application.OpenDocumentEventProcessor;
import org.csstudio.utility.product.ApplicationWorkbenchAdvisor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class TrayApplicationWorkbenchAdvisor extends ApplicationWorkbenchAdvisor {

    private TrayIcon trayIcon;


    public TrayApplicationWorkbenchAdvisor(OpenDocumentEventProcessor openDocProcessor) {
        super(openDocProcessor);
        trayIcon = new TrayIcon();
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(final IWorkbenchWindowConfigurer configurer) {
        return new TrayApplicationWorkbenchWindowAdvisor(configurer, trayIcon);
    }

    @Override
    public void postStartup() {
        IEclipseContext context = PlatformUI.getWorkbench().getService(IEclipseContext.class);
        // Initialise a perspective saver.
        TrayListener trayListener = ContextInjectionFactory.make(TrayListener.class, context);
        trayListener.setTrayIcon(trayIcon);
    }

}

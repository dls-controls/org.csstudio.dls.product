package org.csstudio.openfile.newwindow;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.csstudio.openfile.newwindow.messages"; //$NON-NLS-1$

    public static String FileUtils_selectFile;

    public static String OpenNewWindow_openLog;
    public static String OpenNewWindow_loadFailed;
    public static String OpenNewWindow_fileNotFound;
    public static String OpenNewWindow_failedUpdatingLinks;
    public static String PerspectiveHelper_fileNotFound;
    public static String WindowOpener_openFailed;
    public static String WindowSpecParser_tooManyElements;
    public static String WindowSpecParser_noChildren;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() { /* prevent instantiation */ }
}

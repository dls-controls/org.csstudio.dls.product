package org.csstudio.openfile.newwindow;

import java.util.logging.Logger;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Plugin extends AbstractUIPlugin {

    public static final String ID = "org.csstudio.openfile.newwindow";
    public static final String EXT = "nws";

    private static final Logger logger = Logger.getLogger(ID);

    public static Logger getLogger() {
        return logger;
    }

}

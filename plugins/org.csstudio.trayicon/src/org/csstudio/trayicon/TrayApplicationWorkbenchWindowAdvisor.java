package org.csstudio.trayicon;

import java.io.IOException;

import org.csstudio.utility.product.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class TrayApplicationWorkbenchWindowAdvisor extends ApplicationWorkbenchWindowAdvisor {

    // This requires internal understanding. Since we have changed the labels on
    // the dialog, MessageButtonWithDialog does not assign standard return codes.
    // This is fixed in Oxygen but for now we need to know what is going to be returned.
    public static final String[] BUTTON_LABELS = {
            Messages.TrayDialog_minimize,
            Messages.TrayDialog_exit,
            Messages.TrayDialog_cancel};
    private static final int MINIMIZE_BUTTON_ID = 256;
    private static final int EXIT_BUTTON_ID = 257;
    private static final int CANCEL_BUTTON_ID = IDialogConstants.CANCEL_ID;
    private static final int DIALOG_CLOSED = -1;

    private TrayIcon trayIcon;

    public TrayApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, TrayIcon trayIcon) {
        super(configurer);
        this.trayIcon = trayIcon;
    }

    /**
     * Prompt the user for selection of minimize on exit behaviour.
     *
     * @return xx_BUTTON_ID of clicked button or DIALOG_CLOSED
     */
    public int prompt() {
        Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Plugin.ID);
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent, Messages.TrayDialog_title, null,
                Messages.TrayDialog_question, MessageDialog.QUESTION, BUTTON_LABELS, 2,
                Messages.TrayDialog_rememberDecision, false);
        dialog.open();

        int response = dialog.getReturnCode();

        // Store the decision if checkbox selected on the form
        if (dialog.getToggleState()) {
            if (response == MINIMIZE_BUTTON_ID) {
                store.setValue(TrayIconPreferencePage.MINIMIZE_TO_TRAY, MessageDialogWithToggle.ALWAYS);
            } else if (response == EXIT_BUTTON_ID) {
                store.setValue(TrayIconPreferencePage.MINIMIZE_TO_TRAY, MessageDialogWithToggle.NEVER);
            }
            try {
                store.save();
            } catch (IOException e) {
                Plugin.getLogger().warning(Messages.TrayPreferences_saveFailed + e.getMessage());
            }
        }
        return response;
    }

    /**
     * If this is the last shell prompt handle minimise to tray option. If the
     * 'never' preference isn't set, prompt the user for an action.
     * 
     *  Three possible outcomes:
     *  i) abort the exit (return False)
     *      * user:CANCEL
     *      * user:DIALOG_CLOSED
     *  ii) continue to close this window (return preWindowShellClose())
     *      * multiple windows
     *      * already minimised
     *      * preference:NEVER
     *      * user:EXIT
     *  iii) create trayIcon and close the window
     *      * preference:ALWAYS
     *      * user:MINIMIZE
     *
     *  Returns true to allow window to close; false to prevent window closing
     *
     * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowShellClose
     */
    @Override
    public boolean preWindowShellClose() {
        // If there are multiple workbench windows open or the application is already minimised,
        // bypass the minimise to tray functionality and continue with base class implementation
        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1 || trayIcon.isMinimized()) {
            return super.preWindowShellClose();
        }

        IPreferencesService prefs = Platform.getPreferencesService();
        String minPref = prefs.getString(Plugin.ID, TrayIconPreferencePage.MINIMIZE_TO_TRAY, null, null);

        // If preference is Never bypass
        if (minPref.equals(MessageDialogWithToggle.NEVER)) {
            return super.preWindowShellClose();
        }

        if (minPref.equals(MessageDialogWithToggle.ALWAYS)) {
            trayIcon.minimize();
            return false;
        }

        if (minPref.equals(MessageDialogWithToggle.PROMPT)) {
            switch (prompt()) {
            case EXIT_BUTTON_ID:
                return super.preWindowShellClose();
            case MINIMIZE_BUTTON_ID:
                trayIcon.minimize();
                return false;
            case CANCEL_BUTTON_ID:
            case DIALOG_CLOSED:
            default:
                return false;
            }
        }

        return false;
    }
}

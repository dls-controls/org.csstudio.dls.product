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

    // This requires internal understanding.  Since we have changed the labels on the dialog,
    // MessageButtonWithDialog does not assign standard return codes.  This is fixed in Oxygen
    // but for now we need to know what is going to be returned.
    public static final String[] BUTTON_LABELS = {
            Messages.TrayDialog_minimize,
            Messages.TrayDialog_exit,
            Messages.TrayDialog_cancel};
    private static final int MINIMIZE_BUTTON_ID = 256;
    private static final int EXIT_BUTTON_ID = 257;
    private static final int CANCEL_BUTTON_ID = IDialogConstants.CANCEL_ID;
    private static final int DIALOG_CLOSED = -1;

    private TrayIcon trayIcon;

    public TrayApplicationWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer, TrayIcon trayIcon) {
        super(configurer);
        this.trayIcon = trayIcon;
    }

    public int prompt() {
        Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Plugin.ID);
        MessageDialogWithToggle dialog = new MessageDialogWithToggle(parent, Messages.TrayDialog_title, null,
                Messages.TrayDialog_question, MessageDialog.QUESTION,
                BUTTON_LABELS, 2, Messages.TrayDialog_rememberDecision, false);
        dialog.open();
        int response = dialog.getReturnCode();
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

    @Override
    public boolean preWindowShellClose() {
        if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 1) {
            return super.preWindowShellClose();
        } else {
            IPreferencesService prefs = Platform.getPreferencesService();
            String minPref = prefs.getString(Plugin.ID, TrayIconPreferencePage.MINIMIZE_TO_TRAY, null, null);
            if (trayIcon.isMinimized() || minPref.equals(MessageDialogWithToggle.NEVER)) {
                return super.preWindowShellClose();
            } else {
                if (minPref.equals(MessageDialogWithToggle.PROMPT)) {
                    int response = prompt();
                    if (response == CANCEL_BUTTON_ID || response == DIALOG_CLOSED) {
                        return false;
                    }
                    if (response == EXIT_BUTTON_ID) {
                        return super.preWindowShellClose();
                    }
                }
                trayIcon.minimize();
                return false;
            }
        }
    }

}
